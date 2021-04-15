package com.reign.framework.protocol.tcp;

import com.reign.framework.core.servlet.NettyConfig;
import com.reign.framework.core.servlet.Servlet;
import com.reign.framework.core.servlet.ServletChildHandler;
import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.protocol.tcp.coder.MessageDecoder;
import com.reign.framework.protocol.tcp.coder.MessageEncoder;
import com.reign.framework.protocol.tcp.handler.ITcpHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import redis.clients.jedis.Pipeline;

import java.util.concurrent.ExecutorService;

/**
 * @ClassName: TcpServletChildHandler
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-14 18:45
 **/
public class TcpServletChildHandler extends ChannelInitializer<SocketChannel> implements ServletChildHandler {


    private Class<?> tcpHandlerClass;

    private Servlet servlet;

    private ServletContext servletContext;

    private ExecutorService executor;

    private boolean useSession;

    private boolean isCluster;

    @Override
    public void init(Servlet servlet, ServletContext context, NettyConfig config, ExecutorService executorService, boolean useSession, boolean isCluster) throws Exception {
        //查找channelHandler
        String tcpHandlerClassName = (String) config.getInitParam("tcpHandler");
        Class<?> clazz = getClass().getClassLoader().loadClass(tcpHandlerClassName);
        if (null == clazz) {
            throw new ClassNotFoundException(tcpHandlerClassName);
        } else if (!ITcpHandler.class.isAssignableFrom(clazz)) {
            throw new ClassCastException(clazz.getName() + "  cannot cast to " + ITcpHandler.class.getName());
        }
        this.tcpHandlerClass = clazz;
        this.servlet = servlet;
        this.servletContext = servletContext;
        this.executor = executorService;
        this.useSession = useSession;
        this.isCluster = isCluster;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ITcpHandler tcpHandler = (ITcpHandler) tcpHandlerClass.newInstance();
        tcpHandler.setServlet(servlet);
        tcpHandler.setServletContext(servletContext);
        tcpHandler.setUserSession(useSession);

        channel.pipeline().addLast("decoder", new MessageDecoder(isCluster));
        channel.pipeline().addLast("encoder", new MessageEncoder());
        channel.pipeline().addLast("handler", tcpHandler);
    }
}
