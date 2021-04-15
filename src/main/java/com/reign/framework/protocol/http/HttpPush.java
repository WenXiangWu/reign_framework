package com.reign.framework.protocol.http;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.Push;
import com.reign.framework.core.servlet.Session;
import com.reign.framework.core.servlet.util.WrapperUtil;
import com.reign.framework.protocol.http.handler.ChunkAction;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;

/**
 * @ClassName: HttpPush
 * @Description: http推送
 * @Author: wuwx
 * @Date: 2021-04-12 16:30
 **/
public class HttpPush implements Push {

    /**
     * 空buffer
     */
    private final byte[] EMPTY_BUFFER = "".getBytes();

    /**
     * chunk处理器
     */
    private ChunkAction<byte[]> chunkHandler;

    /**
     * 用于推送的通道
     */
    private Channel channel;

    public HttpPush(Channel channel, ChunkAction<byte[]> chunkAction) {
        this.channel = channel;
        this.chunkHandler = chunkAction;
    }

    @Override
    public void push(Session session, String command, byte[] body) {
        if (isPushable()) {
            ByteBuf buf = (ByteBuf) WrapperUtil.wrapper(command, 0, body);
            try {
                chunkHandler.writeChunk(buf.array());
            } finally {
                buf.release();
            }
        }
    }

    @Override
    public void push(String command, byte[] body) {
        if (isPushable()) {
            ByteBuf buff = (ByteBuf) WrapperUtil.wrapper(command, 0, body);
            try {
                chunkHandler.writeChunk(buff.array());
            } finally {
                buff.release();
            }
        }
    }

    @Override
    public void push(Session session, Object buffer) {
        try {
            if (isPushable()) {
                chunkHandler.writeChunk(((ByteBuf) buffer).array());
            }
        } finally {
            ReferenceCountUtil.release(buffer);
        }
    }

    @Override
    public void push(Object buffer) {
        try {
            if (isPushable()) {
                chunkHandler.writeChunk(((ByteBuf) buffer).array());
            }
        } finally {
            ReferenceCountUtil.release(buffer);
        }
    }

    @Override
    public boolean isPushable() {
        return null != chunkHandler && channel.isWritable() && null != chunkHandler;
    }

    @Override
    public void clear() {
        if (null != chunkHandler)
            chunkHandler.closeChunked();
        if (null != channel) {
            channel.close();
        }
    }


    @Override
    public void discard() {
        clear();
    }

    @Override
    public void heartBeat() {
        if (isPushable()) {
            chunkHandler.writeChunk(EMPTY_BUFFER);
        }
    }

    @Override
    public ServerProtocol getPushProtocol() {
        return ServerProtocol.HTTP;
    }
}
