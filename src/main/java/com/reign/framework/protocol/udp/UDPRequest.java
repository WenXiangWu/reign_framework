package com.reign.framework.protocol.udp;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.*;
import com.reign.framework.protocol.udp.handler.RequestMessage;
import com.reign.framework.protocol.udp.kcp.KCPNettyWrapper;
import com.reign.framework.protocol.util.RequestUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: UDPRequest
 * @Description: UDP请求
 * @Author: wuwx
 * @Date: 2021-04-15 16:54
 **/
public class UDPRequest implements Request {

    private Map<String, String[]> paramMap = new HashMap<>();

    private String command;

    private KCPNettyWrapper kcp;

    private int requestId;

    private ServletContext sc;

    private String sessionId;

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


    /**
     * 构造函数
     *
     * @param ctx
     * @param sc
     * @param kcp
     * @param requestMessage
     */
    public UDPRequest(ChannelHandlerContext ctx, ServletContext sc, KCPNettyWrapper kcp, RequestMessage requestMessage) {
        this.ctx = ctx;
        this.sc = sc;
        this.command = requestMessage.getCommand();
        this.requestId = requestMessage.getRequestId();
        this.kcp = kcp;
        this.content = requestMessage.getContent();
        this.createTime = System.currentTimeMillis();
        this.globalKeyValue = requestMessage.getGlobalKeyValue();
        this.createTime = System.currentTimeMillis();

        sessionId = kcp.getSessionId();
        SessionManager.getInstance().access(sessionId);

    }


    /**
     * 解析请求参数
     *
     * @param bytes
     */
    private void parseParam(byte[] bytes) {
        if (parse) return;
        if (bytes == null) return;
        try {
            parseParam(new String(bytes));
        } catch (UnsupportedOperationException e) {

        }
        parse = true;
    }

    /**
     * 解析参数
     *
     * @param content
     * @throws UnsupportedOperationException
     */
    private void parseParam(String content) throws UnsupportedOperationException {
        //延迟赋值
        this.paramMap = new HashMap<>();
        RequestUtil.parseParamWithoutDecode(content, paramMap);
        if (null == sessionId) {
            String[] values = paramMap.get("sessionId");
            sessionId = ((null == values) ? null : values[0]);
        }

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
        return null == paramMap ? null : paramMap.get(key);
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
        throw new UnsupportedOperationException("udp request cannot offer this operation");
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
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public int getRequestId() {
        return requestId;
    }

    @Override
    public void setSessionId(String sessionId) {

    }

    @Override
    public ServerProtocol getProtocol() {
        return ServerProtocol.UDP;
    }

    @Override
    public String getHeader(String key) {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public String getCookieValue(String key) {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public Collection<?> getCookies() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public boolean isHttpLong() {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return ((InetSocketAddress)kcp.getUser());
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void pushAndClose(Object buffer) {
        throw new UnsupportedOperationException("udp request cannot offer this operation");
    }

    @Override
    public String getIp() {
        return ((InetSocketAddress)kcp.getUser()).getAddress().getHostAddress();
    }

    @Override
    public Push newPush() {
        return new UDPPush(kcp);
    }

    @Override
    public long createTime() {
        return createTime;
    }
}
