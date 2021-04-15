package com.reign.framework.protocol.udp.handler;

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
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {

    }
}
