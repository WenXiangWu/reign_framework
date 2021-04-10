package com.reign.framework.jdbc.orm.cache.ehcache;

import com.reign.framework.jdbc.orm.cache.CacheStatistics;

/**
 * @ClassName: EhCacheReadWriteRegion
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-10 16:12
 **/
public class EhCacheReadWriteRegion<K,V> extends AbstractEhCacheReadWriteRegion<K,V> {

    /**
     * 二级缓存禁用的情况下，直接返回null
     * @param key
     * @return
     */
    @Override
    public V get(K key) {
        if (!jdbcEntity.getCacheManger().isObjCacheEnable()){
            CacheStatistics.addDisableHits(jdbcEntity.getTableName());
            return null;
        }
        return super.get(key);
    }
}
