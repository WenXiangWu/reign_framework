package com.reign.framework.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: BasicRowProcessor
 * @Description: 基本转换器
 * @Author: wuwx
 * @Date: 2021-04-07 18:13
 **/
public class BasicRowProcessor implements RowProcessor {


    private static final BeanProcessor convert = new BeanProcessor();

    @Override
    public Object[] toArray(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int cols = meta.getColumnCount();
        Object[] result = new Object[cols];
        for (int i = 0; i < cols; i++) {
            result[i] = rs.getObject(i + 1);
        }
        return result;
    }

    @Override
    public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
        return convert.toBean(rs, type);
    }

    @Override
    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
        return convert.toBeanList(rs, type);
    }

    @Override
    public Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        for (int i = 1; i < cols; i++) {
            result.put(rsmd.getColumnName(i), rs.getObject(i));
        }
        return result;
    }
}
