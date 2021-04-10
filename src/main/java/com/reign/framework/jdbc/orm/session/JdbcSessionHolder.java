package com.reign.framework.jdbc.orm.session;

import com.reign.framework.jdbc.orm.transaction.Transaction;
import org.springframework.transaction.support.ResourceHolderSupport;

/**
 * @ClassName: JdbcSessionHolder
 * @Description: Session存储器
 * @Author: wuwx
 * @Date: 2021-04-08 18:14
 **/
public class JdbcSessionHolder extends ResourceHolderSupport {

    /**
     * jdbcSession,不支持 JTA事务
     */
    private JdbcSession jdbcSession;

    /**
     * 事务
     */
    private Transaction transaction;

    public JdbcSessionHolder(JdbcSession jdbcSession) {
        this.jdbcSession = jdbcSession;
    }

    /**
     * 获取绑定的jdbcSession
     *
     * @return
     */
    public JdbcSession getSession() {
        return jdbcSession;
    }

    /**
     * 获取合法的Session，会判断Session是否关闭
     *
     * @return
     */
    public JdbcSession getValidatedSession() {
        if (jdbcSession != null && jdbcSession.isClosed()) {
            jdbcSession = null;
        }
        return jdbcSession;
    }

    /**
     * 设置Session
     *
     * @param session
     */
    public void setSession(JdbcSession session) {
        this.jdbcSession = session;
    }

    /**
     * 判断是否包含当前Session
     *
     * @param session
     * @return
     */
    public boolean containsSession(JdbcSession session) {
        return this.jdbcSession == session;
    }

    /**
     * 判断当前列表是否为空
     *
     * @return
     */
    public boolean isEmpty() {
        return jdbcSession == null;
    }

    /**
     * 设置事务
     *
     * @param transaction
     */
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Override
    public void clear() {
        super.clear();
        this.transaction = null;
    }
}
