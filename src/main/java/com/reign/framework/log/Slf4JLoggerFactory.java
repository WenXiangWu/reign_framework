package com.reign.framework.log;

/**
 * @ClassName: Slf4JLoggerFactory
 * @Description: slf4j日志工厂
 * @Author: wuwx
 * @Date: 2021-04-12 14:27
 **/
public class Slf4JLoggerFactory extends InternalLoggerFactory {

    public Logger createLogger(String name, boolean redirectError) {
        return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger(name),redirectError);
    }

    protected Logger createErrorLogger(String name) {
        return new Slf4JErrorLogger(org.slf4j.LoggerFactory.getLogger(name));
    }

    public static void main(String[] args) {
        InternalLoggerFactory.setErrorLogger("com.reign.error");
        Logger logger = InternalLoggerFactory.getLogger("test");
        DayReportLogger dayLog = new DayReportLogger();
        logger.info("你好{}{}{}",1,2,"dddd");
        dayLog.info("你好{}{}{}",1,2,"哈哈哈");
        dayLog.info("你好1111{}{}{}",1,2,"测试下");

    }

}
