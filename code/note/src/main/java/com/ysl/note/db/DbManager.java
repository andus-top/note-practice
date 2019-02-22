package com.ysl.note.db;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 数据库的操作的简单封装
 * 使用：
 * DbManager db = new Dbmanager(Dbconfig.TEST);
 * db.execute(sqlList);
 * db.close();
 */
public class DbManager implements Serializable {
    private static final long serialVersionUID = -562421134854999697L;
    private final static String mysqlUrlFormat = "jdbc:mysql://%s:%d/%s?useUnicode=false&characterEncoding=UTF8";
    private final static String phoenixUrlFormat = "jdbc:phoenix:%s:%d";
    private DbConfig config = null;
    private Connection conn = null;

    public DbManager(DbConfig config) throws Exception{
        this.config = config;
        initDb(config);
    }

    public static String genJdbcUrl(DbConfig config){
        return String.format(mysqlUrlFormat, config.getHost(), config.getPort(), config.getDatabase());
    }

    /**
     * 初始化connection
     * @param config
     * @throws Exception
     */
    public void initDb(DbConfig config) throws Exception {
        Class.forName(String.format("%s", config.getDriver()));
        String jdbcUrl = null;
        if("com.mysql.jdbc.Driver".equals(config.getDriver())){
            jdbcUrl = String.format(mysqlUrlFormat, config.getHost(), config.getPort(), config.getDatabase());
            conn = DriverManager.getConnection(jdbcUrl, config.getUserName(), config.getPassword());
        }else if("org.apache.phoenix.jdbc.PhoenixDriver".equals(config.getDriver())){
            Properties props = new Properties();
            props.setProperty("phoenix.functions.allowUserDefinedFunctions", "true");// 允许自定义函数
            props.setProperty("phoenix.schema.isNamespaceMappingEnabled", "true");// 允许hbase 命名空间
            props.setProperty("phoenix.default.column.encoded.bytes.attrib", "0");//不对hbase列进行编码存储
            jdbcUrl = String.format(phoenixUrlFormat, config.getHost(), config.getPort());
            conn = DriverManager.getConnection(jdbcUrl, props);
        }
    }

    /**
     * 查询列表
     * @param clazz 结果类
     * @param sql   查询语句
     * @param <T>
     * @return      返回查询结果列表
     * @throws Exception
     */
    public <T> List<T> queryList(Class<T> clazz, String sql) throws Exception {
        // 如果连接断了，重连
        if (conn.isClosed()) {
            initDb(this.config);
        }

        // 查询
        Statement stm = conn.createStatement();
        ResultSet set = stm.executeQuery(sql);
        Method[] methods = clazz.getMethods();
        T obj = null;
        List<T> list = new ArrayList<>();
        while (set.next()) {
            obj = clazz.newInstance();
            for (Method method : methods) {
                String methodName = method.getName();
                if (methodName.startsWith("set")) {
                    String columnName = methodName.substring(3, methodName.length());
                    Class[] parmts = method.getParameterTypes();
                    try {
                        if (parmts[0] == String.class) {
                            method.invoke(obj, set.getString(columnName));
                        } else if (parmts[0] == int.class) {
                            method.invoke(obj, set.getInt(columnName));
                        }
                    } catch (Exception e) {}
                }
            }
            list.add(obj);
        }

        return list;
    }

    /**
     * 查询一行
     * @param clazz 结果类
     * @param sql   查询语句
     * @param <T>
     * @return      返回一行查询结果
     * @throws Exception
     */
    public <T> T queryRow(Class<T> clazz, String sql) throws Exception {
        // 如果连接断了，重连
        if (conn.isClosed()) {
            initDb(this.config);
        }

        // 查询
        Statement stm = conn.createStatement();
        ResultSet set = stm.executeQuery(sql);
        Method[] methods = clazz.getMethods();
        T obj = null;
        List<T> list = new ArrayList<>();
        while (set.next()) {
            obj = clazz.newInstance();
            for (Method method : methods) {
                String methodName = method.getName();
                if (methodName.startsWith("set")) {
                    String columnName = methodName.substring(3, methodName.length());
                    Class[] parmts = method.getParameterTypes();
                    try {
                        if (parmts[0] == String.class) {
                            method.invoke(obj, set.getString(columnName));
                        } else if (parmts[0] == int.class) {
                            method.invoke(obj, set.getInt(columnName));
                        }
                    } catch (Exception e) {}
                }
            }
            break;
        }

        return obj;
    }

    /**
     * 查询列表
     * @param sql   查询语句
     * @return      返回查询结果列表
     * @throws Exception
     */
    public ResultSet queryList(String sql) throws Exception {
        // 如果连接断了，重连
        if (conn.isClosed()) {
            initDb(this.config);
        }

        // 查询
        Statement stm = conn.createStatement();
        return stm.executeQuery(sql);
    }

    /**
     * 查询数量
     * @param sql
     * @return
     * @throws Exception
     */
    public int queryCount(String sql) throws Exception {
        // 如果连接断了，重连
        if (conn.isClosed()) {
            initDb(this.config);
        }

        // 查询
        int count = 0;
        Statement stm = conn.createStatement();
        ResultSet set = stm.executeQuery(sql);
        if(set.next()) {
            count = set.getInt(1);
        };

        return count;
    }

    /**
     * 批量执行sql语句
     * @param sqlList
     * @return
     * @throws Exception
     */
    public int execute(List<String> sqlList) throws Exception{
        if (sqlList == null || sqlList.size() == 0) {
            return 0;
        }

        // 如果连接断了，重连
        if (conn.isClosed()) {
            initDb(this.config);
        }

        Statement stm = conn.createStatement();
        conn.setAutoCommit(false);
        for (String sql : sqlList) {
            stm.addBatch(sql);
        }

        int[] arr = stm.executeBatch();
        conn.commit();
        conn.setAutoCommit(true);
        return arr.length;
    }
	
	/**
     * 批量执行sql语句（预编译方式）
     * @param sql
     * @param params 参数
     * @return
     * @throws Exception
     */
    public int execute(String sql, List<List<Object>> params) throws Exception {
        if (conn.isClosed()) {
            initDb(this.config);
        }
        PreparedStatement pstmt = conn.prepareStatement(sql);
        conn.setAutoCommit(false);
        for (List<Object> objs : params) {
            for (int i = 0; i < objs.size(); i++) {
                pstmt.setObject(i + 1, objs.get(i));
            }
            pstmt.addBatch();
        }
        int[] arr = pstmt.executeBatch();
        conn.commit();
        conn.setAutoCommit(true);
        return arr.length;
    }

    /**
     * 执行单条sql语句
     * @param sql
     * @throws Exception
     */
    public void execute(String sql) throws Exception{
        // 如果连接断了，重连
        if (conn.isClosed()) {
            initDb(this.config);
        }

        Statement stm = conn.createStatement();
        stm.execute(sql);
        // 如果是phoenix db,需要单独commit，mysql不需要
        if ("org.apache.phoenix.jdbc.PhoenixDriver".equals(this.config.getDriver())) {
            conn.commit();
        }
    }

    /**
     * 更新单条sql语句
     * @param sql
     * @throws Exception
     */
    public int executeUpdate(String sql) throws Exception{
        // 如果连接断了，重连
        if (conn.isClosed()) {
            initDb(this.config);
        }

        Statement stm = conn.createStatement();
        int res = stm.executeUpdate(sql);
        // 如果是phoenix db,需要单独commit，mysql不需要
        if ("org.apache.phoenix.jdbc.PhoenixDriver".equals(this.config.getDriver())) {
            conn.commit();
        }
        return res;
    }

    /**
     * 关闭连接，在不用dbmanager对象时候调用
     * @throws Exception
     */
    public void close() throws Exception{
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    public static void main(String[] args) throws Exception {
        DbManager manager = new DbManager(DbConfig.TEST);
        String sql = "select * from test";
        List<String> list = new ArrayList<String>();
        list.add(sql);
        manager.execute(list);
        manager.close();
    }
}
