package com.reign.framework.jdbc.orm.cache.ehcache;

import com.reign.framework.jdbc.orm.cache.CacheStatistics;

/**
 * @ClassName: EhcacheReadWriteCollectionRegion
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-10 16:12
 **/
public class EhcacheReadWriteCollectionRegion<K, V> extends AbstractEhCacheReadWriteRegion<K, V> {

    @Override
    public V get(K key) {
        if (!jdbcEntity.getCacheManger().isQueryCacheEnable()) {
            CacheStatistics.addDisableHits(jdbcEntity.getTableName());
            return null;
        }
        return super.get(key);
    }
}
