package com.reign.framework.protocol.udp.handler;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.Servlet;
import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.protocol.udp.UDPResponse;
import com.reign.framework.protocol.tcp.handler.ITcpHandler;
import com.reign.framework.protocol.udp.UDPRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @ClassName: UDPDefaultHandler
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:54
 **/
public class UDPDefaultHandler extends ChannelInboundHandlerAdapter implements ITcpHandler {

    private Servlet servlet;

    private ServletContext sc;

    public UDPDefaultHandler(Servlet servlet, ServletContext sc) {
        this.servlet = servlet;
        this.sc = sc;
    }

    public UDPDefaultHandler() {
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RequestMessage){
            RequestMessage message = (RequestMessage) msg;

            Response response = new UDPResponse(message.getKcp());
            Request request = new UDPRequest(ctx,sc,message.getKcp(),message);
            servlet.service(request,response);
        }
    }

    @Override
    public void setServletContext(ServletContext sc) {
        this.sc = sc;
    }

    @Override
    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    @Override
    public void setUserSession(boolean userSession) {
        return;
    }
}
