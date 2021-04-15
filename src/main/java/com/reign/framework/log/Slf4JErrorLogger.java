package com.reign.framework.log;

import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.FormattingTuple;

/**
 * @ClassName: Slf4JErrorLogger
 * @Description: slf4j错误日志
 * @Author: wuwx
 * @Date: 2021-04-12 14:35
 **/
public class Slf4JErrorLogger extends AbstractLogger {

    private final transient Logger logger;

    public Slf4JErrorLogger(Logger log) {
        super(log.getName());
        this.logger = log;
    }

    public boolean isTraceEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    public boolean isFatalEnabled() {
        return logger.isErrorEnabled(FATAL);
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }


    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public void trace(String msg) {
        logger.trace(msg);
    }

    public void trace(String format, Object... arg) {
        logger.trace(format,arg);
    }

    public void trace(String msg, Throwable t) {
        logger.trace(getThrowableTrace(msg,getOriginThtowable(t)));
    }


    public void error(String msg) {
        logger.error(msg);
    }

    public void error(String msg, Throwable t) {
        logger.error(getThrowableTrace(msg, getOriginThtowable(t)));
    }

    public void error(String format, Object... arg) {
        logger.error(format, arg);
    }

    public void error(String format, Throwable t, Object... arg) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, arg);
        String formattedMsg = ft.getMessage();
        logger.error(getThrowableTrace(formattedMsg, getOriginThtowable(t)));
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void info(String msg, Throwable t) {
        logger.info(getThrowableTrace(msg, getOriginThtowable(t)));
    }

    public void info(String format, Object... arg) {
        logger.info(format, arg);
    }

    public void fatal(String msg) {
        logger.error(FATAL, msg);
    }

    public void fatal(String msg, Throwable t) {
        logger.error(FATAL, getThrowableTrace(msg, getOriginThtowable(t)));
    }

    public void fatal(String format, Object... arg) {
        logger.error(FATAL, format, arg);
    }

    public void fatal(String format, Throwable t, Object... arg) {
        FormattingTuple ft = MessageFormatter.arrayFormat(format, arg);
        String formattedMsg = ft.getMessage();
        logger.error(FATAL,getThrowableTrace(formattedMsg, getOriginThtowable(t)));
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void warn(String msg, Throwable t) {
        logger.warn(getThrowableTrace(msg,getOriginThtowable(t)));
    }

    public void warn(String format, Object... arg) {
        logger.warn(format,arg);
    }

    public void debug(String msg) {
        logger.debug(msg);
    }

    public void debug(String msg, Throwable t) {
        logger.debug(getThrowableTrace(msg, getOriginThtowable(t)));
    }

    public void debug(String format, Object... arg) {
        logger.debug(format, arg);
    }


}
