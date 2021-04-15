package com.reign.framework.core.servlet;

/**
 * @ClassName: Wrapper
 * @Description: 包装器
 * @Author: wuwx
 * @Date: 2021-04-12 16:53
 **/
public interface Wrapper {

    /**是否开启压缩*/
    boolean compress();

    /**
     * 打包bytes
     * @param bytes
     * @return
     */
    Object wrapper(byte[] bytes);


    /**
     * 打包
     * @param command 请求命令
     * @param requestId 请求id
     * @param body
     * @return
     */
    Object wrapper(String command,int requestId,byte[] body);

    /**
     * 打包，自己控制压缩
     * @param requestId
     * @param body
     * @param compress
     * @return
     */
    Object wrapper(int requestId,byte[] body,boolean compress);


    /**
     * 打包
     * @param requestId
     * @param body
     * @return
     */
    Object wrapper(int requestId,byte[] body);


    /**
     * 打包，自己控制是否压缩
     * @param command
     * @param requestId
     * @param body
     * @param compress
     * @return
     */
    Object wrapper(String command,int requestId,byte[] body,boolean compress);


    /**
     * 给websocket打包
     * @param command
     * @param requestId
     * @param body
     * @return
     */
    Object wrapperWebSocket(String command,int requestId,byte[] body);


    /**
     * 打包包体
     * @return
     */
    byte[] wrapperBody(byte[] bytes);

    /**
     * 打包包体
     * @param bytes
     * @param compress
     * @return
     */
    byte[] wrapperBody(byte[] bytes,boolean compress);


    /**
     * 获取contentType， 供Http使用
     * @return
     */
    String getContentType();

    /**
     * 打包，为chunk协议打包
     * @param chunk
     * @return
     */
    Object wrapperChunk(byte[] chunk);

    /**
     * 引用计数+1
     * @param msg
     */
    void retain(Object msg);

    /**
     * 引用计数-1
     * @param msg
     */
    void release(Object msg);

    /**
     * 重新wrapper
     * @param buffer
     * @return
     */
    Object newWrapper(Object buffer);

    /**
     * 获取buff分配器
     * @return
     */
    Object getByteBufAllocator();
}
