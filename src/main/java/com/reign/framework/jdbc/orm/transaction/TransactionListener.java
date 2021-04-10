package com.reign.framework.jdbc.orm.transaction;

/**
 * @ClassName: TransactionListener
 * @Description: 事务监听器
 * @Author: wuwx
 * @Date: 2021-04-08 18:16
 **/
public interface TransactionListener {

    /**
     * 事务开启
     * @param transaction
     */
    void begin(Transaction transaction);

    /**
     * 事务准备提交
     * @param transaction
     * @param succ
     */
    void beforeCommit(Transaction transaction,boolean succ);

    /**
     * 事务提交
     * @param transaction
     * @param succ commit成功为true，反之为false
     */
    void commit(Transaction transaction,boolean succ);

}
