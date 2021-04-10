package com.reign.framework.jdbc.orm.transaction;

import com.reign.framework.jdbc.orm.JdbcFactory;
import com.reign.framework.jdbc.orm.session.JdbcSession;
import com.reign.framework.jdbc.orm.session.JdbcSessionHolder;
import com.reign.framework.jdbc.orm.session.JdbcSessionUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.ConnectionHolder;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.JdbcTransactionObjectSupport;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;
import org.springframework.transaction.*;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @ClassName: JdbcTransactionManager
 * @Description: 整合JDBC事务，交给Spring管理
 * @Author: wuwx
 * @Date: 2021-04-08 18:16
 **/
public class JdbcTransactionManager extends AbstractPlatformTransactionManager implements ResourceTransactionManager, InitializingBean {

    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    private JdbcFactory jdbcFactory;

    public JdbcFactory getJdbcFactory() {
        return jdbcFactory;
    }

    public void setJdbcFactory(JdbcFactory jdbcFactory) {
        this.jdbcFactory = jdbcFactory;
    }

    /**
     * 设置数据源
     *
     * @param dataSource
     */
    public void setDataSource(DataSource dataSource) {
        if (dataSource instanceof TransactionAwareDataSourceProxy) {

            this.dataSource = ((TransactionAwareDataSourceProxy) dataSource).getTargetDataSource();
        } else {
            this.dataSource = dataSource;
        }

    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (getJdbcFactory() == null) {
            throw new IllegalArgumentException("Property 'jdbcContext' is required ");
        }
        setDataSource(getJdbcFactory().getDataSource());
    }

    @Override
    public Object getResourceFactory() {
        return jdbcFactory;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        if (logger.isDebugEnabled()) {
            logger.debug("doGetTransaction call");
        }
        JdbcTransactionObject txObject = new JdbcTransactionObject();

        return null;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {
        if (logger.isDebugEnabled()) {
            logger.debug("doBegin call");
        }
        JdbcTransactionObject txObject = (JdbcTransactionObject) transaction;
        //事务对象中有链接了，但是不是Transaction同步的，说明有其他人修改了该Connection的事务状态
        if (txObject.hasConnectionHolder() && !txObject.getConnectionHolder().isSynchronizedWithTransaction()) {
            throw new IllegalTransactionStateException("Pre-bound JDBC Connection found! JpaTransactionManager does not support" +
                    "running within DataSourceTransactionManager if told to manage the DataSource itself." +
                    "It is recommended to use a single JpaTransactionManager for all transactions " +
                    "on a single DataSource ,no matter whether JPA or JDBC access.");
        }
        JdbcSession session = null;
        try {
            if (txObject.getSessionHolder() == null || txObject.getSessionHolder().isSynchronizedWithTransaction()) {
                //当前没有Session，或者当前的SessionHolder是事务同步的，创建一个新的Session
                JdbcSession newSession = getJdbcFactory().openSession();
                txObject.setSession(newSession);
                if (logger.isDebugEnabled()) {
                    logger.debug("Open New Session [" + newSession + " ] for JDBC transaction");
                }
            }
            //为事务做一些准备工作
            session = txObject.getSessionHolder().getSession();
            Connection connection = session.getConnection();
            Integer previousIsolationLevel = DataSourceUtils.prepareConnectionForTransaction(connection, definition);
            txObject.setPreviousIsolationLevel(previousIsolationLevel);

            //开启事务，并且储存起来
            Transaction t = session.getTransaction();
            t.begin();
            txObject.getSessionHolder().setTransaction(t);
            //注册事务超时
            int timeout = determineTimeout(definition);
            if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                txObject.getSessionHolder().setTimeoutInSeconds(timeout);
            }

            //绑定Session的Connection到数据源，使得如果使用原生的DataSource也可以受该事务的管理
            if (getDataSource() != null) {
                Connection conn = session.getConnection();
                ConnectionHolder conHolder = new ConnectionHolder(conn);
                if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
                    conHolder.setTimeoutInSeconds(timeout);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Exposing Wrapper JDBC transaction as JDBC transaction [" + conn + " ]");
                }
                TransactionSynchronizationManager.bindResource(getDataSource(), conHolder);
                txObject.setConnectionHolder(conHolder);
            }

            //如果是新的SessionHolder，注册到线程变量
            if (txObject.isNewSessionHolder()) {
                TransactionSynchronizationManager.bindResource(getResourceFactory(), txObject.getSessionHolder());
            }
            txObject.getSessionHolder().setSynchronizedWithTransaction(true);

        } catch (Exception ex) {
            if (txObject.isNewSession()) {
                try {
                    if (session.getTransaction().isActive()) {
                        session.getTransaction().rollback();
                    }
                } catch (Throwable ex2) {
                    logger.error("Could not rollback Session after failed transaction begin", ex);
                } finally {
                    JdbcSessionUtil.closeSession(session);
                }
            }
            throw new CannotCreateTransactionException("Could not open Session for transaction", ex);
        }

    }

    @Override
    protected void doCommit(DefaultTransactionStatus status) throws TransactionException {

        if (logger.isDebugEnabled()) {
            logger.debug("doCommit call");
        }
        JdbcTransactionObject txObject = (JdbcTransactionObject) status.getTransaction();
        if (status.isDebug()) {
            logger.debug("Committing Wrapper JDBC transaction on Session [" +
                    txObject.getSessionHolder().getSession()
                    + "]");
        }

        try {
            txObject.getSessionHolder().getTransaction().commit();
        } catch (TransactionSystemException ex) {
            throw ex;
        }

    }

    /**
     * 挂起当前事务，就是将当前事务的SessionHolder和ConnectionHolder保存起来
     *
     * @param transaction
     * @return
     * @throws TransactionException
     */
    @Override
    protected Object doSuspend(Object transaction) throws TransactionException {
        if (logger.isDebugEnabled()) {
            logger.debug("doSuspend call");
        }
        JdbcTransactionObject txObject = (JdbcTransactionObject) transaction;
        txObject.setSessionHolder(null);
        txObject.setConnectionHolder(null);

        JdbcSessionHolder sessionHolder = (JdbcSessionHolder) TransactionSynchronizationManager.unbindResource(getResourceFactory());
        ConnectionHolder connectionHolder = null;
        if (getDataSource() != null) {
            connectionHolder = (ConnectionHolder) TransactionSynchronizationManager.unbindResource(getDataSource());
        }
        return new SuspenderResourcesHolder(sessionHolder, connectionHolder);
    }


    /**
     * 恢复挂起事务，就是将保存的SessionHolder，ConnectionHolder重新绑定
     *
     * @param transaction
     * @param suspendedResources
     * @throws TransactionException
     */
    @Override
    protected void doResume(Object transaction, Object suspendedResources) throws TransactionException {
        if (logger.isDebugEnabled()) {
            logger.debug("doResume call");
        }
        SuspenderResourcesHolder resourcesHolder = (SuspenderResourcesHolder) suspendedResources;
        if (TransactionSynchronizationManager.hasResource(getResourceFactory())) {
            TransactionSynchronizationManager.unbindResource(getResourceFactory());
        }

        TransactionSynchronizationManager.bindResource(getResourceFactory(), resourcesHolder.getSessionHolder());
        if (getDataSource() != null) {

            TransactionSynchronizationManager.bindResource(getDataSource(), resourcesHolder.getConnectionHolder());
        }
    }


    @Override
    protected void doRollback(DefaultTransactionStatus status) throws TransactionException {
        if (logger.isDebugEnabled()) {
            logger.debug("doRollback call");
        }
        JdbcTransactionObject txObject = (JdbcTransactionObject) status.getTransaction();
        if (status.isDebug()) {
            logger.debug("Rolling back Wrapper JDBC transaction on Session [" + txObject.getSessionHolder().getSession() + " ]");
        }
        try {
            txObject.getSessionHolder().getTransaction().rollback();
        } catch (TransactionSystemException ex) {
            throw ex;
        } finally {
            if (!txObject.isNewSession()) {
                txObject.getSessionHolder().getSession().clear();
            }
        }

    }

    @Override
    protected void doSetRollbackOnly(DefaultTransactionStatus status) throws TransactionException {
        if (logger.isDebugEnabled()) {
            logger.debug("doSetRollbackOnly call");
        }
        JdbcTransactionObject txObject = (JdbcTransactionObject) status.getTransaction();
        txObject.setRollbackOnly();
        if (status.isDebug()) {
            logger.debug("Setting Wrapper JDBC transaction as JDBC Session [" + txObject.getSessionHolder().getSession() + " ] rollback-only");
        }
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        if (logger.isDebugEnabled()) {
            logger.debug("doCleanupAfterCompletion call");
        }
        JdbcTransactionObject txObject = (JdbcTransactionObject) transaction;

        //如果是新创建的SessionHolder，则取消注册
        if (txObject.isNewSessionHolder()) {
            TransactionSynchronizationManager.unbindResource(getResourceFactory());
        }

        //取消ConnectionHolder的注册
        if (getDataSource() != null) {
            TransactionSynchronizationManager.unbindResource(getDataSource());
        }

        //释放连接
        JdbcSession session = txObject.getSessionHolder().getSession();
        if (txObject.hasConnectionHolder()) {
            Connection conn = session.getConnection();
            DataSourceUtils.resetConnectionAfterTransaction(conn, txObject.getPreviousIsolationLevel());
        }

        if (txObject.isNewSession()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Closing Wrapper JDBC   Session [" + session + " ] after transaction");
            }
            JdbcSessionUtil.closeSession(session);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Not closing Wrapper JDBC   Session [" + session + " ] after transaction");
            }
            //当嵌套多个Transaction时 TODO
        }
        txObject.getSessionHolder().clear();

    }

    /**
     * 用于JdbcTransactionManager
     */
    private class JdbcTransactionObject extends JdbcTransactionObjectSupport {

        /**
         * JdbcSessionHolder  Session保持者
         */
        private JdbcSessionHolder sessionHolder;

        /**
         * 是否是新的SessionHolder
         */
        private boolean newSessionHolder;

        /**
         * 是否是新的Session
         */
        private boolean newSession;

        /**
         * 设置Session
         *
         * @param session
         */
        public void setSession(JdbcSession session) {
            this.sessionHolder = new JdbcSessionHolder(session);
            this.newSessionHolder = true;
            this.newSession = true;
        }

        /**
         * 设置sessionHolder，说明是已经存在的SessionHolder
         *
         * @param sessionHolder
         */
        public void setSessionHolder(JdbcSessionHolder sessionHolder) {
            this.sessionHolder = sessionHolder;
            this.newSessionHolder = false;
            this.newSession = false;
        }


        /**
         * 设置Session，但是Session已经是存在的
         *
         * @param session
         */
        public void setExistSession(JdbcSession session) {
            this.sessionHolder = new JdbcSessionHolder(session);
            this.newSessionHolder = true;
            this.newSession = false;
        }

        public JdbcSessionHolder getSessionHolder() {
            return sessionHolder;
        }

        public boolean isNewSessionHolder() {
            return newSessionHolder;
        }

        public boolean isNewSession() {
            return newSession;
        }

        /**
         * 判断当前是否存在Spring管理的事务
         *
         * @return
         */
        public boolean hasTransactionManager() {
            return (this.sessionHolder != null && this.sessionHolder.getTransaction() != null);
        }

        /**
         * 标志rollback
         */
        public void setRollbackOnly() {
            this.sessionHolder.setRollbackOnly();
            if (hasConnectionHolder()) {
                getConnectionHolder().setRollbackOnly();
            }
        }

        /**
         * 判断当前是否rollback
         *
         * @return
         */
        @Override
        public boolean isRollbackOnly() {
            return this.sessionHolder.isRollbackOnly() ||
                    (hasConnectionHolder() && getConnectionHolder().isRollbackOnly());
        }
    }

    /**
     * 用于暂停事务的ResourcesHolder
     */
    private static class SuspenderResourcesHolder {


        private final JdbcSessionHolder sessionHolder;

        private final ConnectionHolder connectionHolder;

        public SuspenderResourcesHolder(JdbcSessionHolder sessionHolder, ConnectionHolder connectionHolder) {
            this.sessionHolder = sessionHolder;
            this.connectionHolder = connectionHolder;
        }

        public JdbcSessionHolder getSessionHolder() {
            return sessionHolder;
        }

        public ConnectionHolder getConnectionHolder() {
            return connectionHolder;
        }
    }

}
