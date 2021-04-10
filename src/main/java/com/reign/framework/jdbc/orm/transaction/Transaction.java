package com.reign.framework.jdbc.orm.transaction;

/**
 * @ClassName: Transaction
 * @Description: 事务
 * @Author: wuwx
 * @Date: 2021-04-08 18:16
 **/
public interface Transaction {

    /**
     * 开启事务
     */
    void begin();

    /**
     * 提交事务
     */
    void commit();


    /**
     * 回滚事务
     */
    void rollback();


    /**
     * 是否是活动的事务
     * @return
     */
    boolean isActive();
}
