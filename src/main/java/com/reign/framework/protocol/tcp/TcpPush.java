package com.reign.framework.protocol.tcp;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.Push;
import com.reign.framework.core.servlet.Session;
import com.reign.framework.core.util.WrapperUtil;
import io.netty.channel.Channel;
import io.netty.util.ReferenceCountUtil;


/**
 * @ClassName: TcpPush
 * @Description: tcp推送
 * @Author: wuwx
 * @Date: 2021-04-14 18:45
 **/
public class TcpPush implements Push {

    private Channel channel;

    public TcpPush(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void push(Session session, String command, byte[] body) {
        if (isPushable()) {
            channel.writeAndFlush(WrapperUtil.wrapper(command, 0, body));
        }
    }

    @Override
    public void push(String command, byte[] body) {
        if (isPushable()) {
            channel.writeAndFlush(WrapperUtil.wrapper(command, 0, body));
        }

    }

    @Override
    public void push(Session session, Object buffer) {
        if (isPushable()) {
            channel.writeAndFlush(buffer);
        } else {
            ReferenceCountUtil.release(buffer);
        }
    }

    @Override
    public void push(Object buffer) {
        if (isPushable()) {
            channel.writeAndFlush(buffer);
        } else {
            ReferenceCountUtil.release(buffer);
        }
    }

    @Override
    public boolean isPushable() {
        return null != channel && channel.isWritable();
    }

    @Override
    public void clear() {
        if (null != channel) {
            channel.close();
        }
    }

    @Override
    public void discard() {
        return;
    }

    @Override
    public void heartBeat() {
        return;
    }

    @Override
    public ServerProtocol getPushProtocol() {
        return ServerProtocol.TCP;
    }
}
