package com.reign.framework.core.servlet;

/**
 * @ClassName: Session
 * @Description: session
 * @Author: wuwx
 * @Date: 2021-04-15 10:04
 **/
public interface Session {

    String getId();

    Object getAttribute(String key);

    void setAttribute(String key, Object value);

    boolean removeAttribute(String key);

    /**
     * 使所有属性失效
     */
    void invalidate();

    /**
     * 彻底销毁
     */
    void destroy();

    /**
     * 标记为丢失
     */
    void markDiscard();

    /**
     * 访问session对象
     */
    void access();

    /**
     * 设置连接是否有效
     *
     * @param isValid
     */
    void setValid(boolean isValid);

    /**
     * 连接是否有效
     *
     * @return
     */
    boolean isValid();

    /**
     * 是否活跃
     *
     * @return
     */
    boolean isActive();

    /**
     * 是否已过期
     *
     * @return
     */
    boolean isExpire();

    /**
     * 是否已失效
     *
     * @return
     */
    boolean isInvalidate();

    /**
     * 是否为空session
     *
     * @return
     */
    boolean isEmpty();

    /**
     * 重新激活
     */
    void reActive();

    /**
     * 过期连接
     */
    void expire();

    /**
     * 设置推送通道
     *
     * @param push
     */
    void setPush(Push push);

    /**
     * 设置UDP推送通道
     *
     * @param udpPush
     */
    void setUDPPush(Push udpPush);

    /**
     * 获得推送通道
     */
    Push getPush();

    /**
     * 获得UDP
     * @return
     */
    Push getUDPPush();

    /**
     * 推送数据
     * @param command
     * @param body
     */
    void push(String command,byte[] body);


    /**
     * 推送数据
     * @param buffer
     */
    void push(Object buffer);


    /**
     * 推送数据,尝试使用UDP
     * @param command
     * @param body
     * @param tryUdp
     */
    void push(String command,byte[] body,boolean tryUdp);

    /**
     * 推送数据，推送数据的最终结果，不会再做压缩处理
     * @param buffer
     * @param tryUdp
     */
    void push(Object buffer,boolean tryUdp);
}
