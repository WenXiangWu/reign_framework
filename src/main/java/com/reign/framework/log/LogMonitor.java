package com.reign.framework.log;

import com.reign.framework.common.concurrent.standardthread.StandardRunnable;
import com.reign.framework.common.concurrent.standardthread.StandardThread;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName: LogMonitor
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-08 10:04
 **/
public class LogMonitor extends StandardRunnable {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.monitor");

    /**
     * 日志类别
     */
    public static final int MONITOR_TYPE_THRESHOLD = 1;

    public static final int MONITOR_TYPE_METRIC = 2;

    /**
     * 日志监控key
     */
    public static final String MKEY_JDBCSQL = "jdbcsql";
    /**
     * 日志监控key  simpleTimer
     */
    public static final String MKEY_SIMPLETIMER = "simpletimer";
    /**
     * 日志监控 dbTimer
     */
    public static final String MKEY_DBTIMER = "dbtimer";
    /**
     * 日志监控key quartz
     */
    public static final String MKEY_QUARTZ = "quartz";
    /**
     * 日志监控key userThread
     */
    public static final String MKEY_USERTHREAD = "userthread";


    private static final LogMonitor instance = new LogMonitor();

    private ConcurrentHashMap<String, MonitorConfig> configMap = new ConcurrentHashMap<String, MonitorConfig>();


    private ConcurrentHashMap<String, MonitorEntry> entryMap = new ConcurrentHashMap<String, MonitorEntry>();

    private final StandardThread monitorThread;

    //是否启用
    private volatile boolean enableFlag = false;

    public LogMonitor() {
        super(null);
        //每分钟监控一次
        monitorThread = new StandardThread("LogMonitorThread", this, 60 * 1000);

    }

    public static LogMonitor getInstance() {
        return instance;
    }

    public void startMonitor() {
        monitorThread.startExecutor();
        enableFlag = true;
    }

    public void stopMonitor() {
        monitorThread.stopThread();
        enableFlag = false;
        configMap.clear();
        entryMap.clear();
    }


    public void config(String configKey, int threshold, LogLevel level, long interval) {
        configMap.put(configKey, new MonitorConfig(configKey, threshold, level, interval));
    }

    public void disableConfig(String configKey) {
        for (Map.Entry<String, MonitorConfig> entry : configMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(configKey)) {
                entry.getValue().disable = false;
            }
        }

    }


    public void enableConfig(String configKey) {
        for (Map.Entry<String, MonitorConfig> entry : configMap.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(configKey)) {
                entry.getValue().disable = true;
            }
        }

    }

    public void execute() {
        for (Map.Entry<String, MonitorEntry> entryEntry : entryMap.entrySet()) {
            MonitorEntry monitorEntry = entryEntry.getValue();
            //一分钟
            monitorEntry.monitor(log, configMap.get(monitorEntry.configKey), 60 * 1000);
        }

    }

    /**
     * 增加记录
     *
     * @param configKey
     * @param tag
     * @param key
     * @param value
     */
    public void addRecord(String configKey, String tag, String key, int value) {
        if (!enableFlag) {
            return;
        }
        MonitorConfig config = configMap.get(configKey);
        if (null == config || config.disable || !log.isLogEnabled(config.logLevel)) {
            return;
        }

        MonitorEntry entry = entryMap.get(key);
        if (entry == null) {
            entry = new MonitorEntry();
            entry.configKey = configKey;
            entry.tag = tag;
            entry.key = key;
            entry.type = MONITOR_TYPE_THRESHOLD;
            entry.addValue(value);
            entryMap.put(key, entry);
        } else {
            entry.addValue(value);
        }

    }

    /**
     * 移除key
     *
     * @param key
     */
    void removeMonitorEntry(String key) {
        entryMap.remove(key);
    }

    /**
     * 记录统计值
     *
     * @param configKey
     * @param tag
     * @param key
     * @param value
     */
    public void addMetric(String configKey, String tag, String key, int value) {
        if (!enableFlag) {
            return;
        }
        MonitorConfig config = configMap.get(configKey);
        if (null == config || config.disable || !log.isLogEnabled(config.logLevel)) {
            return;
        }

        MonitorMetricEntry metricEntry = (MonitorMetricEntry) entryMap.get(key);
        if (metricEntry == null) {
            metricEntry = new MonitorMetricEntry();
            metricEntry.tag = tag;
            metricEntry.key = key;
            metricEntry.configKey = configKey;
            metricEntry.type = MONITOR_TYPE_METRIC;
            metricEntry.addValue(value);
            entryMap.put(key, metricEntry);
        } else {
            metricEntry.addValue(value);
        }

    }

    /**
     * 监控配置
     */
    public static class MonitorConfig {
        public boolean disable;

        public String tag;

        public int threshold;
        public LogLevel logLevel;
        public long interval;

        public MonitorConfig(String tag, int threshold, LogLevel logLevel, long interval) {
            super();
            this.tag = tag;
            this.threshold = threshold;
            this.logLevel = logLevel;
            this.interval = interval;
            this.disable = false;
        }


        /**
         * 提升log级别
         *
         * @return
         */
        public LogLevel promoteLogLevel() {
            switch (logLevel) {
                case TRACE:
                case DEBUG:
                    return LogLevel.INFO;
                case INFO:
                    return LogLevel.WARN;
                case WARN:
                    return LogLevel.ERROR;
                case ERROR:
                    return LogLevel.FATAL;
                default:
                    return logLevel;


            }
        }
    }
}
