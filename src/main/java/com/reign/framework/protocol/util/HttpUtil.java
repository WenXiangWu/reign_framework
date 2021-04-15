package com.reign.framework.protocol.util;

import com.reign.framework.common.util.Tuple;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.util.WrapperUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedInput;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @ClassName: HttpUtil
 * @Description: http相关处理
 * @Author: wuwx
 * @Date: 2021-04-12 17:13
 **/
public class HttpUtil {

    private static final String WEBSOCKET_PATH = "/websocket";


    /**
     * 获取http请求中的cookies
     *
     * @param httpRequest
     * @return
     */
    public static Map<String, Cookie> getCookies(HttpRequest httpRequest) {
        Map<String, Cookie> cookies = new HashMap<String, Cookie>();
        String value = httpRequest.headers().get(HttpHeaders.Names.COOKIE);
        if (value != null) {
            Set<Cookie> cookieSet = new CookieDecoder().decode(value);
            if (cookieSet != null) {
                for (Cookie cookie : cookieSet) {
                    Cookie temp = new DefaultCookie(cookie.getName(), cookie.getValue());
                    temp.setPath(cookie.getPath());
                    temp.setDomain(cookie.getDomain());
                    temp.setSecure(cookie.isSecure());
                    temp.setHttpOnly(cookie.isHttpOnly());

                    cookies.put(temp.getName(), temp);
                }
            }

        }
        return cookies;
    }


    /**
     * 获取http请求的header
     *
     * @param httpRequest
     * @return
     */
    public static Map<String, String> getHeaders(HttpRequest httpRequest) {
        Map<String, String> headers = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : httpRequest.headers()) {
            headers.put(entry.getKey(), entry.getValue());
        }
        return headers;
    }

    /**
     * 获取参数
     *
     * @param httpRequest
     * @return
     */
    public static Tuple<byte[], byte[]> getRequestContent(FullHttpRequest httpRequest) {
        Tuple<byte[], byte[]> tuple = new Tuple<>();
        //GET
        String uri = httpRequest.getUri();
        if (!uri.endsWith(".action")) {
            String params = uri.substring(uri.indexOf(".action") + 8);
            tuple.left = params.getBytes();
        }

        //POST
        if (httpRequest.getMethod().equals(HttpMethod.POST)) {
            //获取消息内容
            ByteBuf channelBuffer = httpRequest.content();
            byte[] body = new byte[channelBuffer.readableBytes()];
            httpRequest.content().getBytes(channelBuffer.readerIndex(), body, 0, body.length);
            tuple.right = body;
        }
        return tuple;
    }


    /**
     * 处理响应
     *
     * @param ctx
     * @param chunkedInput
     * @param httpRequest
     * @throws Exception
     */
    public static void doChunkedResponse(ChannelHandlerContext ctx, ChunkedInput chunkedInput, HttpRequest httpRequest) throws Exception {
        doChunkedResponse(ctx.channel(), chunkedInput, httpRequest);
    }

    /**
     * 处理响应
     *
     * @param channel
     * @param chunkedInput
     * @param httpRequest
     * @throws Exception
     */
    public static void doChunkedResponse(Channel channel, ChunkedInput chunkedInput, HttpRequest httpRequest) throws Exception {
        FullHttpResponse nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, PooledByteBufAllocator.DEFAULT.buffer());
        nettyResponse.headers().add(HttpHeaders.Names.CONTENT_TYPE, WrapperUtil.getContentType());
        //是否保持连接
        final boolean keepActive = isKeepAlive(httpRequest);
        if (keepActive && httpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)) {
            //是http1.0协议
            nettyResponse.headers().add(HttpHeaders.Names.CONNECTION, "Keep-Alive");
        }

        //写header
        nettyResponse.headers().add(HttpHeaders.Names.CACHE_CONTROL, "no-cache");
        nettyResponse.headers().add(HttpHeaders.Names.TRANSFER_ENCODING, "chunked");

        if (chunkedInput != null) {
            //是chunk请求
            ChannelFuture writeFuture = channel.writeAndFlush(nettyResponse);
            if (!httpRequest.getMethod().equals(HttpMethod.HEAD) && !nettyResponse.getStatus().equals(HttpResponseStatus.NOT_MODIFIED)) {
                writeFuture = channel.writeAndFlush(chunkedInput);
            } else {
                chunkedInput.close();
            }
            if (!keepActive) {
                writeFuture.addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    /**
     * 处理响应
     * @param ctx
     * @param request
     * @param response
     * @param httpRequest
     * @throws Exception
     */
    public static void doResponse(ChannelHandlerContext ctx,Request request,Response response,HttpRequest httpRequest)throws  Exception{
        doResponse(ctx.channel(),request,response,httpRequest);
    }

    /**
     * 响应并写回
     *
     * @param channel
     * @param request
     * @param response
     * @param httpRequest
     * @throws Exception
     */
    public static void doResponse(Channel channel, Request request, Response response, HttpRequest httpRequest) throws Exception {
        FullHttpResponse nettyResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, PooledByteBufAllocator.DEFAULT.buffer());
        nettyResponse.headers().add(HttpHeaders.Names.CONTENT_TYPE, WrapperUtil.getContentType());
        //是否保持连接
        final boolean keepActive = isKeepAlive(httpRequest);
        if (keepActive && httpRequest.getProtocolVersion().equals(HttpVersion.HTTP_1_0)) {
            //是http1.0协议
            nettyResponse.headers().add(HttpHeaders.Names.CONNECTION, "Keep-Alive");
        }

        //拷贝头和cookie
        addHeadAndCookieToResponse(response, nettyResponse);
        //写会response
        writeResponse(channel, response, nettyResponse, httpRequest);
    }

    /**
     * 判断是否keepAlive
     *
     * @param message
     * @return
     */
    public static boolean isKeepAlive(HttpMessage message) {
        return HttpHeaders.isKeepAlive(message);
    }


    /**
     * 获取websocket访问地址
     * @param req
     * @return
     */
    public static String getWebsocketLocation(HttpRequest req){
        return "ws://"+req.headers().get(HttpHeaders.Names.HOST)+WEBSOCKET_PATH;
    }


    /**
     * 是否是WebSocket请求
     * @param request
     * @return
     */
    public static boolean isWebSocketRequest(HttpRequest request){
        return HttpHeaders.Values.UPGRADE.equalsIgnoreCase(request.headers().get(HttpHeaders.Names.CONNECTION))
        &&HttpHeaders.Values.WEBSOCKET.equalsIgnoreCase(request.headers().get(HttpHeaders.Names.UPGRADE));

    }

    /**
     * 拷贝
     *
     * @param response
     * @param nettyResponse
     */
    private static void addHeadAndCookieToResponse(Response response, HttpResponse nettyResponse) {
        //拷贝Http_header
        Map<String, String> headers = response.getHeaders();
        if (null != headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                nettyResponse.headers().add(entry.getKey(), entry.getValue());
            }
        }
        //拷贝HTTP_COOKIE
        Map<String, Object> cookies = response.getCookies();
        if (null != cookies) {
            for (Map.Entry<String, Object> entry : cookies.entrySet()) {
                nettyResponse.headers().add(HttpHeaders.Names.SET_COOKIE, entry.getValue());
            }
        }
        //设置HTTP_HEADER
        if (null != headers) {
            if (!headers.containsKey(HttpHeaders.Names.CACHE_CONTROL) && !headers.containsKey(HttpHeaders.Names.EXPIRES)) {
                nettyResponse.headers().add(HttpHeaders.Names.CACHE_CONTROL, "no-cache");
            }
        }
        //设置响应状态码
        nettyResponse.setStatus((HttpResponseStatus) response.getStatus());
    }

    /**
     * 回写response
     *
     * @param channel
     * @param response
     * @param httpResponse
     * @param httpRequest
     */
    private static void writeResponse(Channel channel, Response response, FullHttpResponse httpResponse, HttpRequest httpRequest) {
        byte[] content = null;
        final boolean keepAlive = isKeepAlive(httpRequest);
        if (httpRequest.getMethod().equals(HttpMethod.HEAD)) {
            content = new byte[0];
        } else {
            content = response.getContent();
        }
        //写内容
        httpResponse.content().writeBytes(content);
        //设置长度
        setContentLength(httpResponse, content.length);
        //写回客户端
        ChannelFuture f = channel.writeAndFlush(httpResponse);

        if (!keepAlive) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    /**
     * 设置content 长度
     *
     * @param httpResponse
     * @param contentLength
     */
    private static void setContentLength(HttpResponse httpResponse, int contentLength) {
        httpResponse.headers().add(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(contentLength));
    }
}
