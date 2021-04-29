package com.reign.framework.protocol.udp;

import com.reign.framework.core.servlet.*;
import com.reign.framework.core.util.WrapperUtil;
import com.reign.framework.protocol.servlet.NettyWrapper;
import com.reign.framework.protocol.udp.kcp.KCPManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;

/**
 * @ClassName: UDPServletBootstrap
 * @Description: UDP启动器
 * @Author: wuwx
 * @Date: 2021-04-15 16:55
 **/
public class UDPServletBootstrap {

    public void start() throws Exception {
        //载入配置文件
        XmlConfig config = new XmlConfig("conf.xml");
        ServletConfig servletConfig = config.getServletConfig();
        NettyConfig nettyConfig = config.getNettyConfig();
        ServletContext context = new ServletContextImpl();

        Servlet servlet = servletConfig.getServletClass().newInstance();
        servlet.init(servletConfig, context);

        //设置为pooled线程池
        System.setProperty("io.netty.allocator.type", "pooled");

        //设置bytebuf
        WrapperUtil.setWrapper(new NettyWrapper(UnpooledByteBufAllocator.DEFAULT, false));

        //启动netty
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(bossGroup).channel(NioDatagramChannel.class).option(ChannelOption.SO_BROADCAST, true)
                .option(ChannelOption.ALLOCATOR, new UnpooledByteBufAllocator(false))
                .handler(new UDPServletChildHandler(servlet, context, nettyConfig, workerGroup));
        ChannelFuture future = bootstrap.bind(new InetSocketAddress((Integer) nettyConfig.getInitParam("port"))).syncUninterruptibly();

        KCPManager.getInstance().init((NioDatagramChannel) future.channel());
        System.out.println("output tcp " + nettyConfig.getInitParam("port") + " ok");
    }
}
