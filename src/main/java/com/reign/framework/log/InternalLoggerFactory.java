package com.reign.framework.log;

/**
 * @ClassName: InternalLoggerFactory
 * @Description: 日志工厂
 * @Author: wuwx
 * @Date: 2021-04-01 16:30
 **/
public abstract class InternalLoggerFactory {

    //默认日志工厂
    private static volatile InternalLoggerFactory defaultFactory;

    //默认错误日志类
    static volatile Logger errorLog;

    //异常堆栈行数
    static volatile int LINES = 100;

    static {
        final String name = InternalLoggerFactory.class.getName();
        InternalLoggerFactory f;
//        try {
//            f = new Slf4JLoggerFactory();
//
//        }


    }

    //创建日志
    public static Logger getLogger(String s) {
        return null;
    }


    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz, true);
    }

    private static Logger getLogger(Class<?> clazz, boolean redirectError) {
        return getLogger(clazz.getName(), redirectError);
    }

    public static Logger getLogger(String name, boolean redirectError) {

        return getDefaultFactory().createLogger(name, redirectError);
    }

    public abstract Logger createLogger(String name, boolean redirectError);

    private static InternalLoggerFactory getDefaultFactory() {
        return defaultFactory;
    }

}
