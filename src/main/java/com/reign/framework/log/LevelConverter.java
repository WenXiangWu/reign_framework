package com.reign.framework.log;


import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * @ClassName: LevelConverter
 * @Description: 日志级别转换器
 * @Author: wuwx
 * @Date: 2021-04-12 14:03
 **/
public class LevelConverter extends ClassicConverter {
    public String convert(ILoggingEvent iLoggingEvent) {
        if (iLoggingEvent.getMarker() != null) {
            return iLoggingEvent.getMarker().getName();
        }
        return iLoggingEvent.getLevel().levelStr;
    }
}
