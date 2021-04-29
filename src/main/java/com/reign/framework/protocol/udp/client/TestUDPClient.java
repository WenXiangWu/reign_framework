package com.reign.framework.protocol.udp.client;

import com.reign.framework.protocol.udp.kcp.KCPManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

/**
 * @ClassName: TestUDPClient
 * @Description: 测试UDP
 * @Author: wuwx
 * @Date: 2021-04-15 16:54
 **/
public class TestUDPClient {
    public static void main(String[] args) throws Exception {

        UDPClientBootstrap bootstrap = new UDPClientBootstrap();
        ChannelFuture future = bootstrap.start();
        future.await();

        KCPManager.getInstance().init((NioDatagramChannel)future.channel());

        KCPManager.getInstance().newKCP("sdsd",new InetSocketAddress("127.0.0.1",8010));
        //初始化本地UDP的socket
        LocalUDPSocketProvider.getInstance().initSocket();
        //启动本地UDP监听，接收数据用
        LocalUDPDataReciever.getInstance().startUp();


        while (true){
            String serverMsg = "我是客户端，这是我的时间戳:"+System.currentTimeMillis();
            byte[] toServer = serverMsg.getBytes("UTF-8");

            //开始发送
            KCPManager.getInstance().write(1L, Unpooled.wrappedBuffer(toServer));

            DatagramPacket packet = new DatagramPacket(toServer,toServer.length);
            DatagramSocket localUDPSocket = LocalUDPSocketProvider.getInstance().getLocalUDPSocket();
            if (localUDPSocket == null||localUDPSocket.isClosed()){
                break;
            }
            localUDPSocket.send(packet);

            //休眠3s后进入下一轮循环
            Thread.sleep(3000);

        }


    }
}
