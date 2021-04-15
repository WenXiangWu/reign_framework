package com.reign.framework.core.servlet;


import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @ClassName: SessionManager
 * @Description: session管理器  TODO 暂不处理
 * @Author: wuwx
 * @Date: 2021-04-15 10:05
 **/
public class SessionManager {

    private static final Logger log = InternalLoggerFactory.getLogger(SessionManager.class);

    private static final SessionManager instance = new SessionManager();

    /**重复sessionId个数*/
    protected int duplicates = 0;

    protected final List<SessionListener> sessionListenerList = new ArrayList<>();

    protected final List<SessionAttributeListener> sessionAttributeListeners = new ArrayList<>();

    /***
     * session管理器
     */
    protected final ConcurrentMap<String,Session> sessions = new ConcurrentHashMap<>();

    protected volatile MessageDigest digest;

    protected Random random;

    protected String entropy;

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;

    private boolean checkThreadStarted = false;

    private ServletConfig sc;


    /**默认为玩家保存的历史消息数目*/
    volatile static int MAX_HISTORY_MSG_LEN = 100;

    public SessionManager() {
    }

    public static SessionManager getInstance() {
        return instance;
    }

    public Session getSession(String sessionId, boolean allowCreate) {
        return null;
    }

    public void access(String sessionId) {
    }
}
