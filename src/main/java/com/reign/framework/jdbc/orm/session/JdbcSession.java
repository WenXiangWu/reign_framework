package com.reign.framework.jdbc.orm.session;

import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.JdbcFactory;
import com.reign.framework.jdbc.orm.extractor.BaseJdbcExtractor;
import com.reign.framework.jdbc.orm.transaction.Transaction;

import java.sql.Connection;

/**
 * @ClassName: JdbcSession
 * @Description: jdbcSession
 * @Author: wuwx
 * @Date: 2021-04-08 18:14
 **/
public interface JdbcSession extends BaseJdbcExtractor {

    /**
     * 获取连接
     * @return
     */
    Connection getConnection();

    /**
     * 获取事务
     * @return
     */
    Transaction getTransaction();

    /**
     * 是否是关闭的session
     * @return
     */
    boolean isClosed();

    /**
     * 关闭session
     */
    void close();

    /**
     * 移除key
     * @param key
     * @param entity
     */
    void evict(String key, JdbcEntity entity);

    /**
     * 移除所有
     */
    void evictAll();

    /**
     * 清理，用于rollback失败后的清理工作
     */
    void clear();

    /**
     * 清理指定表的二级缓存
     * @param entity
     */
    void clear(JdbcEntity entity);

    /**
     * 获取jdbc工厂类
     * @return
     */
    JdbcFactory getJdbcFactory();
}
