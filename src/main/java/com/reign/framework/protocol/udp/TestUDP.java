package com.reign.framework.protocol.udp;


import com.reign.framework.protocol.udp.kcp.KCPManager;

/**
 * @ClassName: TestUDP
 * @Description: 测试UDP服务端
 * @Author: wuwx
 * @Date: 2021-04-15 16:52
 **/
public class TestUDP {

    public static void main(String[] args) throws Exception {
        UDPServletBootstrap bootstrap = new UDPServletBootstrap();
        bootstrap.start();
        KCPManager.getInstance().allocUDPConv("123", 1L);
    }


}
