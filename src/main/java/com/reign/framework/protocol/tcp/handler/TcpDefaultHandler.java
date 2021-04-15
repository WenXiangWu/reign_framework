package com.reign.framework.protocol.tcp.handler;

import com.reign.framework.core.servlet.*;
import com.reign.framework.protocol.NettyConstants;
import com.reign.framework.protocol.tcp.TcpPush;
import com.reign.framework.protocol.tcp.TcpRequest;
import com.reign.framework.protocol.tcp.TcpResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @ClassName: TcpDefaultHandler
 * @Description: tcp默认处理器
 * @Author: wuwx
 * @Date: 2021-04-14 18:44
 **/
public class TcpDefaultHandler extends ChannelInboundHandlerAdapter implements ITcpHandler {

    /**
     * 命令处理器
     */
    private Servlet servlet;
    /**
     * 系统应用环境
     */
    private ServletContext sc;
    /**
     * 是否使用session
     */
    private boolean useSession;

    public TcpDefaultHandler() {
    }

    public TcpDefaultHandler(Servlet servlet, ServletContext sc, boolean useSession) {
        this.servlet = servlet;
        this.sc = sc;
        this.useSession = useSession;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (!useSession) {
            super.channelActive(ctx);
            return;
        }

        //为当前连接创建一个session
        String sessionId = ctx.attr(NettyConstants.SESSIONID).get();
        if (sessionId == null) {
            Session session = SessionManager.getInstance().getSession(null, true);
            session.setPush(new TcpPush(ctx.channel()));
            ctx.attr(NettyConstants.SESSIONID).set(session.getId());
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RequestMessage) {
            //接收消息
            RequestMessage message = (RequestMessage) msg;
            message.setSessionId(ctx.attr(NettyConstants.SESSIONID).get());

            Response response = new TcpResponse(ctx.channel());
            Request request = new TcpRequest(ctx, sc, ctx.channel(), message);
            servlet.service(request, response);
        }

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
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
        this.useSession = userSession;
    }
}
