package com.reign.framework.jdbc.handlers;

import com.reign.framework.jdbc.RowProcessor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * @ClassName: MapListHandler
 * @Description: 返回map
 * @Author: wuwx
 * @Date: 2021-04-08 10:02
 **/
public class MapListHandler extends AbstractListHandler<Map<String,Object>> {


    /**转换器*/
    private final RowProcessor convert;

    public MapListHandler(){
        this(ArrayHandler.ROW_PROCESSOR);
    }

    public MapListHandler(RowProcessor convert){
        this.convert = convert;
    }

    protected Map<String, Object> handlerRow(ResultSet rs) throws SQLException {
        return convert.toMap(rs);
    }
}
