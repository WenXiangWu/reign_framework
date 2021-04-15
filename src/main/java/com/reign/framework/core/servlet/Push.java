package com.reign.framework.core.servlet;

import com.reign.framework.common.ServerProtocol;

/**
 * @ClassName: Push
 * @Description: 用于推的对象
 * @Author: wuwx
 * @Date: 2021-04-12 16:42
 **/
public interface Push {


    /**
     * 推送数据
     * @param session
     * @param command
     * @param body
     */
    void push(Session session,String command,byte[] body);

    /**
     * 推送数据
     * @param command
     * @param body
     */
    void push(String command,byte[] body);


    /**
     * 推送的最终的数据结果，不会做压缩处理
     * @param buffer
     */
    void push(Session session,Object buffer);

    /**
     * 推送的最终的数据结果，不会再做压缩处理
     * @param buffer
     */
    void push(Object buffer);

    /**
     * 是否可以推送
     * @return
     */
    boolean isPushable();

    /**
     * 清理工作
     */
    void clear();

    /**
     * 丢弃该推送通道
     */
    void discard();

    /**
     * 心跳
     */
    void heartBeat();


    /**
     * 获取推送协议
     * @return
     */
    ServerProtocol getPushProtocol();
}

