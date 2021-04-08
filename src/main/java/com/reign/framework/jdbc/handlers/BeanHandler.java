package com.reign.framework.jdbc.handlers;

import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName: BeanHandler
 * @Description: 将ResultSet转换为Bean
 * @Author: wuwx
 * @Date: 2021-04-08 15:05
 **/
public class BeanHandler<T> implements ResultSetHandler<T> {

    /**
     * 转化器
     */
    private final RowProcessor convert;

    private final Class<T> type;

    public BeanHandler(Class<T> type) {
        this(type, ArrayHandler.ROW_PROCESSOR);
    }

    public BeanHandler(Class<T> type, RowProcessor convert) {
        this.type = type;
        this.convert = convert;
    }

    @Override
    public T handler(ResultSet rs) throws SQLException {
        return !rs.next() ? null : convert.toBean(rs, type);
    }
}
