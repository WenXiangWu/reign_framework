package com.reign.framework.log;

import sun.rmi.runtime.Log;

/**
 * @ClassName: DayReportLogger
 * @Description: 日常日志
 * @Author: wuwx
 * @Date: 2021-04-12 14:48
 **/
public class DayReportLogger extends DefaultLogger {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.dayreport");

    public DayReportLogger() {
        super(log);
    }
}
