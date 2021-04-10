package com.reign.framework.jdbc.orm.cache.redis;

import com.reign.framework.jdbc.orm.cache.AbstractReadWriteCacheRegionAccessStrategy;
import com.reign.framework.jdbc.orm.cache.CacheRegion;


/**
 * @ClassName: RedisCacheReadWriteRegionAccessStrategy
 * @Description: redis实现的读写缓存策略
 * @Author: wuwx
 * @Date: 2021-04-10 15:54
 **/
public class RedisCacheReadWriteRegionAccessStrategy<V> extends AbstractReadWriteCacheRegionAccessStrategy<String, V> {


    public RedisCacheReadWriteRegionAccessStrategy(CacheRegion region) {
        super(region);
    }


}
