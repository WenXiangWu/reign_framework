package com.reign.framework.jdbc.orm.cache;


/**
 * @ClassName: LockItem
 * @Description: 锁定
 * @Author: wuwx
 * @Date: 2021-04-10 13:33
 **/
public class LockItem<T> implements CacheItem<T> {

    private static final long serialVersionUID = 1L;

    //计数器
    private int lockCount;

    public LockItem() {
        this.lockCount = 1;
    }

    @Override
    public T getValue() {
        return null;
    }

    @Override
    public boolean isWritable() {
        return lockCount <= 0;
    }

    public void lock() {
        lockCount++;
    }

    public void unlock() {
        --lockCount;
    }
}
