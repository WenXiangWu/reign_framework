package com.reign.framework.protocol.tcp.handler;

import com.reign.framework.common.ServerConstants;
import com.reign.framework.core.util.WrapperUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @ClassName: FlashPolicyHandler
 * @Description: flash策略文件handler
 * @Author: wuwx
 * @Date: 2021-04-14 18:46
 **/
public class FlashPolicyHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes()<2){
            return;
        }
        //魔数1
        final int magic1 = in.getUnsignedByte(in.readerIndex());
        //魔数2
        final int magic2 =in.getUnsignedByte(in.readerIndex()+1);
        //判断是否是策略文件
        boolean isFlashPolicyRequest = (magic1=='<'&&magic2=='p');

        //是flash策略文件模式
        if (isFlashPolicyRequest){
            //discard everything
            in.skipBytes(in.readableBytes());
            ctx.channel().writeAndFlush(WrapperUtil.wrapper(ServerConstants.CROSSDOMAIN)).addListener(ChannelFutureListener.CLOSE);
            ctx.channel().pipeline().remove(this);
            return;
        }
        ctx.channel().pipeline().remove(this);
        return;
    }
}
