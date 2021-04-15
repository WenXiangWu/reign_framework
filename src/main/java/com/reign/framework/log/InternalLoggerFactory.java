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
        try {
            f = new Slf4JLoggerFactory();
            f.createLogger(name).debug("Using SLF4J as the default logging framework");
            defaultFactory = f;
        } catch (Throwable t) {
            throw new RuntimeException("SLF4J not found ,logger init error", t);
        }

        //设置默认日志工厂类
        defaultFactory = f;
        //设置错误日志
        setErrorLogger("com.reign.error");


    }

    /**
     * 设置指定的错误日志
     *
     * @param name
     */
    public static void setErrorLogger(String name) {
        errorLog = getDefaultFactory().createErrorLogger(name);
    }

    public static InternalLoggerFactory getDefaultFactory() {
        return defaultFactory;
    }

    /**
     * 根据类名创建一个指定的logger
     *
     * @param name
     * @return
     */
    public static Logger getLogger(String name) {
        return getLogger(name, true);
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

    protected Logger createLogger(String name) {
        return createLogger(name, true);
    }

    protected abstract Logger createLogger(String name, boolean redirectError);

    protected abstract Logger createErrorLogger(String name);


}
