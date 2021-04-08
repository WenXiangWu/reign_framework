package com.reign.framework.memorydb;

import java.util.concurrent.locks.Lock;

/**
 * @ClassName: AbstractLockableDomain
 * @Description: 抽象实体，提供锁定功能
 * 使用示例：
 * A a = table.read(PK);
 * a.lock();
 * a.setName("abc");
 * table.update(a);
 * a.unlock();
 * @Author: wuwx
 * @Date: 2021-04-02 10:42
 **/
public abstract class AbstractLockableDomain extends AbstractDomain {

    private static final long serialVersionUid = 1L;

    /**
     * 表锁
     */
    public Lock tableLock;

    /**
     * 锁的计数器
     */
    private Thread owner;

    private Object lock = new Object();


    /**
     * 锁
     */
    public void lock() {
        synchronized (lock) {
            while (null != owner && owner != Thread.currentThread()) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {

                }

            }
            owner = Thread.currentThread();
        }
    }


    /**
     * 解锁
     */
    public void unlock() {
        synchronized (lock) {
            if (null == owner || owner != Thread.currentThread()) {
                throw new IllegalStateException("current thread don't hold this lock ,can't unlock");
            }
            owner = null;
            lock.notifyAll();
        }
    }


    /**
     * 做一个标记，当更新到索引时候需要使用到
     */
    @Override
    public void mark() {
        if (!managed || marked) {
            return;
        }
        tableLock.lock();
        super.mark();
    }

    @Override
    public void reset() {
        if (marked) {
            tableLock.unlock();
        }
        super.reset();
    }
}
