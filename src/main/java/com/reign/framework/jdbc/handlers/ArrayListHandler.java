package com.reign.framework.jdbc.handlers;

import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName: ArrayListHandler
 * @Description: 将bean转为list
 * @Author: wuwx
 * @Date: 2021-04-08 15:10
 **/
public class ArrayListHandler implements ResultSetHandler<Object[]> {

    /**
     * 转化器
     */
    private final RowProcessor convert;

    public ArrayListHandler() {
        this(ArrayHandler.ROW_PROCESSOR);
    }

    public ArrayListHandler(RowProcessor convert) {
        this.convert = convert;
    }

    @Override
    public Object[] handler(ResultSet rs) throws SQLException {
        return convert.toArray(rs);
    }
}
