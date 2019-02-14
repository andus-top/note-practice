package com.ysl.note.bigdata;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 记录一些Hbase基本操作
 * @author YSL
 * 2019-01-31 13:31
 */
public class Hbase {

    private Connection connection;
    private Admin admin;

    private String COLUMN_FAMILY1 = "cf1";
    private String COLUMN_FAMILY2 = "cf2";
    private String COLUMN1 = "col1";
    private String COLUMN2 = "col2";
    private Table table;

    @Before
    public void conn() throws IOException {
        Configuration hconf = HBaseConfiguration.create();
        hconf.set("hbase.zookeeper.quorum", "172.19.209.1:2181,172.19.209.131:2181,172.19.209.132:2181");
        connection = ConnectionFactory.createConnection(hconf);
        admin = connection.getAdmin();
        table = connection.getTable(TableName.valueOf("test"));
    }

    @After
    public void close() throws IOException {
        if(connection != null)connection.close();
        if(admin != null)admin.close();
        if(table != null)table.close();
    }

    /**
     * 存数据
     * shell：put 't1', 'r1', 'c1', 'value', ts1
     * t1指表名，r1指行键名，c1指列名，value指单元格值。ts1指时间戳，一般都省略掉了。
     */
    @Test
    public void putData() throws IOException {
        List data = new ArrayList<Put>();
        for (int i = 0; i < 5; i++) {
            //构建put的参数是rowkey   rowkey_i (Bytes工具类，各种java基础数据类型和字节数组之间的相互转换)
            Put put = new Put(Bytes.toBytes("rowkey_"+i));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(COLUMN1), Bytes.toBytes("电话_"+i));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY1), Bytes.toBytes(COLUMN2), Bytes.toBytes("邮箱_"+i));
            put.addColumn(Bytes.toBytes(COLUMN_FAMILY2), Bytes.toBytes(COLUMN1), Bytes.toBytes("测试_"+i));
            // 存放单个记录
            //table.put(put);
            data.add(put);
        }
        table.put(data);
        System.out.println("插入记录成功");

    }

    /**
     * 通过hbase api 读取数据
     * @author YSL
     * 2019-01-31 18:01
     */
    @Test
    public void scan() throws IOException {
        Scan scan = new Scan();// 不指定，扫描全部
        scan.addFamily(Bytes.toBytes(COLUMN_FAMILY1));// 扫描指定列簇
        scan.addColumn(Bytes.toBytes(COLUMN_FAMILY2), Bytes.toBytes(COLUMN1));// 扫描指定列
        ResultScanner rs = table.getScanner(scan);
        for (Result result : rs) {
            CellScanner cellScanner = result.cellScanner();
            while (cellScanner.advance()) {

                //从单元格cell中把数据获取并输出
                //使用 CellUtil工具类，从cell中把数据获取出来
                Cell cell = cellScanner.current();

                String rowkey = Bytes.toString(CellUtil.cloneRow(cell));// rowkey
                String famliy = Bytes.toString(CellUtil.cloneFamily(cell));// 列簇
                String qualify = Bytes.toString(CellUtil.cloneQualifier(cell));// 列
                Long value = Bytes.toLong(CellUtil.cloneValue(cell)); // 值
                System.out.println("rowkey:" + rowkey + ",  columnfamily:" + famliy + ",  column:" + qualify + ",  value:" + value);
            }
        }
    }

    /**
     * 获取通过rowkey获取数据
     * @author YSL
     * 2019-02-01 15:22
     */
    public void get() throws IOException {
        List<Get> gets = new ArrayList<Get>();

        Get get = new Get(Bytes.toBytes("rowkey"));
        //get.addFamily(Bytes.toBytes(COLUMN_FAMILY1)); // 获取指定列族数据
        //get.addColumn(Bytes.toBytes(COLUMN_FAMILY1),Bytes.toBytes(COLUMN1));// 获取指定列数据

        gets.add(get);

        // Result results = table.get(get);// 查询单行
        Result[] results = table.get(gets);// 查询多行

        for (Result result : results) {
            //使用cell获取result里面的数据
            CellScanner cellScanner = result.cellScanner();
            while (cellScanner.advance()) {
                Cell cell = cellScanner.current();
                //从单元格cell中把数据获取并输出
                //使用 CellUtil工具类，从cell中把数据获取出来
                String rowkey = Bytes.toString(CellUtil.cloneRow(cell));// rowkey
                String famliy = Bytes.toString(CellUtil.cloneFamily(cell));// 列簇
                String qualify = Bytes.toString(CellUtil.cloneQualifier(cell));// 列
                Long value = Bytes.toLong(CellUtil.cloneValue(cell)); // 值
                System.out.println("rowkey:" + rowkey + ",  columnfamily:" + famliy + ",  column:" + qualify + ",  value:" + value);
            }
        }
    }

    /**
     * 删除 rowkey中以目标字符串开头的行
     * @author YSL
     * 2019-01-31 17:51
     */
    @Test
    public void delRowFileter() throws IOException {
        Scan scan = new Scan();
        Filter filter = new RowFilter(CompareOperator.EQUAL, new BinaryPrefixComparator(Bytes.toBytes("rowkey")));
        scan.setFilter(filter);
        ResultScanner results = table.getScanner(scan);
        List<Delete> deletes = new ArrayList<>();
        for (Result result : results) {
            deletes.add(new Delete(result.getRow()));
        }
        table.delete(deletes);
        System.out.println("删除HBase数据成功");
        table.close();
    }

    /**
     * 删除rowkey中以目标字符串开头的行
     * @author YSL
     * 2019-01-31 17:52
     */
    @Test
    public void startRow() throws IOException {
        Scan scan = new Scan();
        // 扫描 rowkey大于rowkey1并且小于rowkey2的行
        scan.withStartRow(Bytes.toBytes("rowkey1#"));
        scan.withStopRow(Bytes.toBytes("rowkey2|"));
        ResultScanner results = table.getScanner(scan);
        List<Delete> deletes = new ArrayList<>();
        for (Result result : results) {
            deletes.add(new Delete(result.getRow()));
        }
        table.delete(deletes);
        System.out.println("删除HBase数据成功");
        table.close();
    }

    /**
     * 列值过滤查询
     * @author YSL
     * 2019-01-31 17:54
     */
    @Test
    public void singleColumnValueFilter() throws IOException {
        Scan queryScan = new Scan();
        queryScan.addFamily(Bytes.toBytes(COLUMN_FAMILY2));
        queryScan.addColumn(Bytes.toBytes(COLUMN_FAMILY1),Bytes.toBytes(COLUMN1));// 过滤条件也需要查询，否则过滤无效

        // 根据列的值来决定这一行数据是否返回，落脚点在行，而不是列。
        SingleColumnValueFilter valueFilter = new SingleColumnValueFilter(COLUMN_FAMILY1.getBytes(), COLUMN1.getBytes(), CompareOperator.GREATER,Bytes.toBytes(0l));
        valueFilter.setFilterIfMissing(true);// 这里必须设置为true。 如果为true，当这一列不存在时，不会返回，如果为false（默认），当这一列不存在时，会返回所有的列信息
        queryScan.setFilter(valueFilter);
        ResultScanner rs = table.getScanner(queryScan);
        for (Result result : rs) {
            CellScanner cellScanner = result.cellScanner();
            while (cellScanner.advance()) {

                //从单元格cell中把数据获取并输出
                //使用 CellUtil工具类，从cell中把数据获取出来
                Cell cell = cellScanner.current();

                String rowkey = Bytes.toString(CellUtil.cloneRow(cell));// rowkey
                String famliy = Bytes.toString(CellUtil.cloneFamily(cell));// 列簇
                String qualify = Bytes.toString(CellUtil.cloneQualifier(cell));// 列
                Long value = Bytes.toLong(CellUtil.cloneValue(cell)); // 值
                System.out.println("rowkey:" + rowkey + ",  columnfamily:" + famliy + ",  column:" + qualify + ",  value:" + value);
            }
        }
    }

    /**
     * 多过滤条件查询
     *  查询日期为"2018-09-01","2018-10-01","2018-11-01"这三天 且 cf1.col1列的值大于0的数据
     * @author YSL
     * 2019-01-31 18:14
     */
    @Test
    public void multipleFilter() throws IOException {
        Scan queryScan = new Scan();
        // rowkey过滤条件
        // 有“与” FilterList.Operator.MUST_PASS_ALL 和“或” FilterList.Operator.MUST_PASS_ONE 关系。
        FilterList filterOne = new FilterList(FilterList.Operator.MUST_PASS_ONE);// 或关系

        String[]  days = {"2018-09-01","2018-10-01","2018-11-01"};
        for (String day : days) {
            RowFilter rowFilter = new RowFilter(CompareOperator.EQUAL, new BinaryPrefixComparator(Bytes.toBytes(day)));
            filterOne.addFilter(rowFilter);
        }
        // 列值过滤条件，该列必须添加到san中
        SingleColumnValueFilter valueFilter = new SingleColumnValueFilter(COLUMN_FAMILY1.getBytes(), COLUMN1.getBytes(), CompareOperator.GREATER,Bytes.toBytes(0l));
        // 这里必须设置为true。 如果为true，当这一列不存在时，不会返回，如果为false（默认），当这一列不存在时，会返回所有的列信息
        valueFilter.setFilterIfMissing(true);
        FilterList filterAll = new FilterList(FilterList.Operator.MUST_PASS_ALL);// 与关系
        filterAll.addFilter(filterOne);// filter是有序的,filterOne先匹配
        filterAll.addFilter(valueFilter);
        queryScan.setFilter(filterAll);

        ResultScanner rs = table.getScanner(queryScan);
        for (Result result : rs) {
            CellScanner cellScanner = result.cellScanner();
            while (cellScanner.advance()) {

                //从单元格cell中把数据获取并输出
                //使用 CellUtil工具类，从cell中把数据获取出来
                Cell cell = cellScanner.current();

                String rowkey = Bytes.toString(CellUtil.cloneRow(cell));// rowkey
                String famliy = Bytes.toString(CellUtil.cloneFamily(cell));// 列簇
                String qualify = Bytes.toString(CellUtil.cloneQualifier(cell));// 列
                Long value = Bytes.toLong(CellUtil.cloneValue(cell)); // 值
                System.out.println("rowkey:" + rowkey + ",  columnfamily:" + famliy + ",  column:" + qualify + ",  value:" + value);
            }
        }

    }

    /**
     * 创建表
     * shell: create '命名空间:表名', {NAME => '列簇1', VERSIONS => 1},{NAME => '列簇2', VERSIONS => 1}
     *  versions=>1 : 使hbase数据只有一个备份，默认3
     */
    @Test
    public void createTab() throws IOException {
        String[]  cf = new String[]{COLUMN_FAMILY1, COLUMN_FAMILY2};
        // 创建tablename obj 描述表的名称信息
        TableName tname = TableName.valueOf("test");
        // 创建HTableDescriptor 描述表信息
        HTableDescriptor tableDescriptor = new HTableDescriptor(tname);
        // 判断该表是否存在
        if(admin.tableExists(tname)){
            System.out.println("test表已存在");
            return;
        }

        // 添加列簇
        for (String c : cf) {
            HColumnDescriptor famliy = new HColumnDescriptor(c);
            tableDescriptor.addFamily(famliy);
            famliy = null;
        }
        // 创建表
        admin.createTable(tableDescriptor);
        System.out.println("创建test表成功");
    }

    /**
     * 删除表
     * shell :
     *  disable '命名空间:表名'
     *  drop '命名空间:表名'
     */
    @Test
    public void delTable() throws IOException {
        TableName tableName = TableName.valueOf("test");
        if(admin.tableExists(tableName)){
            // 删除表之前先disable表
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("删除test表成功");
        }else{
            System.out.println("该表不存在");
        }

    }

    /**
     * 查看所有的表
     * shell: list
     * @return
     */
    @Test
    public void allTabName() throws IOException {
        TableName[] allTable = admin.listTableNames();
        for (int i = 0; i < allTable.length; i++) {
            System.out.println(allTable[i].getNameAsString());
        }
    }

    /**
     * 判断命名空间是否存在
     * @return
     */
    @Test
    public void namespaceExists() throws IOException {
        String namespace = "test";
        NamespaceDescriptor[] names = listNameSpace(); // 获取所有的命名空间
        for (NamespaceDescriptor desc : names) {
            if (namespace.equals(desc.getName())){
                System.out.println(namespace+"该命名空间存在");
            }
        }
    }


    /**
     * 获取所有命名空间
     * shell: list_namespace
     * @throws IOException
     */
    public NamespaceDescriptor[] listNameSpace() throws IOException {
        NamespaceDescriptor[] list = admin.listNamespaceDescriptors();
        for (NamespaceDescriptor namespaceDescriptor : list) {
            System.out.println(namespaceDescriptor.getName());
        }
        return list;
    }


    /**
     * 创建命名空间
     * shell：create_namespace '名称'
     *   删除：drop_namespace '名称'
     * @param namespace
     * @return
     */
    public void createNamespace(String namespace) throws IOException {
        admin.createNamespace(NamespaceDescriptor.create(namespace).build());
        System.out.println("创建"+namespace+"成功");
    }
}
