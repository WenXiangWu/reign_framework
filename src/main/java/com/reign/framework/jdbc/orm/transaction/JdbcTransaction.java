package com.reign.framework.jdbc.orm.transaction;

import com.reign.framework.jdbc.orm.JdbcFactory;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import com.reign.framework.memorydb.SqlExecutor;
import org.springframework.transaction.TransactionSystemException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @ClassName: JdbcTransaction
 * @Description: 事务
 * @Author: wuwx
 * @Date: 2021-04-08 18:15
 **/
public class JdbcTransaction implements Transaction {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.framework.jdbc.transaction");


    private Connection connection;

    private TransactionListener jdbcSession;

    private boolean toggleAutoCommit;

    private boolean begin;

    private boolean rolledBack;

    private boolean committed;

    private boolean commitFailed;

    private JdbcFactory jdbcFactory;


    public JdbcTransaction(TransactionListener jdbcSession, Connection connection, JdbcFactory jdbcFactory) {
        this.connection = connection;
        this.jdbcFactory = jdbcFactory;
        this.jdbcSession = jdbcSession;
    }

    @Override
    public void begin() {
        if (begin) return;
        if (commitFailed) {
            throw new RuntimeException("cannot restart transaction after failed commit");
        }

        try {
            //判断当前connection的事务状态
            toggleAutoCommit = connection.getAutoCommit();
            if (toggleAutoCommit) {
                connection.setAutoCommit(false);
            }

        } catch (SQLException e) {
            throw new TransactionSystemException("JDBC begin failed :", e);
        }
        begin = true;
        committed = false;
        rolledBack = false;
        try {
            //通知事务开始
            jdbcSession.begin(this);
            jdbcFactory.notifyTransactionBegin(this);
        } catch (Throwable t) {
            log.error("after transation begin error", t);
        }

        if (log.isDebugEnabled()) {
            log.debug("start new transaction ["
                    + this.toString() +
                    "] on session ["
                    + jdbcSession.toString()
                    + "] on connection [" +
                    connection.toString()
                    + "]");

        }
    }

    @Override
    public void commit() {
        if (!begin) throw new RuntimeException("Transaction not successfully started");

        //开始事务提交
        jdbcSession.beforeCommit(this, true);
        jdbcFactory.notifyTransactionBegin(this, true);

        try {
            if (log.isDebugEnabled()) {
                log.debug("before commit transaction [" +
                        this.toString() + "]");
            }

            commitAndResetAutoCommit();
            committed = true;
            try {
                //通知事务提交成功
                jdbcSession.commit(this, true);
                jdbcFactory.notifyTransactionCommit(this, true);
            } catch (Throwable t) {
                log.error("after transaction commit error ", t);
            }
            if (log.isDebugEnabled()) {
                log.debug("commmit transaction [" +
                        this.toString() + "" +
                        "] succ");
            }
        } catch (SQLException e) {
            commitFailed = true;
            throw new TransactionSystemException("JDBC commit failed ", e);
        }

    }


    @Override
    public void rollback() {
        if (!begin && !commitFailed) {
            throw new RuntimeException("Transaction not successfully started ");
        }
        //回滚事务
        if (!commitFailed) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("before rollback transaction [" +
                            this.toString() + "]");
                }


                //开始回滚之前的处理
                try {
                    jdbcSession.beforeCommit(this, false);
                    jdbcFactory.notifyTransactionBeforeCommit(this, false);
                } catch (Throwable t) {
                    log.error("before transaction rollback error ", t);
                }

                //回滚事务
                rollbackAndResetAutoCommit();
                rolledBack = true;

                if (log.isDebugEnabled()) {
                    log.debug("rollback transaction [" +
                            this.toString() + "" +
                            "] succ");
                }
            } catch (SQLException e) {
                commitFailed = true;
                throw new TransactionSystemException("JDBC rollback failed ", e);
            }
        }
    }

    /**
     * 回滚事务，并且设置连接为初始状态
     *
     * @throws SQLException
     */
    private void rollbackAndResetAutoCommit() throws SQLException {
        try {
            connection.rollback();
        } finally {
            toggleAutoCommit();
        }
    }

    /**
     * 提交事务，并且设置连接为初始状态
     *
     * @throws SQLException
     */
    private void commitAndResetAutoCommit() throws SQLException {
        try {
            connection.commit();
        } finally {
            toggleAutoCommit();
        }
    }

    /**
     * 还原连接事务状态
     */
    private void toggleAutoCommit() {
        try {
            if (toggleAutoCommit) {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {

        }
    }


    public boolean isActive() {
        return begin && !(rolledBack || commitFailed || committed);
    }

}
