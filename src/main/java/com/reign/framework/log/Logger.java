package com.reign.framework.log;

/**
 * @ClassName: Logger
 * @Description: 框架日志
 * @Author: wuwx
 * @Date: 2021-04-01 16:24
 **/
public interface Logger {

    //获取日志的名称
    String name();

    //是否可以输出Trace级别的日志
    boolean isTraceEnabled();

    boolean isInfoEnabled();

    boolean isErrorEnabled();

    boolean isFatalEnabled();


    //输出trace级别的日志
    void trace(String msg);

    //多参数trace日志
    void trace(String format, Object... arg);

    //输出trace级别日志
    void trace(String msg, Throwable t);

    //是否可以输出Debug级别的日志
    boolean isDebugEnabled();

    void error(String msg);

    void error(String msg, Throwable t);

    void error(String format, Object... arg);

    void error(String format,Throwable t,Object... arg);

    void info(String msg);

    void info(String msg, Throwable t);

    void info(String format, Object... arg);


    void fatal(String msg);

    void fatal(String msg, Throwable t);

    void fatal(String format, Object... arg);

    void fatal(String format,Throwable t,Object... arg);


    void warn(String msg);

    void warn(String msg, Throwable t);

    void warn(String format, Object... arg);

    void debug(String msg);

    void debug(String format, Throwable t);

    void debug(String format, Object... arg);

    /**
     * 输出指定级别的日志
     *
     * @param level
     * @param format
     * @param args
     */
    void log(LogLevel level, String format, Object... args);

    void log(LogLevel level, String msg, Throwable t);

    void log(LogLevel level, String msg);


    //获取错误的原始原因
    Throwable getOriginThtowable(Throwable t);

    boolean isLogEnabled(LogLevel logLevel);

    boolean isWarnEnabled();
}
