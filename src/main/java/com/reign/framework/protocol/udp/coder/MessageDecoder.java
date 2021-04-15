package com.reign.framework.protocol.udp.coder;

import com.reign.framework.protocol.tcp.handler.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @ClassName: MessageDecoder
 * @Description: 解码器
 * @Author: wuwx
 * @Date: 2021-04-15 16:53
 **/
public class MessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //不完整的包
        if (in.readableBytes() < 4) return;
        int dataLen = in.getInt(in.readerIndex());
        //不完整的包
        if (in.readableBytes() < dataLen + 4) {
            return;
        }
        in.skipBytes(4);
        RequestMessage r = new RequestMessage();
        byte[] commandArray = new byte[32];
        in.readBytes(commandArray);
        r.setCommand(new String(commandArray).trim());
        r.setRequestId(in.readInt());
        byte[] contentBytes = new byte[dataLen-36];
        in.readBytes(contentBytes);
        r.setContent(contentBytes);

        out.add(r);
    }
}
