package com.reign.framework.jdbc.orm.cache.ehcache;

import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.cache.CacheItem;
import com.reign.framework.jdbc.orm.cache.CacheRegion;
import com.reign.framework.jdbc.orm.cache.LockItem;
import net.sf.ehcache.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: AbstractEhCacheReadWriteRegion
 * @Description: EhCache默认读写策略
 * @Author: wuwx
 * @Date: 2021-04-10 16:11
 **/
public class AbstractEhCacheReadWriteRegion<K, V> implements CacheRegion<K, V> {
    protected net.sf.ehcache.Cache cache;

    protected JdbcEntity jdbcEntity;

    @Override
    public LockItem<V> getLockItem(K key) {
        Element element = cache.get(key);
        if (null == element) return null;
        CacheItem<V> cacheItem = (CacheItem<V>) element.getValue();
        if (cacheItem instanceof LockItem)
            return (LockItem) cacheItem;

        return null;
    }

    @Override
    public void removeLockItem(K key) {

    }

    @Override
    public V get(K key) {
        Element element = cache.get(key);
        if (null == element) return null;
        CacheItem<V> cacheItem = (CacheItem<V>) element.getValue();
        return cacheItem == null ? null : cacheItem.getValue();
    }

    @Override
    public List<V> mget(K... keys) {
        List<V> resultList = new ArrayList<>();
        for (K key : keys) {
            V value = get(key);
            if (null == value) {
                break;
            } else {
                resultList.add(value);
            }
        }
        return resultList;
    }

    @Override
    public void put(K key, V value) {
        cache.put(new Element(key, new EhCacheItem(value)));
    }

    @Override
    public void put(K key, V... values) {
        throw new RuntimeException("not supported");

    }

    @Override
    public void put(K key, CacheItem<V> item) {
        cache.put(new Element(key, item));
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
    }

    @Override
    public void clear() {
        cache.removeAll();
    }

    @Override
    public void destroy() {
        cache.dispose();
    }

    @Override
    public int size() {
        return cache.getSize();
    }

    public void init(String name, JdbcEntity entity) {

        this.cache = EhcacheManager.getInstance().getCache(name);
        this.jdbcEntity = entity;
    }
}
