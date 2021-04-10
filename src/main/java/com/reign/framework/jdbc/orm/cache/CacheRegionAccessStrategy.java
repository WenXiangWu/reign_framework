package com.reign.framework.jdbc.orm.cache;

/**
 * @ClassName: CacheRegionAccessStrategy
 * @Description: cache访问策略
 * @Author: wuwx
 * @Date: 2021-04-10 14:21
 **/
public interface CacheRegionAccessStrategy<K,V> extends Cache<K,V> {

    /**
     * 获取缓存区域
     * @return
     */
    CacheRegion<K,V> getCacheRegion();

    /**
     * 锁定相关对象
     * @param key
     * @return
     */
    LockItem<V> lockItem(K key);


    /**
     * 解除锁定
     * @param key
     * @param lock
     * @return
     */
    boolean unlockItem(K key,LockItem<V> lock);

}
