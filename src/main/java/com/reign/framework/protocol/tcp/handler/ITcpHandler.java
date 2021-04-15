package com.reign.framework.protocol.tcp.handler;

import com.reign.framework.core.servlet.Servlet;
import com.reign.framework.core.servlet.ServletContext;
import io.netty.channel.ChannelInboundHandler;

/**
 * @ClassName: ITcpHandler
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:10
 **/
public interface ITcpHandler extends ChannelInboundHandler {

    void setServletContext(ServletContext sc);

    void setServlet(Servlet servlet);

    void setUserSession(boolean userSession);
}
