package com.reign.framework.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: GlobalCache
 * @Description: 全局缓存
 * @Author: wuwx
 * @Date: 2021-04-19 17:19
 **/
public class GlobalCache<K, V> {

    private Map<K, V> cacheMap = new ConcurrentHashMap<>();

    public void putToCache(K key, V value) {
        this.cacheMap.put(key, value);
    }

    public V getFromCache(K key) {
        return this.cacheMap.get(key);
    }

    public V removeFromCache(K key) {
        return this.cacheMap.remove(key);
    }


    public void clearAll() {
        this.cacheMap.clear();
    }
}
