package com.reign.framework.jdbc.orm.cache;

import com.reign.framework.common.Lang;
import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.JdbcFactory;
import com.reign.framework.jdbc.orm.annotation.NoCache;
import com.reign.framework.jdbc.orm.cache.ehcache.EhCacheFactory;
import com.reign.framework.jdbc.orm.cache.ehcache.EhCacheReadWriteRegionAccessStrategy;
import com.reign.framework.jdbc.orm.cache.redis.RedisCacheFactory;
import com.reign.framework.jdbc.orm.extractor.CacheEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CacheManager
 * @Description: 缓存管理器
 * @Author: wuwx
 * @Date: 2021-04-02 15:23
 **/
public class CacheManager {

    /**
     * 缓存
     */
    private CacheRegionAccessStrategy<String, Object> cache;

    /**
     * 查询缓存
     */
    private CacheRegionAccessStrategy<String, String[]> queryCache;

    /**
     * 缓存配置
     */
    private CacheConfig cacheConfig;

    private JdbcFactory context;

    private JdbcEntity entity;

    /**
     * 查询缓存是否启用
     */
    private ThreadLocal<Boolean> queryCacheEnable;

    /**
     * 二级缓存是否启用
     */
    private ThreadLocal<Boolean> objCacheEnable;

    /**
     * 前缀
     */
    private String prefix;

    /**
     * 查询前缀
     */
    private String queryPrefix;

    /**
     * 是否启用对象缓存
     */
    private boolean useObjCache;

    /**
     * 是否启用查询缓存
     */
    private boolean useQueryCache;


    public static CacheManager build(JdbcEntity entity, JdbcFactory context, CacheFactory factory) {
        CacheManager manager = new CacheManager();
        manager.entity = entity;
        manager.context = context;
        manager.prefix = entity.getTableName() + "::cache::";
        manager.queryPrefix = entity.getTableName() + "::query::";

        if (null == factory) {
            manager.useObjCache = false;
            manager.useQueryCache = false;
            return manager;
        }

        //判断是否启用缓存（优先判断cache，逐步淘汰NoCache）
        com.reign.framework.jdbc.orm.annotation.Cache cache = Lang.getAnnotation(entity.getEntityClass(), com.reign.framework.jdbc.orm.annotation.Cache.class);
        if (null != cache) {
            manager.useObjCache = !cache.disable();
        } else {
            manager.useObjCache = Lang.getAnnotation(entity.getEntityClass(), NoCache.class) == null;
        }

        if (manager.useObjCache) {
            //获得cache实例
            manager.cache = getCache(factory, entity);
            if (null == manager.cache) {
                manager.useObjCache = false;
            } else {
                manager.objCacheEnable = new ThreadLocal<>();
            }
        }

        //使用二级缓存的情况下查询缓存才生效
        if (manager.useObjCache) {
            manager.queryCache = getQueryCache(factory, entity);
            if (manager.queryCache != null) {
                manager.useQueryCache = true;
                manager.queryCacheEnable = new ThreadLocal<>();
            }
        }
        return manager;

    }


    /**
     * 放置索引缓存
     *
     * @param obj
     * @param cacheObj
     */
    public final void buildIndexCache(Object obj, Object cacheObj) {
        if (!useObjCache || null == entity.getIndex()) {
            return;
        }

        if (obj.getClass() != entity.getEntityClass()) {
            return;
        }
        cache.put(entity.getIndex().getKeyValueByObject(obj), cacheObj);

    }


    public String getPrefix() {
        return prefix;
    }

    public String getQueryPrefix() {
        return queryPrefix;
    }

    /**
     * 获得缓存配置文件
     *
     * @return
     */
    public CacheConfig getCacheConfig() {
        if (cacheConfig == null)
            cacheConfig = context.getCacheConfig(entity.getEntityClass().getName());

        return cacheConfig;
    }

    /**
     * 获得查询缓存配置文件
     *
     * @return
     */
    public final CacheConfig getQueryCacheConfig() {
        if (cacheConfig == null) {
            cacheConfig = context.getCacheConfig("queryCache");
        }
        return cacheConfig;
    }

    /**
     * 获得二级缓存
     *
     * @return
     */
    public final CacheRegionAccessStrategy<String, Object> getCache() {
        return cache;
    }

    /**
     * 获得查询缓存
     *
     * @return
     */
    public final CacheRegionAccessStrategy<String, String[]> getQueryCache() {
        return queryCache;
    }

    /**
     * 是否使用对象缓存
     *
     * @return
     */
    public boolean isUseObjCache() {
        return useObjCache;
    }

    /**
     * 是否使用查询缓存
     *
     * @return
     */
    public boolean isUseQueryCache() {
        return useQueryCache;
    }

    /**
     * 重置缓存禁用标志
     */
    public final void resetCacheFlag() {
        if (useObjCache) {
            objCacheEnable.remove();
        }
        if (useQueryCache) {
            queryCacheEnable.remove();
        }
    }

    /**
     * 禁用查询缓存
     */
    public final void disableQueryCache() {
        if (useQueryCache) {
            queryCacheEnable.set(false);
        }
    }

    /**
     * 禁用对象缓存
     */
    public final void disableObjCache() {
        if (useObjCache) {
            objCacheEnable.set(false);
        }
    }

    /**
     * 查询缓存是否启用
     *
     * @return
     */
    public final boolean isQueryCacheEnable() {
        return useQueryCache && (queryCacheEnable.get() == null || queryCacheEnable.get());
    }

    /**
     * 对象缓存是否启用
     *
     * @return
     */
    public final boolean isObjCacheEnable() {
        return useObjCache && (objCacheEnable.get() == null || objCacheEnable.get());
    }

    /**
     * 锁定对象
     *
     * @param key
     * @return
     */
    public final LockItem<Object> lockItem(String key) {
        if (useObjCache) return getCache().lockItem(key);
        return null;
    }

    /**
     * 解锁对象
     *
     * @param key
     * @param lock
     * @return
     */
    public final boolean unlockItem(String key, LockItem<Object> lock) {
        if (useObjCache) {
            return getCache().unlockItem(key, lock);
        }
        return true;
    }

    /**
     * 从二级缓存中查找对象
     *
     * @param key
     * @return
     */
    public final Object get(String key) {
        if (useObjCache) {
            return getCache().get(key);
        }
        return null;
    }

    /**
     * 从缓存中批量获取
     *
     * @param keys
     * @return
     */
    public final List<Object> mget(String[] keys) {
        if (useObjCache) {
            List<Object> entryList = getCache().mget(keys);
            if (null == entryList || entryList.size() == 0) {
                return entryList;
            }
            List<Object> newList = new ArrayList<>(entryList.size());
            for (Object obj : entryList) {
                if (obj instanceof CacheEntry) {
                    CacheEntry entry = (CacheEntry) obj;
                    newList.add(entry.getValue());
                } else {
                    newList.add(obj);
                }
            }
            return newList;
        }
        return null;
    }


    /**
     * 将对象放置到二级缓存
     *
     * @param key
     * @param entry
     */
    public final void put(String key, Object entry) {
        if (useObjCache) getCache().put(key, entry);
    }

    /**
     * 清理二级缓存
     */
    public final void clear() {
        if (useObjCache) getCache().clear();
    }


    /**
     * 通过查询缓存查找
     *
     * @param key
     * @return
     */
    public final String[] getFromQueryCache(String key) {
        if (useQueryCache) return getQueryCache().get(key);
        return null;
    }

    /**
     * 将对象放置到查询缓存
     *
     * @param key
     * @param values
     */
    public final void putToQueryCache(String key, String[] values) {
        if (useQueryCache) getQueryCache().put(key, values);
    }


    public final void clearQueryCache(String[] keys) {
        if (useQueryCache) {
            //清理查询缓存
            if (null == keys) {
                getQueryCache().clear();
                return;
            } else if (keys.length == 0) {
                //没有传值
                getQueryCache().clear();
                return;
            } else {
                for (String key : keys) {
                    if ("all".equalsIgnoreCase(key)) {
                        getQueryCache().clear();
                        break;
                    } else {
                        getQueryCache().remove(key + "*");
                    }
                }

            }

        }
    }


    /**
     * 初始化cache
     *
     * @param factory
     * @param entity
     * @return
     */
    private static CacheRegionAccessStrategy<String, Object> getCache(CacheFactory factory, JdbcEntity entity) {
        if (factory instanceof RedisCacheFactory) {
            RedisCacheFactory cacheFactory = (RedisCacheFactory) factory;
            CacheRegionAccessStrategy<String, Object> strategy = cacheFactory.getCache();

        } else if (factory instanceof EhCacheFactory) {
            EhCacheFactory ehCacheFactory = (EhCacheFactory) factory;
            CacheRegionAccessStrategy<String, Object> strategy = ehCacheFactory.getCache();
            EhCacheReadWriteRegionAccessStrategy<String, Object> ehStrategy = (EhCacheReadWriteRegionAccessStrategy<String, Object>) strategy;
            ehStrategy.init(entity.getEntityClass().getName(), entity);
            return strategy;
        }
        return null;
    }


    private static CacheRegionAccessStrategy<String, String[]> getQueryCache(CacheFactory factory, JdbcEntity entity) {
        if (factory instanceof RedisCacheFactory) {

        } else if (factory instanceof EhCacheFactory) {
            EhCacheFactory cacheFactory = (EhCacheFactory) factory;
            CacheRegionAccessStrategy<String, String[]> strategy = cacheFactory.getQueryCache();
            EhCacheReadWriteRegionAccessStrategy<String, String[]> ehStrategy = (EhCacheReadWriteRegionAccessStrategy<String, String[]>) strategy;
            ehStrategy.init(entity.getEntityClass().getName() + "::com.reign.queryCache", entity);
        }
        return null;
    }

}
