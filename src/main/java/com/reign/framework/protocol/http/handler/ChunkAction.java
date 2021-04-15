package com.reign.framework.protocol.http.handler;


/**
 * @ClassName: ChunkAction
 * @Description: 处理chunk
 * @Author: wuwx
 * @Date: 2021-04-12 16:29
 **/
public interface ChunkAction<T> {


    /**
     * 写chunk
     * @param result
     */
    void writeChunk(T result);

    /**
     * 关闭
     */
    void closeChunked();

}
