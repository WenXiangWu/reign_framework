package com.reign.framework.protocol.udp.client;

import com.reign.framework.core.servlet.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;


/**
 * @ClassName: UDPClientBootstrap
 * @Description: UDP客户端启动器
 * @Author: wuwx
 * @Date: 2021-04-15 16:53
 **/
public class UDPClientBootstrap {

    public ChannelFuture start() throws Exception{
        //载入配置
        XmlConfig config = new XmlConfig("conf.xml");
        ServletConfig servletConfig = config.getServletConfig();
        NettyConfig nettyConfig = config.getNettyConfig();
        ServletContext context = new ServletContextImpl();

        //启动netty
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(bossGroup).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST,true)
                .option(ChannelOption.ALLOCATOR,new UnpooledByteBufAllocator(false));

        ChannelFuture future = bootstrap.connect("127.0.0.1",(Integer)config.getNettyConfig().getInitParam("port"));
        System.out.println("Connect udp :"+nettyConfig.getInitParam("port")+" ok");
        return future;
    }

}
