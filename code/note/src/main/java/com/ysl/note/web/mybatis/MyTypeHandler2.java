package com.ysl.note.web.mybatis;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * 自定义 JavaType 与 JdbcType 之间的转换
 * 用于 PreparedStatement 设置参数值和从 ResultSet 或 CallableStatement 中取出一个值。
 * <p> xml中：</p>
 * <p><result column="like" property="like" jdbcType="CHAR" typeHandler="com.ysl.note.web.configbak.mybatis.MyTypeHandler2" /></p>
 * <p>insert into user (like) values (#{like,typeHandler=com.ysl.note.web.configbak.mybatis.MyTypeHandler2})</p>
 * <p>mybatis配置文件中：</p>
 * <p><typeHandlers><typeHandler handler="com.ysl.note.web.configbak.mybatis.MyTypeHandler"/></typeHandlers></p>
 * <p><typeHandlers><package name="com.ysl.note.web.configbak.mybatis"/></typeHandlers></p>
 * @author YSL
 * 2/12/19 9:08 PM
 */
public class MyTypeHandler2 extends BaseTypeHandler<List<String>>{
    @Override
    public void setNonNullParameter(PreparedStatement preparedStatement, int i, List<String> list, JdbcType jdbcType) throws SQLException {
        StringBuffer sb = new StringBuffer();
        for(String s : list){
            sb.append(s).append(",");
        }
        preparedStatement.setString(i,sb.toString().substring(0,sb.toString().length() - 1));
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, String s) throws SQLException {
        String[] arr = resultSet.getString(s).split(",");
        return Arrays.asList(arr);
    }

    @Override
    public List<String> getNullableResult(ResultSet resultSet, int i) throws SQLException {
        String[] arr = resultSet.getString(i).split(",");
        return Arrays.asList(arr);
    }

    @Override
    public List<String> getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
        String[] arr = callableStatement.getString(i).split(",");
        return Arrays.asList(arr);
    }
}
