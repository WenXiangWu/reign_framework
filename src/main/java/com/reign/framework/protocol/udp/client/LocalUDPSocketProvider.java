package com.reign.framework.protocol.udp.client;

import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * @ClassName: LocalUDPSocketProvider
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:53
 **/
public class LocalUDPSocketProvider {

    private static LocalUDPSocketProvider instance = null;
    private DatagramSocket localUDPSocket = null;

    public static LocalUDPSocketProvider getInstance() {
        if (instance == null){
            instance = new LocalUDPSocketProvider();
        }
        return instance;
    }

    public void initSocket(){
        try{
            this.localUDPSocket = new DatagramSocket();

            this.localUDPSocket.connect(InetAddress.getByName("127.0.0.1"),8010);
            this.localUDPSocket.setReuseAddress(true);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public DatagramSocket getLocalUDPSocket() {
        return localUDPSocket;
    }
}
