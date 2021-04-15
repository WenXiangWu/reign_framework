package com.reign.framework.protocol.tcp.coder;

import com.reign.framework.protocol.tcp.handler.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @ClassName: MessageDecoder
 * @Description: 消息解码器
 * @Author: wuwx
 * @Date: 2021-04-12 18:43
 **/
public class MessageDecoder extends ByteToMessageDecoder {

    //是否是集群模式
    private boolean isCluster = false;

    public MessageDecoder(boolean isCluster) {
        this.isCluster = isCluster;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 4) {
            return;
        }
        int dataLen = in.getInt(in.readerIndex());
        if (in.readableBytes() < dataLen + 1) {
            return;
        }
        in.skipBytes(4);

        if (isCluster) {
            in.skipBytes(1);
        }

        RequestMessage r = new RequestMessage();
        byte[] commandBytes = new byte[32];
        in.readBytes(commandBytes);
        r.setCommand(new String(commandBytes).trim());
        r.setRequestId(in.readInt());
        byte[] contentBytes = new byte[dataLen - 37];
        in.readBytes(contentBytes);
        r.setContent(contentBytes);
        out.add(r);

    }
}
