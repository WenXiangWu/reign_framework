package com.reign.framework.jdbc.orm.cache;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @ClassName: AbstractReadWriteCacheRegionAccessStrategy
 * @Description: 缓存访问策略的抽象类;  封装了读写访问策略的基本实现
 * @Author: wuwx
 * @Date: 2021-04-10 14:18
 **/
public abstract class AbstractReadWriteCacheRegionAccessStrategy<K, V> implements CacheRegionAccessStrategy<K, V> {

    /**
     * 缓存存储region
     */
    private CacheRegion<K, V> region;

    /**
     * 锁
     */
    protected Lock lock = new ReentrantLock(false);

    public AbstractReadWriteCacheRegionAccessStrategy(CacheRegion<K, V> region) {
        this.region = region;
    }

    /**
     *批量获取
     * @param keys
     * @return
     */
    public List<V> mget(K... keys){
        return region.mget(keys);
    }

    @Override
    public V get(K key){
        return region.get(key);
    }

    @Override
    public void put(K key,V value){
        try {
            lock.lock();
            LockItem<V> lockItem = region.getLockItem(key);
            if (null!=lockItem){
                if (!lockItem.isWritable()){
                    //被锁定了
                    return;
                }else {
                    region.removeLockItem(key);
                }
            }
            region.put(key,value);
        }finally {
            lock.unlock();
        }
    }



    @Override
    public void put(K key,CacheItem<V> item){
        try {
            lock.lock();
            LockItem<V> lockItem = region.getLockItem(key);
            if (null!=lockItem){
                if (!lockItem.isWritable()){
                    //被锁定了
                    return;
                }else {
                    region.removeLockItem(key);
                }
            }
            region.put(key,item);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void put(K key, V... values) {
        region.put(key,values);
    }

    @Override
    public void remove(K key) {
        try {
            lock.lock();
            LockItem<V> lockItem = region.getLockItem(key);
            if (null!=lockItem){
                if (!lockItem.isWritable()){
                    //被锁定了
                    return;
                }else {
                    region.removeLockItem(key);
                }
            }
            region.remove(key);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        region.clear();
    }

    @Override
    public void destroy() {
        region.destroy();
    }

    @Override
    public int size() {
        return region.size();
    }


    @Override
    public CacheRegion<K, V> getCacheRegion() {
        return region;
    }

    @Override
    public LockItem<V> lockItem(K key) {

        try {
            lock.lock();
            LockItem<V> lockItem = region.getLockItem(key);
            if (null == lockItem){
                lockItem = new LockItem<>();
            }else {
                lockItem.lock();
            }
            region.put(key,lockItem);
            return lockItem;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public boolean unlockItem(K key, LockItem<V> lockItem) {
        try {
            lock.lock();
            LockItem<V> localLockItem = region.getLockItem(key);
            if (null ==localLockItem){
                return false;
            }else if (localLockItem ==lockItem){
                lockItem.unlock();
                return true;
            }
            return false;
        }finally {
            lock.unlock();
        }
    }
}
