package com.reign.framework.log;

/**
 * @ClassName: MonitorMetricEntry
 * @Description: 监控
 * @Author: wuwx
 * @Date: 2021-04-08 10:57
 **/
public class MonitorMetricEntry extends MonitorEntry {

    @Override
    public void addValue(int value) {
        this.value += value;
    }

    @Override
    public void monitor(Logger log, LogMonitor.MonitorConfig config, long dt) {
        cd += dt;
        if (cd < config.interval) {
            return;
        }
        cd = 0;
        log.log(config.logLevel,"{}#{}#{}",tag,key,value);
        //重置
        this.value = 0;
        //移除自身
        LogMonitor.getInstance().removeMonitorEntry(key);
    }
}
