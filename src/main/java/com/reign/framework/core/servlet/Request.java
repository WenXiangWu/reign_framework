package com.reign.framework.core.servlet;


import com.reign.framework.common.ServerProtocol;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

/**
 * @ClassName: Request
 * @Description: 请求
 * @Author: wuwx
 * @Date: 2021-04-12 17:46
 **/
public interface Request {

    /**
     * 获取params
     *
     * @return
     */
    Map<String, String[]> getParamterMap();

    /**
     * 获取请求参数
     *
     * @return
     */
    Object[] getRequestArgs();

    /**
     * 设置请求参数
     *
     * @param args
     */
    void setRequestArgs(Object[] args);

    /**
     * 获取某个ParamValue
     *
     * @param key
     * @return
     */
    String[] getParamterValues(String key);

    /**
     * 获取全局缓存的key值
     *
     * @return
     */
    Object getGlobalKeyValue();

    /**
     * 获取一个session，如果不存在则创建
     *
     * @return
     */
    Session getSession();

    /**
     * 获取一个session，是否允许创建
     *
     * @param allowCreate
     * @return
     */
    Session getSession(boolean allowCreate);

    /**
     * 直接创建一个新session
     *
     * @return
     */
    Session getNewSession();

    /**
     * 获取servlet上下文信息
     *
     * @return
     */
    ServletContext getServletContext();

    /**
     * 获取请求命令
     *
     * @return
     */
    String getCommand();

    /**
     * 获取channel
     *
     * @return
     */
    Object getChannel();

    /**
     * 获取请求id
     *
     * @return
     */
    int getRequestId();

    /**
     * 设置sessionid，并且会attach对象到环境中
     *
     * @param sessionId
     */
    void setSessionId(String sessionId);

    /**
     * 获取服务协议
     *
     * @return
     */
    ServerProtocol getProtocol();

    /**
     * 获取http请求头中指定的值
     *
     * @param key
     * @return
     */
    String getHeader(String key);

    /**
     * 获取cookie中指定的值
     *
     * @param key
     * @return
     */
    String getCookieValue(String key);

    /**
     * 获取所有cookies
     *
     * @return
     */
    Collection<?> getCookies();

    /**
     * 是否是http长连接
     *
     * @return
     */
    boolean isHttpLong();

    /**
     * 获取请求地址
     *
     * @return
     */
    InetSocketAddress getRemoteAddress();

    /**
     * 获取输入流
     *
     * @return
     */
    byte[] getContent();

    /**
     * 推送并且关闭channel
     *
     * @param buffer
     */
    void pushAndClose(Object buffer);

    /**
     * 获取ip
     *
     * @return
     */
    String getIp();

    /**
     * 新建一个push通道
     *
     * @return
     */
    Push newPush();

    /**
     * 获取创建时间
     *
     * @return
     */
    long createTime();

}
