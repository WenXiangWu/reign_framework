package com.reign.framework.jdbc.orm.session;

import com.reign.framework.jdbc.orm.JdbcFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @ClassName: JdbcSessionUtil
 * @Description: session工具类
 * @Author: wuwx
 * @Date: 2021-04-08 18:15
 **/
public final class JdbcSessionUtil {


    /**
     * 是否存在事务中的Session
     *
     * @param factory
     * @return
     */
    public static boolean hasTransactionalSession(JdbcFactory factory) {
        if (factory == null) return false;
        JdbcSessionHolder sessionHolder = (JdbcSessionHolder) TransactionSynchronizationManager.getResource(factory);
        return sessionHolder != null && !sessionHolder.isEmpty();
    }

    /**
     * 是否是处于事务中的Session
     *
     * @param session
     * @param jdbcFactory
     * @return
     */
    public static boolean isSessionTransactional(JdbcSession session, JdbcFactory jdbcFactory) {
        if (jdbcFactory == null) return false;
        JdbcSessionHolder sessionHolder = (JdbcSessionHolder) TransactionSynchronizationManager.getResource(jdbcFactory);
        return sessionHolder != null && sessionHolder.containsSession(session);
    }


    /**
     * 获取Session
     *
     * @param jdbcFactory
     * @param allowCreate
     * @return
     */
    public static JdbcSession getSession(JdbcFactory jdbcFactory, boolean allowCreate) {
        return doGetSession(jdbcFactory, allowCreate);
    }

    /**
     * 获取Session
     *
     * @param jdbcFactory
     * @param allowCreate
     * @return
     */
    private static JdbcSession doGetSession(JdbcFactory jdbcFactory, boolean allowCreate) {
        JdbcSessionHolder sessionHolder = (JdbcSessionHolder) TransactionSynchronizationManager.getResource(jdbcFactory);
        JdbcSession session = null;

        if (sessionHolder != null && !sessionHolder.isEmpty()) {
            session = sessionHolder.getValidatedSession();
        }
        if (session != null) {
            return session;
        }

        if (allowCreate) {
            session = jdbcFactory.openSession();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                JdbcSessionHolder holderToUse = sessionHolder;
                if (holderToUse == null) {
                    holderToUse = new JdbcSessionHolder(session);
                } else {
                    holderToUse.setSession(session);
                }

                if (holderToUse != sessionHolder) {
                    TransactionSynchronizationManager.bindResource(jdbcFactory, holderToUse);
                }
            }
        }
        return session;
    }

    /**
     * 关闭Session
     * @param session
     */
    public static void closeSession(JdbcSession session){
        session.close();
    }


}
