package com.reign.framework.jdbc.orm.cache.ehcache;

import com.reign.framework.jdbc.orm.cache.Cache;
import com.reign.framework.jdbc.orm.cache.CacheFactory;
import com.reign.framework.jdbc.orm.cache.CacheRegionAccessStrategy;

/**
 * @ClassName: EhCacheCacheFactory
 * @Description: ehcache实现的缓存
 * @Author: wuwx
 * @Date: 2021-04-10 15:53
 **/
public class EhCacheFactory implements CacheFactory {


    public EhCacheFactory() {
        EhcacheManager.getInstance();
    }

    @Override
    public CacheRegionAccessStrategy<String, Object> getCache() {
        EhCacheReadWriteRegion<String, Object> region = new EhCacheReadWriteRegion<>();
        return new EhCacheReadWriteRegionAccessStrategy<>(region);
    }

    @Override
    public  CacheRegionAccessStrategy<String,String[]> getQueryCache() {
        EhcacheReadWriteCollectionRegion<String, String[]> region = new EhcacheReadWriteCollectionRegion<>();
        return new EhCacheReadWriteRegionAccessStrategy(region);
    }
}
