package com.reign.framework.core.servlet.util;

import com.reign.framework.core.servlet.NettyConfig;

import java.net.URLDecoder;

/**
 * @ClassName: Utils
 * @Description: 工具类集合
 * @Author: wuwx
 * @Date: 2021-04-15 15:36
 **/
public class Utils {

    private static final String jvmRoute;

    static {
        jvmRoute = _getJvmRoute();
    }

    public static String getJvmRoute() {
        return jvmRoute;
    }

    /**
     * 获取每个线程允许的累计请求数量
     *
     * @param config
     * @return
     */
    public static int getCountPerChannel(NettyConfig config) {
        Integer value = (Integer) config.getInitParam("countPerChannel");
        return null == value ? 10 : value;
    }

    /**
     * URLEncode,忽略encode发生的异常
     *
     * @param str
     * @param encode
     * @return
     * @throws UnsupportedOperationException
     */
    public static String decode(String str, String encode) throws UnsupportedOperationException {
        try {
            return URLDecoder.decode(str, encode);
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Throwable t) {
            //解析失败
            return str;
        }
    }

    /**
     * 获取Executor日志级别
     *
     * @param config
     * @return
     */
    public static String getExecutroLogLevel(NettyConfig config) {
        String value = (String) config.getInitParam("logLevel");
        return null == value ? "WARN" : value;
    }

    private static String _getJvmRoute() {
        return System.getProperty("jvmRoute");
    }
}
