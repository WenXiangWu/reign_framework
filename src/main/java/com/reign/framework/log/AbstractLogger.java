package com.reign.framework.log;

/**
 * @ClassName: AbstractLogger
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-10 18:06
 **/
public class AbstractLogger implements Logger {
    @Override
    public String name() {
        return null;
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {

    }

    @Override
    public void trace(String format, Object... arg) {

    }

    @Override
    public void trace(String msg, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void error(String format, Throwable t) {

    }

    @Override
    public void error(String format, Object... arg) {

    }

    @Override
    public void info(String msg) {

    }

    @Override
    public void info(String format, Throwable t) {

    }

    @Override
    public void info(String format, Object... arg) {

    }

    @Override
    public void fatal(String msg) {

    }

    @Override
    public void fatal(String format, Throwable t) {

    }

    @Override
    public void fatal(String format, Object... arg) {

    }

    @Override
    public void warn(String msg) {

    }

    @Override
    public void warn(String format, Throwable t) {

    }

    @Override
    public void warn(String format, Object... arg) {

    }

    @Override
    public void debug(String msg) {

    }

    @Override
    public void debug(String format, Throwable t) {

    }

    @Override
    public void debug(String format, Object... arg) {

    }

    @Override
    public void log(LogLevel level, String format, Object... args) {

    }

    @Override
    public void log(LogLevel level, String format, Throwable t) {

    }

    @Override
    public void log(LogLevel level, String msg) {

    }

    @Override
    public Throwable getOriginThtowable(Throwable t) {
        return null;
    }

    @Override
    public boolean isLogEnabled(LogLevel logLevel) {
        return false;
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }
}
