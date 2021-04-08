package com.reign.framework.jdbc.handlers;

import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @ClassName: BeanListHandler
 * @Description: 将resultSet转换为beanList
 * @Author: wuwx
 * @Date: 2021-04-08 15:09
 **/
public class BeanListHandler<T> implements ResultSetHandler<List<T>> {

    /**
     * 转化器
     */
    private final RowProcessor convert;

    private final Class<T> type;

    public BeanListHandler(Class<T> type) {
        this(type, ArrayHandler.ROW_PROCESSOR);
    }

    public BeanListHandler(Class<T> type, RowProcessor convert) {
        this.type = type;
        this.convert = convert;
    }

    @Override
    public List<T> handler(ResultSet rs) throws SQLException {
        return convert.toBeanList(rs, type);
    }
}
