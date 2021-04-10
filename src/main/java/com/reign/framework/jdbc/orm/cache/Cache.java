package com.reign.framework.jdbc.orm.cache;

import java.util.List;

/**
 * @ClassName: Cache
 * @Description: 缓存标准
 * @Author: wuwx
 * @Date: 2021-04-10 13:37
 **/
public interface Cache<K,V> {

    /**
     * 从缓存读
     * @param key
     * @return
     */
    V get(K key);

    /**
     * 批量获取
     * @param keys
     * @return
     */
    List<V> mget(K... keys);

    /**
     * 放入缓存
     * @param key
     * @param value
     */
    void put(K key,V value);


    /**
     * 放入缓存
     * @param key
     * @param values
     */
    void put(K key,V... values);

    void put(K key,CacheItem<V> item);

    /**
     * 移除缓存
     * @param key
     */
    void remove(K key);


    /**
     * 清空缓存
     */
    void clear();

    /**
     * 彻底清空
     */
    void destroy();


    /**
     * 获得大小
     * @return
     */
    int size();
}
