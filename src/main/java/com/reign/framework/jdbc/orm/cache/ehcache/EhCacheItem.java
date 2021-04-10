package com.reign.framework.jdbc.orm.cache.ehcache;

import com.reign.framework.jdbc.orm.cache.CacheItem;

/**
 * @ClassName: EhCacheItem
 * @Description: ehcache缓存实体
 * @Author: wuwx
 * @Date: 2021-04-10 16:11
 **/
public class EhCacheItem<T> implements CacheItem<T> {

    private static final long serialVersionUId = 1L;

    /**
     * 真正缓存实体
     */
    private T value;

    public EhCacheItem(T value) {
        this.value = value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public boolean isWritable() {
        return true;
    }
}
