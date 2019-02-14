package com.ysl.note.bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
import org.apache.hadoop.hbase.mapred.TableOutputFormat;
import org.apache.hadoop.hbase.mapreduce.TableInputFormat;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.mapred.JobConf;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.*;
import org.apache.spark.sql.api.java.UDF2;
import org.apache.spark.sql.hive.HiveContext;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import scala.Tuple2;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.reflect.ClassTag;
import scala.reflect.ClassTag$;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * 记录一些Spark基本操作
 *  不保证程序能运行
 * @author YSL
 * 2019-01-31 13:31
 */
public class Spark {

    private SparkSession sparkSession;
    private Broadcast<Map<String, BigDecimal>> broadcastData;// 广播的变量
    private String ZOOKEEPER_QUORUM = "172.19.209.1:2181,172.19.209.131:2181,172.19.209.132:2181"; // zookeeper地址

    private String COLUMN_FAMILY1 = "cf1";
    private String COLUMN_FAMILY2 = "cf2";
    private String COLUMN1 = "col1";
    private String COLUMN2 = "col2";

    @Before
    public void getSparkSession(){
        sparkSession = SparkSession.builder()
                .enableHiveSupport()// spark连接hive
                .appName("sparkTest")
                .getOrCreate();
        sparkSession.sql("use test");
    }

    @After
    public void close(){
        if(sparkSession != null){
            sparkSession.close();
        }
    }

    /**
     * Spark 广播变量
     * @author YSL
     * 2019-01-31 13:33
     */
    @Test
    public void broadcast(){
        Map<String, BigDecimal> map = new HashMap<>();
        ClassTag<Map<String, BigDecimal>> tag = ClassTag$.MODULE$.apply(Map.class);
        broadcastData = sparkSession.sparkContext().broadcast(map, tag);
    }

    /**
     * spark 注册用户自定义函数
     * @author YSL
     * 2019-01-31 13:43
     */
    @Test
    public void udf(){
        /**
         * 方法名称
         * 目标方法
         * 返回类型
         */
        sparkSession.udf().register("myUdf", myUdf(), DataTypes.createDecimalType(18, 6));

        // 使用
        testUdfInSql();
        testUdfInFun();
    }

    /**
     * sql中使用自定义函数
     */
    public void testUdfInSql(){
        String sql = "select id, sum(getDollar(type, price)) as pay from order";
        Dataset<Row> payDf = sparkSession.sql(sql);
    }

    /**
     * 代码中使用自定义函数
     */
    public void testUdfInFun(){
        String sql = "select id, type, price from order";
        Dataset<Row> payDf = sparkSession.sql(sql);
        payDf.select(functions.callUDF("myUdf",payDf.col("type"),payDf.col("price")).alias("pay"));
    }


    /**
     * String：第一个参数类型
     * BigDecimal：第二个参数类型
     * BigDecimal：返回结果类型
     * @return
     */
    public UDF2<String, BigDecimal, BigDecimal> myUdf(){
        return new UDF2<String, BigDecimal, BigDecimal>() {
            @Override
            public BigDecimal call(String type, BigDecimal pay) {
                // 获取广播变量中的数据
                BigDecimal rate = broadcastData.value().get(type);
                if (rate != null && pay != null) {
                    return pay.multiply(rate);
                } else {
                    return pay;
                }
            }
        };
    }

    /**
     * 对指定的列去重，随机保留某一个结果[一般是靠前的结果]
     */
    @Test
    public void dropDuplicates(){
        List<String> duplicateColL = new ArrayList<String>();
        duplicateColL.add("gid");
        Seq<String> duplicateCols = JavaConversions.asScalaBuffer(duplicateColL);

        String sql = "select gid, data, dt from test";
        Dataset<Row> resDf = sparkSession.sql(sql).dropDuplicates(duplicateCols);// 随机保留某一个结果[一般是靠前的结果]
    }

    /**
     * spark读取hbase，并转成Dataset<Row>
     *     没有phoenix读取hbase快
     * @author YSL
     * 2019-01-31 16:46
     */
    @Test
    public void sparkReadHbaseToDs() throws IOException {
        SparkConf conf = new SparkConf().setAppName("test");
        JavaSparkContext sc = new JavaSparkContext(conf);
        // FIXME 更改为spark2.4 API
        HiveContext hiveContext = new HiveContext(sc);
        hiveContext.sql("use test");

        System.out.println("开始读取hbase rtc..");
        Configuration configuration = HBaseConfiguration.create();
        configuration.set("hbase.zookeeper.quorum", ZOOKEEPER_QUORUM); // zookeeper地址
        //configuration.set(TableInputFormat.INPUT_TABLE, "命名空间:表名(区分大小写)");
        configuration.set(TableInputFormat.INPUT_TABLE, "test");
        Scan queryScan = new Scan();

        //ClientProtos.Scan proto = ProtobufUtil.toScan(queryScan);
		//String scanToString  = Base64.getEncoder().encodeToString(proto.toByteArray());
        String scanToString = TableMapReduceUtil.convertScanToString(queryScan);
        configuration.set(TableInputFormat.SCAN, scanToString);

        JavaPairRDD<ImmutableBytesWritable, Result> myRdd = sc.newAPIHadoopRDD(configuration, TableInputFormat.class, ImmutableBytesWritable.class, Result.class);
        JavaRDD<Row> rdd = myRdd.map(tuple2 -> {
            Result result = tuple2._2();
            //String rowKey = Bytes.toString(result.getRow());
            String chl = Bytes.toString(result.getValue(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(COLUMN1)));

            byte[] bytes1 = result.getValue(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(COLUMN2));
            byte[] bytes2 = Bytes.toBytes(0);
            Integer pf = Bytes.toInt(bytes1 == null ? bytes2 : bytes1);

            return RowFactory.create(chl, pf);
        });

        List<StructField> structFields = new ArrayList<>();
        //structFields.add(DataTypes.createStructField("rowkey", DataTypes.StringType, false));
        structFields.add(DataTypes.createStructField(COLUMN1, DataTypes.StringType, true));
        structFields.add(DataTypes.createStructField(COLUMN2, DataTypes.IntegerType, false));
        StructType schema = DataTypes.createStructType(structFields);

        Dataset<Row> resDf = hiveContext.createDataFrame(rdd, schema);
    }

    /**
     * spark将Dataset<Row>写入hbase
     *      没有phoenix写入hbase快
     * @author YSL
     * 2019-02-01 15:37
     */
    public void sparkWriteHbase(){

        // 假设从hive表拿数据
        Dataset<Row> testDf = sparkSession.sql("select str,num,lg,bd from test");

        // 连接hbase
        Configuration hconf = HBaseConfiguration.create();
        hconf.set("hbase.zookeeper.quorum", ZOOKEEPER_QUORUM);
        JobConf gConf = new JobConf(hconf);
        gConf.set(TableOutputFormat.OUTPUT_TABLE, "test");
        gConf.setOutputFormat(TableOutputFormat.class);

        testDf.javaRDD().mapToPair(row -> {

            String str = row.getString(0);
            int num = row.getInt(1);
            Long lg = row.getLong(2);
            BigDecimal bd = row.getDecimal(3);

            Put put = new Put(Bytes.toBytes("rowkey"));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(COLUMN1), Bytes.toBytes(str));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(COLUMN2), Bytes.toBytes(num));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY2), Bytes.toBytes(COLUMN1), Bytes.toBytes(lg));
            // 因为经常会使用phoenix去连接hbase，而目前phoenix5.0.0-hbase2.0.0无decimal类型，所以使用double
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY2), Bytes.toBytes(COLUMN2), Bytes.toBytes(bd.doubleValue()));

            return new Tuple2<ImmutableBytesWritable, Put>(new ImmutableBytesWritable(), put);
        }).saveAsHadoopDataset(gConf);
        System.out.println("写入hbase成功");
    }




}
