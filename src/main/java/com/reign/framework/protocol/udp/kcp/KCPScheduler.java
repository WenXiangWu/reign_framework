package com.reign.framework.protocol.udp.kcp;

import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName: KCPScheduler
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:56
 **/
public class KCPScheduler extends Thread {
    private static final Logger log = InternalLoggerFactory.getLogger(KCPScheduler.class);

    private static final KCPScheduler instance = new KCPScheduler();

    //帧间隔
    private static final int interval = 2;

    //房间列表
    private CopyOnWriteArrayList<KCPNettyWrapper> kcpList = new CopyOnWriteArrayList<>();

    public static KCPScheduler getInstance() {
        return instance;
    }

    private KCPScheduler() {
        super("kcp-scheduler");
        this.start();
    }

    /**
     * 进行帧循环
     *
     * @param ts
     * @param dt
     */
    private void loop(long ts, long dt) {
        for (KCPNettyWrapper kcp : kcpList) {
            try {
                //执行update
                kcp.update(ts);
            } catch (Throwable t) {
                log.error("run loop error,conv:{}", t, kcp.getConv());
            }
        }
    }


    /**
     * 添加到执行器
     *
     * @param kcp
     */
    public void schedule(KCPNettyWrapper kcp) {
        kcpList.add(kcp);
    }

    /**
     * 从执行器中移除
     *
     * @param kcp
     */
    public void unschedule(KCPNettyWrapper kcp) {
        kcpList.remove(kcp);
    }

    @Override
    public void run() {
        //帧间隔
        long dt =0;
        //当前时间
        long curr = 0l;
        //当前帧开始运行时间
        long start = currentTime();
        while (true){
            //当前时间
            curr = currentTime();
            //帧间隔
            dt = curr-start;
            //循环开始
            start = curr;
            //loop处理
            loop(curr,dt);

            //计算休眠时间
            long execTime = (currentTime()-start);
            if (execTime<interval){
                try{
                    sleep(interval - execTime);
                }catch (InterruptedException e){
                    // ignore
                }
            }
        }


    }

    /**
     * 获取当前时间
     * @return
     */
    public static final long currentTime() {
        return (System.nanoTime() / 1000 * 1000) & 0x7fffffff;
    }
}
