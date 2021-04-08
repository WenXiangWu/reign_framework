package com.reign.framework.jdbc.async;

import com.reign.framework.common.concurrent.standardthread.StandardRunnable;
import com.reign.framework.common.concurrent.standardthread.StandardThread;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName: AsyncManager
 * @Description: 异步操作管理器
 * @Author: wuwx
 * @Date: 2021-04-07 18:23
 **/
public class AsyncManager {

    private static final AsyncManager instance = new AsyncManager();

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.async");

    //300ms执行一次
    private static final long INTERVAL = 300;

    private BlockingQueue<AsyncCallback> queue = new LinkedBlockingQueue<>();

    private AsyncManager() {
        StandardThread thread = new StandardThread("async-sql-thread", new AsyncExector(), INTERVAL);
        thread.start();
    }

    public static AsyncManager getInstance() {
        return instance;
    }

    /**
     * 异步操作入队
     *
     * @param callback
     */
    public void addAsyncCallBack(AsyncCallback callback) {
        queue.add(callback);
        callback.doLog(log, 1);

    }

    /**
     * 异步执行线程
     */
    private class AsyncExector extends StandardRunnable {


        public AsyncExector() {
            super(null);
        }

        @Override
        public void execute() {
            AsyncCallback callback = null;
            while ((callback = queue.poll()) != null) {
                try {
                    callback.callback();
                    callback.doLog(AsyncManager.log, 2);
                } catch (RuntimeException e) {
                    callback.doLog(AsyncManager.log, 3);
                    log.error("async db error", AsyncManager.log.getOriginThtowable(e));
                }
            }
        }
    }
}
