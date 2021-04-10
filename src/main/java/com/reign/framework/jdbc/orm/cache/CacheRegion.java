package com.reign.framework.jdbc.orm.cache;

/**
 * @ClassName: CacheRegion
 * @Description: 缓存实体
 * @Author: wuwx
 * @Date: 2021-04-10 14:19
 **/
public interface CacheRegion<K,V> extends Cache<K,V> {

    /**
     * 获取指定key的锁定对象
     * @param key
     * @return
     */
    LockItem<V> getLockItem(K key);

    /**
     * 移除指定key的锁定
     * @param key
     */
    void removeLockItem(K key);

}
