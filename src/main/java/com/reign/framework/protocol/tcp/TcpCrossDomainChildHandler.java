package com.reign.framework.protocol.tcp;

import com.reign.framework.protocol.tcp.coder.MessageDecoder;
import com.reign.framework.protocol.tcp.coder.MessageEncoder;
import com.reign.framework.protocol.tcp.handler.FlashPolicyHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

/**
 * @ClassName: TcpCrossDomainChildHandler
 * @Description: crossDomain
 * @Author: wuwx
 * @Date: 2021-04-14 18:45
 **/
public class TcpCrossDomainChildHandler extends ChannelInitializer<SocketChannel> {
    public TcpCrossDomainChildHandler() throws Exception {

    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("flashPolicy",new FlashPolicyHandler());
        pipeline.addLast("decoder",new MessageDecoder(false));
        pipeline.addLast("encoder",new MessageEncoder());
    }
}
