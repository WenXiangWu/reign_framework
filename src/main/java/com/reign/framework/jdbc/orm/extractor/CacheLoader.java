package com.reign.framework.jdbc.orm.extractor;

import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.cache.LockItem;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;

import java.util.List;

/**
 * @ClassName: CacheLoader
 * @Description: 缓存抓取器
 * @Author: wuwx
 * @Date: 2021-04-08 18:13
 **/
public class CacheLoader {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.framework.cache");

    /**
     * 将对象放入缓存中
     *
     * @param entity
     * @param key
     * @param value
     * @param type
     */
    public static final void put(JdbcEntity entity, String key, Object value, CacheItemType type) {
        if (!entity.getCacheManger().isUseObjCache()) return;
        CacheEntry entry = new CacheEntry(value, value.getClass(), type, entity);
        entity.getCacheManger().put(key, value);

        CacheEntry indexEntry = new CacheEntry(key, null, CacheItemType.Primitive, entity);
        entity.getCacheManger().buildIndexCache(value, indexEntry);
    }

    /**
     * 从缓存中获取对象
     *
     * @param entity
     * @param key
     * @return
     */
    public static final Object get(JdbcEntity entity, String key) {
        CacheEntry entry = (CacheEntry) entity.getCacheManger().get(key);
        if (null != entity) {
            return entry.getValue();
        }
        return null;
    }


    /**
     * 通过索引从缓存中获取对象
     *
     * @param entity
     * @param key
     * @return
     */
    public static final Object getByIndex(JdbcEntity entity, String key) {
        CacheEntry entry = (CacheEntry) entity.getCacheManger().get(key);
        if (null != entity) {
            String idKey = (String) entry.getValue();
            return get(entity, idKey);
        }
        return null;
    }

    public static final List<Object> mget(JdbcEntity entity, String... keys) {
        return entity.getCacheManger().mget(keys);
    }

    /**
     * 放入对象到查询缓存中
     *
     * @param entity
     * @param key
     * @param values
     */
    public static final void putToQueryCache(JdbcEntity entity, String key, String[] values) {
        entity.getCacheManger().putToQueryCache(key, values);
    }


    /**
     * 从查询缓存中获取对象
     *
     * @param entity
     * @param key
     */
    public static final String[]  getFromQueryCache(JdbcEntity entity, String key) {
        return entity.getCacheManger().getFromQueryCache(key);
    }

    /**
     * 锁定相关对象
     *
     * @param entity
     * @param key
     * @return
     */
    public static final LockItem<Object> lockItem(JdbcEntity entity, String key) {
        return entity.getCacheManger().lockItem(key);
    }


    /**
     * 解除锁定
     *
     * @param entity
     * @param key
     * @param lock
     * @return
     */
    public static final boolean unLockItem(JdbcEntity entity, String key, LockItem<Object> lock) {
        return entity.getCacheManger().unlockItem(key, lock);
    }


    /**
     * 清理二级缓存
     * @param entity
     */
    public static final void clear(JdbcEntity entity) {
        if (log.isDebugEnabled()) {
            log.debug("clear entityCache ,table:{}", entity.getTableName());
        }
        entity.getCacheManger().clear();
    }

    /**
     * 清理查询缓存
     * @param entity
     * @param keys
     */
    public static final void clearQueryCache(JdbcEntity entity,String... keys){
        if (log.isDebugEnabled()) {
            log.debug("clear queryCache ,table:{}", entity.getTableName());
        }
        entity.getCacheManger().clearQueryCache(keys);
    }

}
