package com.ysl.note.db;

public enum DbConfig {
    TEST("com.mysql.jdbc.Driver", "172.16.209.1", 3306, "root", "123456", "test"),
    PHOENIX("org.apache.phoenix.jdbc.PhoenixDriver", "172.19.209.1,172.19.209.131,172.19.209.132", 2181, null,null,null);

    private String driver;
    private String host;
    private int port;
    private String userName;
    private String password;
    private String database;

    DbConfig(String driver, String host, int port, String userName, String password, String database) {
        this.driver = driver;
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.database = database;
    }

    public String getDriver() {
        return driver;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getDatabase() {
        return database;
    }

}
