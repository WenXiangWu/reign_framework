package com.reign.framework.common.util;


/**
 * @ClassName: StopWatch
 * @Description: 时间监控
 * @Author: wuwx
 * @Date: 2021-04-29 15:40
 **/
public class StopWatch {

    private long start;

    private long end;

    public StopWatch() {
    }

    public static StopWatch begin() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        return stopWatch;
    }

    public static StopWatch run(Runnable runnable) {
        StopWatch stopWatch = begin();
        runnable.run();
        stopWatch.stop();
        return stopWatch;
    }

    public void start() {
        this.start = System.currentTimeMillis();
    }

    public void stop() {
        this.end = System.currentTimeMillis();
    }

    public long getElapsedTime() {
        return this.end - this.start;
    }
}
