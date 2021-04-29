package com.reign.framework.protocol.udp;

import com.reign.framework.core.servlet.NettyConfig;
import com.reign.framework.core.servlet.Servlet;
import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.protocol.tcp.handler.ITcpHandler;
import com.reign.framework.protocol.udp.handler.NioUDPHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

/**
 * @ClassName: UDPServletChildHandler
 * @Description: 子reactor
 * @Author: wuwx
 * @Date: 2021-04-15 16:55
 **/
public class UDPServletChildHandler extends ChannelInitializer<NioDatagramChannel> {

    private final Servlet servlet;

    private final ServletContext sc;

    private final Class<?> tcpHandlerClass;

    private final EventLoopGroup eventGroup;

    public UDPServletChildHandler(Servlet servlet, ServletContext sc, NettyConfig config, EventLoopGroup eventGroup) throws Exception{
        this.servlet = servlet;
        this.sc = sc;
        this.eventGroup = eventGroup;

        //查找ChannelHandler
        String tcpHandlerName = (String)config.getInitParam("udpHandler");

        //TODO
        this.tcpHandlerClass = null;

    }

    @Override
    protected void initChannel(NioDatagramChannel channel) throws Exception {
        ITcpHandler handler = (ITcpHandler)tcpHandlerClass.newInstance();
        handler.setServletContext(sc);
        handler.setServlet(servlet);

        channel.pipeline().addLast("handler1",new NioUDPHandler());
        channel.pipeline().addLast("handler2",handler);

    }
}
