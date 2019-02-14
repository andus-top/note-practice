package com.ysl.note.bigdata;

import com.ysl.note.db.DbConfig;
import com.ysl.note.db.DbManager;
import org.apache.spark.api.java.function.ForeachPartitionFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.After;
import org.junit.Before;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author YSL
 * 2019-01-31 13:31
 */
public class Phoenix {

    private DbManager dbManager;
    private SparkSession sparkSession;

    @Before
    public void connDb() throws Exception {
        sparkSession = SparkSession.builder()
                .enableHiveSupport()// spark连接hive
                .appName("phoenixTest")
                .getOrCreate();
        sparkSession.sql("use test");
        dbManager = new DbManager(DbConfig.TEST);
    }

    @After
    public void close() throws Exception {
        if(sparkSession != null)sparkSession.close();
        if(dbManager != null)dbManager.close();
    }

    /**
     * phoenix读取hbase，并将结果转换成Dataset<Row>
     * @author YSL
     * 2019-02-01 17:27
     */
    public void readHbaseToDs() throws Exception {

        // 构建查询留存列对应的schema
        List<StructField> structFields = new ArrayList<>();
        //structFields.add(DataTypes.createStructField("rowkey", DataTypes.StringType, false));
        structFields.add(DataTypes.createStructField("col1", DataTypes.LongType, false));
        structFields.add(DataTypes.createStructField("col2", DataTypes.IntegerType, false));
        structFields.add(DataTypes.createStructField("col3", DataTypes.createDecimalType(), true));
        StructType schema = DataTypes.createStructType(structFields);

        System.out.println("读取hbase...");
        // hbase 列簇和列小写时，需要加引号
        String phoenix="select \"cf1\".\"col1\",\"cf1\".\"col2\",\"cf2\".\"col2\" from \"test\" where \"cf1\".\"col2\">0";
        ResultSet resultset = dbManager.queryList(phoenix);

        List<Row> list = new ArrayList<>();
        while (resultset.next()) {
            Object[] objects = {
                    resultset.getString(1),// cf1.col1
                    resultset.getInt(2),// cf1.col2
                    resultset.getString(3),// cf2.col2
            };
            Row row = new GenericRowWithSchema(objects, schema);
            list.add(row);
        }

        // 转换成Dataset<Row>
        Dataset<Row> resDf = sparkSession.createDataFrame(list, schema);
    }

    /**
     * phoenix 将Dataset<Row> 写入hbase
     * @author YSL
     * 2019-02-01 18:07
     */
    public void dsWriteByPhoenix(){
        // 假设从hive拿数据
        Dataset<Row> dataDf = sparkSession.sql("select data1,data2,data3 from test");

        dataDf.foreachPartition(new ForeachPartitionFunction<Row>() {
            @Override
            public void call(Iterator<Row> itr) throws Exception {
                // phoenix 中 schema与hbase命名空间对应
                String phoenix = "upsert into \"myschema\".\"mytable\" (rk,\"cf1\".\"col1\",\"cf2\".\"col2\") values('%s',%f,%d)";
                List<String> sqlList = new ArrayList<>();

                while (itr.hasNext()) {

                    Row row = itr.next();
                    String str = row.getString(0);
                    str = str == null ? "" : str;// 此处为phoenix 的坑，自动将""处理成null
                    double d = row.getDouble(1);
                    int num = row.getInt(2);

                    sqlList.add(String.format(phoenix, str, d, num));
                }
                if (sqlList.size() > 0) {
                    DbManager dbManager_phoenix = new DbManager(DbConfig.PHOENIX);
                    dbManager_phoenix.execute(sqlList);
                    dbManager_phoenix.close();
                }
            }
        });
    }

}
