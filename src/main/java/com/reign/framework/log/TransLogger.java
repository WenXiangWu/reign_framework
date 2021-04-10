package com.reign.framework.log;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: TransLogger
 * @Description: 事务日志，会在事务成功提交之后将log内容写到文件，否则不写入
 * @Author: wuwx
 * @Date: 2021-04-10 17:22
 **/
public class TransLogger {

    //内部log
    private final Logger log;

    //线程log
    private final ThreadLocal<List<String>> threadLog;

    //是否存在事务
    private volatile boolean hasTrans;

    //是否需要写入文件
    private volatile boolean needFlush;

    public TransLogger(Logger log) {
        this.log = log;
        this.threadLog = new ThreadLocal<List<String>>() {
            @Override
            protected List<String> initialValue() {
                return new ArrayList<>(5);
            }
        };
        TransLoggerManager.getInstance().addTransLogger(this);
    }

    /**
     * 打印日志
     *
     * @param msg
     */
    public void info(String msg) {
        if (!hasTrans) {
            log.info(msg);
        } else {
            threadLog.get().add(msg);
            needFlush = true;
        }
    }

    /**
     * 启动trans
     */
    public void startTrans() {
        hasTrans = true;
    }

    /**
     * 提交事务
     *
     * @param succ
     */
    public void commitTrans(boolean succ) {
        if (!hasTrans) return;
        hasTrans = false;
        if (!needFlush) return;
        List<String> list = threadLog.get();
        if (succ) {
            for (String msg : list) {
                log.info(msg);
            }
        }
        threadLog.remove();
        needFlush = false;
    }
}
