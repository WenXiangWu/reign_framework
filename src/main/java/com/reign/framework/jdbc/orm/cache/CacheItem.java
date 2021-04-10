package com.reign.framework.jdbc.orm.cache;

import java.io.Serializable;

/**
 * @ClassName: CacheItem
 * @Description: 缓存实体
 * @Author: wuwx
 * @Date: 2021-04-10 13:33
 **/
public interface CacheItem<T> extends Serializable {

    /**
     * 获取实体
     * @return
     */
    T getValue();


    /**
     * 是否可以写
     * @return
     */
    boolean isWritable();
}
