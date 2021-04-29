package com.reign.framework.protocol.udp;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.Push;
import com.reign.framework.core.servlet.Session;
import com.reign.framework.core.util.WrapperUtil;
import com.reign.framework.protocol.udp.kcp.KCPNettyWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;

/**
 * @ClassName: UDPPush
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:53
 **/
public class UDPPush implements Push {

    private KCPNettyWrapper kcp;

    public UDPPush(KCPNettyWrapper kcp) {
        this.kcp = kcp;
    }

    @Override
    public void push(Session session, String command, byte[] body) {
        push(command, body);
    }

    @Override
    public void push(String command, byte[] body) {
        if (isPushable()) {
            kcp.send((ByteBuf) WrapperUtil.wrapper(command, 0, body));
        }
    }

    @Override
    public void push(Session session, Object buffer) {
        push(buffer);
    }

    @Override
    public void push(Object buffer) {
        if (isPushable()) {
            kcp.send((ByteBuf) buffer);
        } else {
            ReferenceCountUtil.release(buffer);
        }
    }

    @Override
    public boolean isPushable() {
        return !kcp.isClosed();
    }

    @Override
    public void clear() {
        kcp.close();
    }

    @Override
    public void discard() {
        kcp.close();
    }

    @Override
    public void heartBeat() {
        return;
    }

    @Override
    public ServerProtocol getPushProtocol() {
        return ServerProtocol.UDP;
    }
}
