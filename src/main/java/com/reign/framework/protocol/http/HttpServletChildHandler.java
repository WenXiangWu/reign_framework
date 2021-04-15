package com.reign.framework.protocol.http;

import com.reign.framework.core.servlet.NettyConfig;
import com.reign.framework.core.servlet.Servlet;
import com.reign.framework.core.servlet.ServletChildHandler;
import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.protocol.http.handler.HttpDefaultHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.concurrent.ExecutorService;

/**
 * @ClassName: HttpServletChildHandler
 * @Description: 从reactor 处理器
 * @Author: wuwx
 * @Date: 2021-04-12 16:30
 **/
public class HttpServletChildHandler extends ChannelInitializer<SocketChannel> implements ServletChildHandler {


    private NettyConfig config;

    private Servlet servlet;

    private ServletContext sc;

    private ExecutorService executor;

    private boolean useSession;

    private boolean isCluster;

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        HttpDefaultHandler defaultHandler = new HttpDefaultHandler(servlet,sc);

        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("decoder",new HttpRequestDecoder());
        pipeline.addLast("aggregator",new HttpObjectAggregator(Integer.MAX_VALUE));
        pipeline.addLast("encoder",new HttpResponseEncoder());
        pipeline.addLast("chunkedWriter",new ChunkedWriteHandler());
        pipeline.addLast("handler",defaultHandler);
    }

    @Override
    public void init(Servlet servlet, ServletContext context, NettyConfig nettyConfig, ExecutorService executorService, boolean useSession, boolean isCluster) throws Exception {
        this.servlet = servlet;
        this.sc = sc;
        this.config = config;
        this.executor =executorService;
        this.useSession =useSession;
        this.isCluster = isCluster;
    }
}
