package com.reign.framework.protocol.tcp.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @ClassName: TencentPolicyHandler
 * @Description: 解析腾讯TGW协议
 * @Author: wuwx
 * @Date: 2021-04-14 18:46
 **/
public class TencentPolicyHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 14) return;

        String head = new String(in.array());
        if (head.startsWith("tgw")) {
            in.readBytes(100);
        }
        //移除自身
        ctx.channel().pipeline().remove(this);
        if (in.readableBytes() < 4) {
            return;
        }
        return;
    }
}
