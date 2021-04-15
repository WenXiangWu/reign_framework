package com.reign.framework.protocol.http;


import com.reign.framework.common.ServerConstants;
import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.*;
import com.reign.framework.protocol.NettyConstants;
import com.reign.framework.protocol.http.handler.HttpChunkAction;
import com.reign.framework.protocol.util.RequestUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import io.netty.handler.codec.http.FullHttpRequest;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: HttpRequest
 * @Description: http请求
 * @Author: wuwx
 * @Date: 2021-04-12 16:30
 **/
public class HttpRequest implements Request {


    private Map<String, String[]> paramMap = new HashMap<>();

    private String command;

    private Channel channel;

    private ServletContext sc;

    private String sessionId;

    private int requestId;

    private ChannelHandlerContext ctx;

    private Map<String, Cookie> cookies;

    private Map<String, String> headers;

    private Response response;

    private boolean longHttp;

    /**
     * get请求的输入流
     */
    private byte[] getContent;

    /**
     * post请求的输入流
     */
    private byte[] postContent;

    /**
     * 是否已经解析过get
     */
    private volatile boolean parseGet;
    /**
     * 是否已经解析过post
     */
    private volatile boolean parsePost;

    /**
     * 链接url
     */
    private String url;
    /**
     * 请求参数
     */
    private Object[] args;
    /**
     * 创建时间
     */
    private long createTime;


    public HttpRequest(ChannelHandlerContext ctx, ServletContext sc, Channel channel, io.netty.handler.codec.http.HttpRequest httpRequest,
                       byte[] getContent, byte[] postContent, String command, Map<String, Cookie> cookies,
                       Map<String, String> headers, Response response, String uri) {
        this.ctx = ctx;
        this.sc = sc;
        this.channel = channel;
        this.response = response;
        this.cookies = cookies;
        this.headers = headers;
        this.getContent = getContent;
        this.postContent = postContent;
        this.url = uri;
        this.createTime = System.currentTimeMillis();

        //获得command
        if ("gateway".equals(command)) {
            String[] value = getParamterValues(ServerConstants.COMMAND);
            this.command = null == value ? null : value[0];
        } else {
            this.command = command;
        }

        //touch下session
        sessionId = getCookieValue(ServerConstants.JESSIONID);

        //长连接
        if (ServerConstants.LONG_HTTP.equalsIgnoreCase(this.command)) {
            Session session = getSession(false);
            if (null != session) {
                HttpPush push = new HttpPush(channel, new HttpChunkAction<>(ctx, httpRequest));
                session.setPush(push);
            }
            this.longHttp = true;
        }


    }

    @Override
    public Map<String, String[]> getParamterMap() {
        parseParam();
        return paramMap;
    }

    /**
     * 解析请求参数
     */
    private void parseParam() {
        parseGetParam(getContent);
        parsePostParam(postContent);
    }

    private void parsePostParam(byte[] bytes) {
        if (parsePost) return;
        if (null != bytes) {
            try {
                RequestUtil.parseParam(new String(bytes), paramMap);
            } catch (UnsupportedOperationException e) {
                //ignore
            }
        }
        parsePost = true;
    }

    private void parseGetParam(byte[] bytes) {
        if (parseGet) return;
        if (null != bytes) {
            try {
                RequestUtil.parseParam(new String(bytes), paramMap);
            } catch (UnsupportedOperationException e) {
                //ignore
            }
        }
        parseGet = true;
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
        parseParam();
        return paramMap.get(key);
    }

    @Override
    public Object getGlobalKeyValue() {
        throw new UnsupportedOperationException("http not support getGlobalKeyValue operation");
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
            //设置cookie
            response.addCookie(new DefaultCookie(ServerConstants.JESSIONID, session.getId()));
        }

        //touch session
        if (null != session) {
            session.access();
        }
        return session;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public Session getNewSession() {
        //创建session
        Session session = SessionManager.getInstance().getSession(null, true);
        return session;
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
        return ServerProtocol.HTTP;
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String getCookieValue(String key) {
        if (cookies != null) {
            Cookie cookie = cookies.get(key);
            if (null != cookie) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    public Collection<?> getCookies() {
        return cookies.values();
    }

    @Override
    public boolean isHttpLong() {
        return this.longHttp;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public byte[] getContent() {
        byte[] content = postContent;
        if (null == content) {
            content = getContent;
        }
        return content;
    }

    @Override
    public void pushAndClose(Object buffer) {
        throw new UnsupportedOperationException("http not support pushAndClose operation");
    }

    @Override
    public String getIp() {
        String ip = getHeader("x-real-ip");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = getRemoteAddress().getAddress().getHostAddress();
        }
        return ip;
    }

    @Override
    public Push newPush() {
        throw new UnsupportedOperationException("http not support newPush operation");
    }

    @Override
    public long createTime() {
        return createTime;
    }
}
