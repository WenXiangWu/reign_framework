package com.reign.framework.protocol;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;

/**
 * @ClassName: IpUtil
 * @Description: 获取ip工具类
 * @Author: wuwx
 * @Date: 2021-04-14 18:23
 **/
public class IpUtil {

    public static String getIp(HttpRequest httpRequest, Channel channel) {
        String ip = httpRequest.headers().get("x-real-ip");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        }
        return ip;
    }
}
