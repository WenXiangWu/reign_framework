package com.reign.framework.jdbc.orm.cache.ehcache;

import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.cache.AbstractReadWriteCacheRegionAccessStrategy;
import com.reign.framework.jdbc.orm.cache.CacheRegion;

/**
 * @ClassName: EhCacheReadWriteRegionAccessStrategy
 * @Description: ehcache读写缓存
 * @Author: wuwx
 * @Date: 2021-04-10 16:12
 **/
public class EhCacheReadWriteRegionAccessStrategy<K, V> extends AbstractReadWriteCacheRegionAccessStrategy<K, V> {

    public EhCacheReadWriteRegionAccessStrategy(CacheRegion<K, V> region) {
        super(region);
    }

    /**
     * 缓存存储region
     */
    private AbstractEhCacheReadWriteRegion<K, V> region;

    public EhCacheReadWriteRegionAccessStrategy(CacheRegion<K, V> region, AbstractEhCacheReadWriteRegion<K, V> region1) {
        super(region);
        this.region = region1;
    }

    public void init(String name, JdbcEntity entity) {
        region.init(name, entity);
    }
}
