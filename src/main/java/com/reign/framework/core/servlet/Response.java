package com.reign.framework.core.servlet;



import com.reign.framework.common.ServerProtocol;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName: Response
 * @Description: 每次请求都会有一个Response对象，用于传递数据
 * @Author: wuwx
 * @Date: 2021-04-12 17:46
 **/
public interface Response {

    /**
     * 获取channel
     *
     * @return
     */
    Object getChannel();

    /**
     * 是否可写
     *
     * @return
     */
    boolean isWritable();

    /**
     * 写数据
     *
     * @param obj
     * @return
     * @throws IOException
     */
    Object write(Object obj) throws IOException;

    /**
     * 获取服务器协议
     *
     * @return
     */
    ServerProtocol getProtocol();

    /**
     * 增加cookie内容
     *
     * @param cookie
     */
    void addCookie(Object cookie);

    /**
     * 获取cookies
     *
     * @return
     */
    Map<String, Object> getCookies();

    /**
     * 获取http请求头
     *
     * @return
     */
    Map<String, String> getHeaders();

    /**
     * 添加http头
     *
     * @param name
     * @param value
     */
    void addHeader(String name, String value);

    /**
     * 获取返回内容
     *
     * @return
     */
    byte[] getContent();

    /**
     * 设置响应状态；
     */
    void setStatus(Object status);

    /**
     * 获取响应状态码
     *
     * @return
     */
    Object getStatus();

    /**
     * 标记为关闭
     */
    void markClose();
}
