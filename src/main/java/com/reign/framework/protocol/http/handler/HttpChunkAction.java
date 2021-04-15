package com.reign.framework.protocol.http.handler;


import com.reign.framework.core.servlet.util.WrapperUtil;
import com.reign.framework.protocol.util.HttpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.codec.http.HttpRequest;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @ClassName: HttpChunkAction
 * @Description: chunck处理
 * @Author: wuwx
 * @Date: 2021-04-12 16:30
 **/
public class HttpChunkAction<T> implements ChunkAction<byte[]> {


    /**
     * 上下文
     */
    private ChannelHandlerContext ctx;

    /**
     * chunk的http请求
     */
    private HttpRequest httpRequest;

    /**
     * chunked输入流
     */
    private LazyChunkedInput chunkedInput;


    public HttpChunkAction(ChannelHandlerContext ctx, HttpRequest httpRequest) {
        this.ctx = ctx;
        this.httpRequest = httpRequest;
    }

    public void writeChunk(byte[] result) {
        try {
            if (chunkedInput == null) {
                chunkedInput = new LazyChunkedInput();
                HttpUtil.doChunkedResponse(ctx, chunkedInput, httpRequest);
            }
            chunkedInput.writeChunk(result);
            ChunkedWriteHandler handler = (ChunkedWriteHandler) ctx.channel().pipeline().get("chunkedWriter");
            handler.resumeTransfer();
        } catch (Exception e) {
            System.out.println("writeChunk error" + e);
        }
    }

    public void closeChunked() {
        try {
            chunkedInput.close();
            ChunkedWriteHandler handler = (ChunkedWriteHandler) ctx.channel().pipeline().get("chunkedWriter");
            handler.resumeTransfer();
        } catch (Exception e) {
            System.out.println("close chunk error" + e);
        }
    }


    static class LazyChunkedInput implements ChunkedInput<ByteBuf> {

        /**
         * chunk是否关闭标识
         */
        private boolean closed = false;
        /**
         * chunk队列
         */
        private ConcurrentLinkedQueue<ByteBuf> nextChunks = new ConcurrentLinkedQueue<ByteBuf>();

        public boolean isEndOfInput() throws Exception {
            return closed && nextChunks.isEmpty();
        }

        public void close() throws Exception {
            if (!closed) {
                nextChunks.offer((ByteBuf) WrapperUtil.wrapperChunk(WrapperUtil.EMPTY_BYTE));
            }
            closed = true;
        }

        public ByteBuf readChunk(ChannelHandlerContext channelHandlerContext) throws Exception {
            return nextChunks.poll();
        }

        public void writeChunk(byte[] chunk) throws Exception {
            nextChunks.offer((ByteBuf) WrapperUtil.wrapperChunk(chunk));
        }
    }
}
