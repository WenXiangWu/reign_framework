package com.reign.framework.protocol.udp.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * @ClassName: LocalUDPDataReciever
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:52
 **/
public class LocalUDPDataReciever {
    private static LocalUDPDataReciever instance = null;

    private Thread thread = null;

    public static LocalUDPDataReciever getInstance() {
        if (instance == null) {
            instance = new LocalUDPDataReciever();
        }
        return instance;
    }

    public void startUp() {
        this.thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    LocalUDPDataReciever.this.udpListeningImpl();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        this.thread.start();
    }

    private void udpListeningImpl() throws IOException {

        while (true) {
            byte[] data = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data,data.length);
            DatagramSocket localUDPSocket = LocalUDPSocketProvider.getInstance().getLocalUDPSocket();
            if ((localUDPSocket==null)||localUDPSocket.isClosed()){
                continue;
            }

            //阻塞直到收到数据
            localUDPSocket.receive(packet);
            //解析服务端发来的数
            String pFromServer = new String(packet.getData(),0,packet.getLength(),"UTF-8");
        }

    }
}
