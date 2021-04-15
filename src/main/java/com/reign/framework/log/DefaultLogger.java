package com.reign.framework.log;

import sun.rmi.runtime.Log;

/**
 * @ClassName: DefaultLogger
 * @Description: 默认日志
 * @Author: wuwx
 * @Date: 2021-04-12 13:57
 **/
public class DefaultLogger extends AbstractLogger {

    /**
     * 内部日志
     */
    private final Logger log;

    public DefaultLogger(Logger log) {
        super(log.name());
        this.log = log;
    }


    public boolean isTraceEnabled() {
        return log.isTraceEnabled();
    }

    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return log.isFatalEnabled();
    }

    public void trace(String msg) {
        log.trace(msg);
    }

    public void trace(String format, Object... arg) {
        log.trace(format, arg);
    }

    public void trace(String msg, Throwable t) {
        log.trace(msg, t);
    }

    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    public void error(String msg) {
        log.error(msg);
    }

    public void error(String format, Throwable t) {
        log.error(format, t);
    }

    public void error(String format, Object... arg) {
        log.error(format, arg);
    }

    public void error(String format, Throwable t, Object... arg) {
        log.error(format, t, arg);
    }

    public void info(String msg) {
        log.info(msg);
    }

    public void info(String format, Throwable t) {
        log.info(format, t);
    }

    public void info(String format, Object... arg) {
        log.info(format, arg);
    }

    public void fatal(String msg) {
        log.fatal(msg);
    }

    public void fatal(String format, Throwable t) {
        log.fatal(format, t);
    }

    public void fatal(String format, Object... arg) {
        log.fatal(format, arg);
    }

    public void fatal(String format, Throwable t, Object... arg) {
        log.fatal(format, t, arg);
    }

    public void warn(String msg) {
        log.warn(msg);
    }

    public void warn(String format, Throwable t) {
        log.warn(format, t);
    }

    public void warn(String format, Object... arg) {
        log.warn(format, arg);
    }

    public void debug(String msg) {
        log.debug(msg);
    }

    public void debug(String format, Throwable t) {
        log.debug(format, t);
    }

    public void debug(String format, Object... arg) {
        log.debug(format, arg);
    }

    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }
}
