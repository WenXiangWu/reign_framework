package com.reign.framework.jdbc.orm.cache;

/**
 * @ClassName: CacheFactory
 * @Description: 缓存工厂
 * @Author: wuwx
 * @Date: 2021-04-07 16:03
 **/
public interface CacheFactory {

    /**
     * 获得cache
     * @return
     */
    Cache getCache();

    /**
     * 获取查询缓存
     * @return
     */
    Cache getQueryCache();
}
