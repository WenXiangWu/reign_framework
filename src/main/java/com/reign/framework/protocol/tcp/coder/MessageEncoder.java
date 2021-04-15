package com.reign.framework.protocol.tcp.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @ClassName: MessageEncoder
 * @Description: 消息编码器
 * @Author: wuwx
 * @Date: 2021-04-12 18:43
 **/
public class MessageEncoder extends MessageToByteEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) throws Exception {
        out.writeBytes(msg, msg.readerIndex(), msg.readableBytes());
    }
}
