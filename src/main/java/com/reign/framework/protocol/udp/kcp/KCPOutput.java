package com.reign.framework.protocol.udp.kcp;

/**
 * @ClassName: KCPOutput
 * @Description: KCP监听器
 * @Author: wuwx
 * @Date: 2021-04-15 16:56
 **/
public interface KCPOutput {

    /**
     * 处理写数据
     * @param kcp
     * @param data
     * @param size
     */
    void handleWriteData(KCP kcp,byte[] data,int size);

    /**
     * 处理接收数据
     * @param kcp
     * @param data
     */
    void handleReceiveData(KCP kcp,byte[] data);
}
