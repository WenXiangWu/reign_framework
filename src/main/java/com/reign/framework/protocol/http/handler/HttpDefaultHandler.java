package com.reign.framework.protocol.http.handler;


import com.reign.framework.common.ServerConstants;
import com.reign.framework.common.util.Tuple;
import com.reign.framework.core.servlet.*;
import com.reign.framework.protocol.IpUtil;
import com.reign.framework.protocol.NettyConstants;
import com.reign.framework.protocol.http.HttpRespone;
import com.reign.framework.protocol.http.websocket.WebSocketDefaultHandler;
import com.reign.framework.protocol.util.HttpUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: HttpDefaultHandler
 * @Description: 默认http协议处理器
 * @Author: wuwx
 * @Date: 2021-04-12 16:30
 **/
public class HttpDefaultHandler extends ChannelInboundHandlerAdapter {

    private final Servlet servlet;

    private final ServletContext sc;

    private ChunkedWriteHandler chunkedWriteHandler = new ChunkedWriteHandler();

    private static final Pattern pattern = Pattern.compile("^/root/([\\w-/]*)\\.action([\\s\\s]*)?$");

    public HttpDefaultHandler(Servlet servlet, ServletContext sc) {
        this.servlet = servlet;
        this.sc = sc;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        System.out.println("channel error,channel " + ctx.channel().toString() + cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            final FullHttpRequest httpRequest = (FullHttpRequest) msg;
            try {
                //处理跨域请求
                if (httpRequest.getUri().equalsIgnoreCase("/crossdomain.xml")) {
                    FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, PooledByteBufAllocator.DEFAULT.buffer(ServerConstants.CROSSDOMAIN.length));
                    response.content().writeBytes(ServerConstants.CROSSDOMAIN);
                    response.headers().add(HttpHeaders.Names.CONTENT_TYPE, "text/plain;charset=UTF-8");
                    ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    return;
                } else if (httpRequest.getUri().equalsIgnoreCase("test.html")) {

                    //乱七八糟，不管
                }

                //判断是否为WebSocket
                if (HttpUtil.isWebSocketRequest(httpRequest)) {
                    WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(HttpUtil.getWebsocketLocation(httpRequest), null, false);
                    WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(httpRequest);
                    if (null == handshaker) {
                        WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
                    } else {
                        handshaker.handshake(ctx.channel(), httpRequest);
                        //获取cookie
                        Map<String, Cookie> cookies = HttpUtil.getCookies(httpRequest);

                        //touch session，这里不处理
                        Cookie cookie = cookies.get(ServerConstants.JESSIONID);
                        String sessionId=(null == cookie)?null:cookie.getValue();
                        ctx.attr(NettyConstants.SESSIONID).set(sessionId);
                        //获取id
                        String ip = IpUtil.getIp(httpRequest, ctx.channel());

                        //替换掉普通的handler
                        ctx.channel().pipeline().replace(HttpDefaultHandler.class, "wshandler", new WebSocketDefaultHandler(servlet, handshaker, ip));
                    }
                    return;
                }
                String uri = httpRequest.getUri();
                Matcher matcher = pattern.matcher(uri);
                if (!matcher.find()) {
                    //不是正常的请求
                    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_GATEWAY);
                    ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                    return;
                }


                String command = matcher.group(1);

                //获取参数
                Tuple<byte[],byte[]> requestContent = HttpUtil.getRequestContent(httpRequest);
                //获取cookies
                Map<String,Cookie> cookieMap = HttpUtil.getCookies(httpRequest);
                //获取headers
                Map<String,String> headers = HttpUtil.getHeaders(httpRequest);
                //解析消息
                final Response response = new HttpRespone(ctx.channel());
                final Request request = new com.reign.framework.protocol.http.HttpRequest(ctx,ctx.channel(),httpRequest,requestContent.left,requestContent.right,command,cookieMap,headers,response,uri);

                if (!request.isHttpLong()){
                    servlet.service(request,response);
                    //响应处理
                    HttpUtil.doResponse(ctx,request,response,httpRequest);
                }
            }catch (Exception e){
                //返回500
                HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.INTERNAL_SERVER_ERROR);
                ctx.channel().writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }finally {
                httpRequest.release();
            }

        }
    }
}
