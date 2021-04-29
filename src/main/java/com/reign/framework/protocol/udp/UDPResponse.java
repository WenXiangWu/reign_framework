package com.reign.framework.protocol.udp;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.protocol.udp.kcp.KCPNettyWrapper;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Map;

/**
 * @ClassName: UDPResponse
 * @Description: 响应
 * @Author: wuwx
 * @Date: 2021-04-15 16:54
 **/
public class UDPResponse implements Response {

    private KCPNettyWrapper kcp;

    private boolean close;

    public UDPResponse(KCPNettyWrapper kcp) {
        this.kcp = kcp;
    }

    @Override
    public Object getChannel() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public Object write(Object obj) throws IOException {
        kcp.send((ByteBuf) obj);
        return null;
    }

    @Override
    public ServerProtocol getProtocol() {
        return ServerProtocol.UDP;
    }

    @Override
    public void addCookie(Object cookie) {
        throw new UnsupportedOperationException("udp request cannot offer this operation");

    }

    @Override
    public Map<String, Object> getCookies() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");

    }

    @Override
    public Map<String, String> getHeaders() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");

    }

    @Override
    public void addHeader(String name, String value) {
        throw new UnsupportedOperationException("udp request cannot offer this operation");

    }

    @Override
    public byte[] getContent() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");

    }

    @Override
    public void setStatus(Object status) {
        throw new UnsupportedOperationException("udp request cannot offer this operation");

    }

    @Override
    public Object getStatus() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public void markClose() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }
}
