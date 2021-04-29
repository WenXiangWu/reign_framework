package com.reign.framework.protocol.udp.handler;

import com.reign.framework.protocol.udp.kcp.KCPManager;
import com.reign.framework.protocol.udp.kcp.KCPNettyWrapper;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;


/**
 * @ClassName: NioUDPHandler
 * @Description: UDP处理器
 * @Author: wuwx
 * @Date: 2021-04-15 16:53
 **/
public class NioUDPHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) throws Exception {
        try {
            KCPNettyWrapper kcp = KCPManager.getInstance().getKCP(msg);
            if (null != kcp) {
                kcp.input(msg.content());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
