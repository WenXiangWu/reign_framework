package com.reign.framework.protocol.tcp;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.Response;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.ReferenceCountUtil;

import java.io.IOException;

import java.util.Map;

/**
 * @ClassName: TcpResponse
 * @Description: tcp响应封装
 * @Author: wuwx
 * @Date: 2021-04-14 18:45
 **/
public class TcpResponse implements Response {

    private Channel channel;

    private boolean close = false;

    public TcpResponse(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Object getChannel() {
        return channel;
    }

    @Override
    public boolean isWritable() {
        return channel.isWritable();
    }

    @Override
    public Object write(Object obj) throws IOException {
        if (channel.isWritable()){
            ChannelFuture future = channel.write(obj);
            if (close){
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }else {
            ReferenceCountUtil.release(obj);
        }
        return null;
    }

    @Override
    public ServerProtocol getProtocol() {
        return ServerProtocol.TCP;
    }

    @Override
    public void addCookie(Object cookie) {
        throw new UnsupportedOperationException("tcp response cannot offer this operation");

    }

    @Override
    public Map<String, Object> getCookies() {
        throw new UnsupportedOperationException("tcp response cannot offer this operation");

    }

    @Override
    public Map<String, String> getHeaders() {
        throw new UnsupportedOperationException("tcp response cannot offer this operation");

    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException("tcp response cannot offer this operation");

    }

    @Override
    public byte[] getContent() {
        throw new UnsupportedOperationException("tcp response cannot offer this operation");

    }

    @Override
    public void setStatus(Object status) {
        throw new UnsupportedOperationException("tcp response cannot offer this operation");
    }

    @Override
    public Object getStatus() {
       throw new UnsupportedOperationException("tcp response cannot offer this operation");
    }

    @Override
    public void markClose() {
        this.close = true;
    }
}
