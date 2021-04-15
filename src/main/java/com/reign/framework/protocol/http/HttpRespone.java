package com.reign.framework.protocol.http;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.servlet.Response;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: HttpRespone
 * @Description: http响应
 * @Author: wuwx
 * @Date: 2021-04-12 16:30
 **/
public class HttpRespone implements Response {

    private Channel channel;

    private HttpResponse httpResponse;

    private Map<String, String> headers;

    private Map<String, Object> cookies;

    private ByteArrayOutputStream outPutStream;


    public HttpRespone(Channel channel) {
        this.channel = channel;
    }

    @Override
    public Object getChannel() {
        return channel;
    }

    @Override
    public boolean isWritable() {
        return channel.isWritable();
    }

    @Override
    public Object write(Object obj) throws IOException {
        if (channel.isWritable()) {
            getOutPutStream().write((byte[]) obj);
        }
        return null;
    }


    @Override
    public ServerProtocol getProtocol() {
        return ServerProtocol.HTTP;
    }

    @Override
    public void addCookie(Object cookie) {
        Cookie _cookie = (Cookie) cookie;
        getInternalCookies().put(_cookie.getName(), _cookie);
    }

    @Override
    public Map<String, Object> getCookies() {
        return cookies;
    }


    @Override
    public void addHeader(String name, String value) {
        getHeads().put(name, value);
    }

    @Override
    public byte[] getContent() {
        return getOutPutStream().toByteArray();
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }

    @Override
    public void setStatus(Object status) {
        if (httpResponse == null) {
            httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, (HttpResponseStatus) status);
            return;
        }
        httpResponse.setStatus((HttpResponseStatus) status);
    }

    @Override
    public Object getStatus() {
        if (httpResponse == null){
            return HttpResponseStatus.OK;
        }
        return httpResponse.getStatus();
    }

    @Override
    public void markClose() {
        //不需要处理
    }


    private synchronized Map<String, Object> getInternalCookies() {
        if (null == cookies) {
            cookies = new HashMap<>();
        }
        return cookies;
    }

    private synchronized Map<String, String> getHeads() {
        if (null == headers) {
            headers = new HashMap<>();
        }
        return headers;
    }


    public synchronized ByteArrayOutputStream getOutPutStream() {
        if (outPutStream == null) {
            outPutStream = new ByteArrayOutputStream();
        }
        return outPutStream;
    }
}
