package com.reign.framework.log;


/**
 * @ClassName: MonitorEntry
 * @Description: 监控项
 * @Author: wuwx
 * @Date: 2021-04-08 10:17
 **/
public class MonitorEntry {

    public String configKey;

    public String tag;

    public int type;

    public String key;

    public int value;

    protected int logTimes;

    protected long cd;

    public void addValue(int value) {
        this.value = value;
    }

    public void monitor(Logger log, LogMonitor.MonitorConfig config, long dt) {
        cd += dt;
        if (cd < config.interval) return;

        cd = 0;
        if (value >= config.threshold) {
            if (logTimes > 10) {
                log.log(config.promoteLogLevel(), "{}#{}#{}", tag, key, value);
            } else {
                log.log(config.logLevel, "{}#{}#{}", tag, key, value);
            }
            logTimes++;
        } else {
            logTimes = 0;
        }

    }
}
