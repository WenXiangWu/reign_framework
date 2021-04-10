package com.reign.framework.jdbc.orm.session;

/**
 * @ClassName: JdbcSessionTrigger
 * @Description: JdbcSession触发器
 * @Author: wuwx
 * @Date: 2021-04-08 18:15
 **/
public interface JdbcSessionTrigger {

    /**
     * 触发操作
     */
    void trigger();
    /**触发操作类型 ：插入*/
    int INSERT = 1;
    /**触发操作类型 ：更新*/
    int UPDATE = 2;
    /**触发操作类型 ：删除*/
    int DELETE = 3;
}
