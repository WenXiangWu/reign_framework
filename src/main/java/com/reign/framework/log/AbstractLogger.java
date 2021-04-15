package com.reign.framework.log;

import com.reign.framework.common.util.SymbolConstants;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

/**
 * @ClassName: AbstractLogger
 * @Description: 日志实现
 * @Author: wuwx
 * @Date: 2021-04-10 18:06
 **/
public abstract class AbstractLogger implements Logger {

    /**
     * 日志名称
     */
    private final String name;

    /**
     * 异常关键串
     */
    protected static final String EXCEPTION_STR = "com.reign";

    protected static Marker FATAL = MarkerFactory.getMarker("FATAL");


    public AbstractLogger(String name) {
        this.name = name;
    }


    /**
     * 获取异常trace第num条;默认获取100条
     *
     * @param msg
     * @param t
     * @return
     */
    protected String getThrowableTrace(String msg, Throwable t) {
        int num = InternalLoggerFactory.LINES;
        StackTraceElement[] stacks = t.getStackTrace();
        StringBuilder builder = new StringBuilder(256);
        builder.append(t.toString());
        builder.append("#");
        if (null != msg) {
            builder.append(msg.replace("#", "@_@"));
        }
        int index = 1;
        boolean count = false;
        for (StackTraceElement element : stacks) {
            String value = element.toString();
            builder.append(SymbolConstants.NEW_LINE).append(SymbolConstants.TAB).append(value);
            if (!count && value.indexOf(EXCEPTION_STR) != -1) {
                //如果出现了标志行
                count = true;
            }
            if (count) {
                //计数
                index++;
                if (index > num) {
                    //超过指定行
                    break;
                }
            }
        }
        return builder.toString();
    }


    public Throwable getOriginThtowable(Throwable t) {
        if (t instanceof InvocationTargetException) {
            InvocationTargetException e = (InvocationTargetException) t;
            return getOriginThtowable(e.getTargetException());
        } else if (t instanceof UndeclaredThrowableException) {
            UndeclaredThrowableException e = (UndeclaredThrowableException) t;
            return getOriginThtowable(e.getUndeclaredThrowable());
        } else if (t.getClass() == RuntimeException.class) {
            RuntimeException e = (RuntimeException) t;
            if (null != e.getCause() && (e.getCause() instanceof InvocationTargetException
                    || e.getCause() instanceof UndeclaredThrowableException)) {
                return getOriginThtowable(e.getCause());
            }
        }

        return t;
    }

    public boolean isLogEnabled(LogLevel logLevel) {
        switch (logLevel) {
            case INFO:
                return isInfoEnabled();
            case WARN:
                return isWarnEnabled();
            case ERROR:
                return isErrorEnabled();
            case DEBUG:
                return isDebugEnabled();
            case FATAL:
                return isFatalEnabled();
            case TRACE:
                return isTraceEnabled();
            default:
                return false;
        }
    }

    public void log(LogLevel level, String msg) {
        switch (level) {
            case FATAL:
                fatal(msg);
            case DEBUG:
                debug(msg);
            case ERROR:
                error(msg);
            case WARN:
                warn(msg);
            case INFO:
                info(msg);
            case TRACE:
                trace(msg);
            default:
                break;
        }
    }

    public void log(LogLevel level, String format, Object... args) {
        switch (level) {
            case FATAL:
                fatal(format, args);
            case DEBUG:
                debug(format, args);
            case ERROR:
                error(format, args);
            case WARN:
                warn(format, args);
            case INFO:
                info(format, args);
            case TRACE:
                trace(format, args);
            default:
                break;
        }

    }

    public void log(LogLevel level, String format, Throwable t) {
        switch (level) {
            case FATAL:
                fatal(format, t);
            case DEBUG:
                debug(format, t);
            case ERROR:
                error(format, t);
            case WARN:
                warn(format, t);
            case INFO:
                info(format, t);
            case TRACE:
                trace(format, t);
            default:
                break;
        }
    }

    public String name() {
        return name;
    }
}
