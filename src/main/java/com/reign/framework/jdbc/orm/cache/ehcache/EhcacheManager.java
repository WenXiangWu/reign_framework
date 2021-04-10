package com.reign.framework.jdbc.orm.cache.ehcache;

import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

/**
 * @ClassName: EhcacheManager
 * @Description: 管理器
 * @Author: wuwx
 * @Date: 2021-04-10 16:11
 **/
public class EhcacheManager {

    private static final Logger log = InternalLoggerFactory.getLogger(EhcacheManager.class);

    private static final EhcacheManager instance = new EhcacheManager();


    private static CacheManager manager;

    /**
     * 清理线程每隔10分钟检查一次
     */
    private static final long INTERVAL = 10 * 60 * 1000;


    private void init() {
        manager = new CacheManager(getClass().getClassLoader().getResourceAsStream("ehcache.xml"));
        CacheExpiredThread thread = new CacheExpiredThread();
        thread.start();
    }


    /**
     * 构造函数
     */
    private EhcacheManager() {
        init();
    }

    public static EhcacheManager getInstance() {
        return instance;
    }

    public Cache getCache(String name) {
        Cache cache = manager.getCache(name);
        if (null == cache) {
            if (log.isWarnEnabled()) {
                log.warn(name + "  cache cannot found configuration,using default cache");
            }
            manager.addCache(name);
            cache = manager.getCache(name);
        }
        return cache;
    }


    public void clear() {
        manager.clearAll();
    }

    private class CacheExpiredThread extends Thread {

        public CacheExpiredThread() {
            super("ehcache-expired-thread");
        }

        @Override
        public void run() {
            while (!this.isInterrupted()) {
                try {
                    String[] caches = manager.getCacheNames();
                    for (String cacheName : caches) {
                        Cache cache = manager.getCache(cacheName);
                        if (null != cache) {
                            cache.evictExpiredElements();
                        }
                    }
                } catch (Exception e) {
                    log.error("expired ehcache error ", e);
                }
                try {
                    sleep(INTERVAL);
                } catch (InterruptedException e) {
                    log.error("expired ehcache error", e);
                }

            }
        }
    }
}
