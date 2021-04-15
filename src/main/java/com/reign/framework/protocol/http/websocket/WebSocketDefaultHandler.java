package com.reign.framework.protocol.http.websocket;

import com.reign.framework.core.servlet.Servlet;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * @ClassName: WebSocketDefaultHandler
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-12 16:31
 **/
public class WebSocketDefaultHandler extends ChannelInboundHandlerAdapter {


    public WebSocketDefaultHandler(Servlet servlet, WebSocketServerHandshaker handshaker, String ip) {
    }
}
