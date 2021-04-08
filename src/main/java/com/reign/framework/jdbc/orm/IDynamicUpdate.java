package com.reign.framework.jdbc.orm;


import com.reign.framework.common.util.Tuple;
import com.reign.framework.jdbc.Param;

import java.util.List;

/**
 * @ClassName: IDynamicUpdate
 * @Description: 能够动态生成更新语句的接口类，不用实现
 * @Author: wuwx
 * @Date: 2021-04-02 14:50
 **/
public interface IDynamicUpdate {
    /**
     * 获取动态更新SQL
     *
     * @param tableName
     * @param oldUpdateObj
     * @return
     */
    Tuple<String, List<Param>> dynamicUpdateSQL(String tableName, JdbcModel oldUpdateObj);
}
