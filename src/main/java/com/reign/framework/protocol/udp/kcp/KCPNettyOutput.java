package com.reign.framework.protocol.udp.kcp;

import io.netty.buffer.ByteBuf;

/**
 * @ClassName: KCPNettyOutput
 * @Description: KCP监听器
 * @Author: wuwx
 * @Date: 2021-04-15 16:56
 **/
public interface KCPNettyOutput {


    /**
     * 处理写数据
     * @param kcp
     * @param buf
     */
    void  handleWriteData(KCPNettyWrapper kcp, ByteBuf buf);


    /**
     * 处理接收数据
     * @param kcp
     * @param buf
     */
    void  handleReceiveData(KCPNettyWrapper kcp, ByteBuf buf);

    /**
     * 处理异常
     * @param kcp
     * @param t
     */
    void  handleException(KCPNettyWrapper kcp,Throwable t);


}
