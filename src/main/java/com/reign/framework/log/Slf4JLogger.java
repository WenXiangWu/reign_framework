package com.reign.framework.log;


import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * @ClassName: Slf4JLogger
 * @Description: slf4j日志
 * @Author: wuwx
 * @Date: 2021-04-12 14:08
 **/
public class Slf4JLogger extends AbstractLogger {

    /**
     * log
     */
    private final transient Logger log;

    /**
     * 是否重定向errorLog
     */
    private final boolean redirectError;

    public Slf4JLogger(Logger log, boolean redirectError) {
        super(log.getName());
        this.redirectError = redirectError;
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
        if (redirectError){
            return InternalLoggerFactory.errorLog.isFatalEnabled();
        }else {
            return log.isErrorEnabled(FATAL);
        }
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
        return false;
    }

    public void error(String msg) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.error(msg);
        } else {
            log.error(msg);
        }
    }

    public void error(String msg, Throwable t) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.error(msg, t);
        } else {
            log.error(getThrowableTrace(msg, t));
        }
    }

    public void error(String format, Object... arg) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.error(format, arg);
        } else {
            log.error(format, arg);
        }
    }

    public void error(String format, Throwable t, Object... arg) {
        if (redirectError){
            InternalLoggerFactory.errorLog.error(format,arg);
        }else {
            FormattingTuple ft = MessageFormatter.arrayFormat(format,arg);
            String formattedMessage = ft.getMessage();
            log.error(getThrowableTrace(formattedMessage,getOriginThtowable(t)));

        }
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
        if (redirectError) {
            InternalLoggerFactory.errorLog.fatal(msg);
        } else {
            log.error(FATAL, msg);
        }
    }

    public void fatal(String msg, Throwable t) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.fatal(msg, t);
        } else {
            log.error(FATAL, getThrowableTrace(msg, getOriginThtowable(t)));
        }
    }

    public void fatal(String format, Object... arg) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.fatal(format, arg);
        } else {
            log.error(FATAL, format, arg);
        }
    }

    public void fatal(String format, Throwable t, Object... arg) {
        if (redirectError){
            InternalLoggerFactory.errorLog.fatal(format,arg);
        }else {
            FormattingTuple ft = MessageFormatter.arrayFormat(format,arg);
            String formattedMessage = ft.getMessage();
            log.error(FATAL,getThrowableTrace(formattedMessage,getOriginThtowable(t)));

        }
    }

    public void warn(String msg) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.warn(msg);
        } else {
            log.warn(msg);
        }
    }

    public void warn(String msg, Throwable t) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.warn(msg, t);
        } else {
            log.warn(getThrowableTrace(msg, t));
        }
    }

    public void warn(String format, Object... arg) {
        if (redirectError) {
            InternalLoggerFactory.errorLog.warn(format, arg);
        } else {
            log.warn(format, arg);
        }
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
        if (redirectError) {
            return InternalLoggerFactory.errorLog.isWarnEnabled();
        } else {
            return log.isWarnEnabled();
        }
    }
}
