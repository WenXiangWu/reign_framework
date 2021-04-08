package com.reign.framework.jdbc.handlers;

import com.reign.framework.jdbc.BasicRowProcessor;
import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.RowProcessor;
import sun.plugin2.main.server.ResultHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName: ArrayHandler
 * @Description: 数组处理器
 * @Author: wuwx
 * @Date: 2021-04-08 14:52
 **/
public class ArrayHandler implements ResultSetHandler<Object[]> {

    /**默认Processor*/
    static final RowProcessor ROW_PROCESSOR = new BasicRowProcessor();

    /**转化器*/
    private final RowProcessor convert;

    public ArrayHandler(){
        this(ROW_PROCESSOR);
    }

    public ArrayHandler(RowProcessor convert){
        super();
        this.convert = convert;
    }

    @Override
    public Object[] handler(ResultSet rs) throws SQLException {
        return rs.next()?this.convert.toArray(rs):null;
    }
}
