package com.reign.framework.protocol.tcp;

import com.reign.framework.common.ServerConstants;
import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.*;
import com.reign.framework.protocol.NettyConstants;
import com.reign.framework.protocol.tcp.handler.RequestMessage;
import com.reign.framework.protocol.util.RequestUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultCookie;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: TcpRequest
 * @Description: Tcp请求对象
 * @Author: wuwx
 * @Date: 2021-04-14 18:45
 **/
public class TcpRequest implements Request {


    private Map<String, String[]> paramMap = new HashMap<>();

    private String command;

    private Channel channel;

    private ServletContext sc;

    private String sessionId;

    private int requestId;

    private ChannelHandlerContext ctx;

    /**
     * 输入流
     */
    private byte[] content;
    /**
     * 是否已经解析过
     */
    private volatile boolean parse;

    /**
     * 全局缓存key值
     */
    private Object globalKeyValue;

    /**
     * 请求参数
     */
    private Object[] args;
    /**
     * 创建时间
     */
    private long createTime;


    public TcpRequest(ChannelHandlerContext ctx, ServletContext sc, Channel channel, RequestMessage requestMessage) {
        this.ctx = ctx;
        this.sc = sc;
        this.channel = channel;
        this.requestId = requestMessage.getRequestId();
        this.content = requestMessage.getContent();
        this.createTime = System.currentTimeMillis();
        this.globalKeyValue = requestMessage.getGlobalKeyValue();
        sessionId = requestMessage.getSessionId();
        SessionManager.getInstance().access(sessionId);

    }

    @Override
    public Map<String, String[]> getParamterMap() {
        parseParam(content);
        return paramMap;
    }

    @Override
    public Object[] getRequestArgs() {
        return args;
    }

    @Override
    public void setRequestArgs(Object[] args) {
        this.args = args;
    }

    @Override
    public String[] getParamterValues(String key) {
        parseParam(content);
        return paramMap.get(key);
    }

    @Override
    public Object getGlobalKeyValue() {
        return globalKeyValue;
    }

    @Override
    public Session getSession() {
        return getSession(true);
    }

    @Override
    public Session getSession(boolean allowCreate) {
        Session session = SessionManager.getInstance().getSession(sessionId, allowCreate);
        if (allowCreate && (null != session && !session.getId().equals(sessionId))) {
            //以前没有session或者以前的session失效了
            sessionId = session.getId();
            session.setPush(newPush());
        }

        //touch session
        if (null != session) {
            session.access();
        }
        return session;
    }

    @Override
    public Session getNewSession() {
        throw new UnsupportedOperationException("tcp request not support session");
    }

    @Override
    public ServletContext getServletContext() {
        return sc;
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public Object getChannel() {
        return channel;
    }

    @Override
    public int getRequestId() {
        return requestId;
    }

    @Override
    public void setSessionId(String sessionId) {
        ctx.attr(NettyConstants.SESSIONID).set(sessionId);
        this.sessionId = sessionId;
    }

    @Override
    public ServerProtocol getProtocol() {
        return ServerProtocol.TCP;
    }

    @Override
    public String getHeader(String key) {
        throw new UnsupportedOperationException("tcp request not support getHeader");
    }

    @Override
    public String getCookieValue(String key) {
        throw new UnsupportedOperationException("tcp request not support getCookieValue");
    }

    @Override
    public Collection<?> getCookies() {
        throw new UnsupportedOperationException("tcp request not support getCookies");
    }

    @Override
    public boolean isHttpLong() {
        throw new UnsupportedOperationException("tcp request not support isHttpLong");
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void pushAndClose(Object buffer) {
        if (null != channel && channel.isWritable()) {
            ChannelFuture future = channel.write(buffer);
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public String getIp() {
        return getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public Push newPush() {
        return new TcpPush(channel);
    }

    @Override
    public long createTime() {
        return createTime;
    }

    /**
     * 解析请求参数
     *
     * @param bytes
     */
    private void parseParam(byte[] bytes) {
        if (parse) return;
        try {
            RequestUtil.parseParamWithoutDecode(new String(bytes), paramMap);
        } catch (UnsupportedOperationException e) {

        }
        parse = true;
    }
}
