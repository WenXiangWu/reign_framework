package com.reign.framework.jdbc.orm.cache.redis;

import com.reign.framework.jdbc.orm.cache.Cache;
import com.reign.framework.jdbc.orm.cache.CacheFactory;
import com.reign.framework.jdbc.orm.cache.CacheRegionAccessStrategy;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;


/**
 * @ClassName: RedisCacheFactory
 * @Description: redis实现的缓存
 * @Author: wuwx
 * @Date: 2021-04-10 15:52
 **/
public class RedisCacheFactory implements CacheFactory {

    private JedisPool pool;

    public RedisCacheFactory(String host, int db) {
        //this.pool = new JedisPool(new Config(), Protocol.DEFAULT_PORT,Protocol.DEFAULT_TIMEOUT,null);
    }

    @Override
    public CacheRegionAccessStrategy<String, Object> getCache() {
        //RedisCacheReadWriteRegionAccessStrategy region = new RedisCacheReadWriteRegion();
        //return new RedisCacheReadWriteRegionAccessStrategy<>(region);
        return null;
    }

    @Override
    public CacheRegionAccessStrategy<String, Object> getQueryCache() {

        return null;
    }

    public JedisPool getPool() {
        return pool;
    }
}
