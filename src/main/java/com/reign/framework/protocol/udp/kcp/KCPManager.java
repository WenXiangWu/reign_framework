package com.reign.framework.protocol.udp.kcp;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.reign.framework.core.util.WrapperUtil;
import com.reign.framework.protocol.udp.handler.RequestMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.ReferenceCountUtil;


import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: KCPManager
 * @Description: 管理器
 * @Author: wuwx
 * @Date: 2021-04-15 16:56
 **/
public class KCPManager implements KCPNettyOutput {

    private static final KCPManager instance = new KCPManager();

    // ip kcp map
    private Map<InetSocketAddress, KCPNettyWrapper> kcpMap = new HashMap<>();

    //会话 kcp map
    private Map<Long, KCPNettyWrapper> convKcpMap = new HashMap<>();

    // sessionKcp Map
    private BiMap<Long, String> sessionKcpMap = HashBiMap.create();

    //player kcp map
    private Map<Long, Long> playerKcpMap = new ConcurrentHashMap<>();

    //udp channel
    private NioDatagramChannel udpChannel;

    //id 自增器
    private AtomicInteger id = new AtomicInteger();

    //上次分配时间
    private long lastAllocTime = 0;

    //初始化标识
    private volatile boolean initFlag;


    public static KCPManager getInstance() {
        return instance;
    }

    public synchronized void init(NioDatagramChannel udpChannel) {
        if (initFlag) {
            throw new RuntimeException("kcp manager inited already");
        }
        this.udpChannel = udpChannel;
        this.initFlag = true;
    }


    /**
     * 获取kcp
     *
     * @param msg
     * @return
     */
    public synchronized KCPNettyWrapper getKCP(DatagramPacket msg) {
        if (!initFlag) {
            throw new RuntimeException("kcp manager not inited");
        }
        KCPNettyWrapper kcp = kcpMap.get(msg.sender());
        if (null == kcp) {
            long conv = msg.content().getUnsignedInt(0);
            String sessionId = sessionKcpMap.get(conv);
            Long playerId = playerKcpMap.get(conv);
            if (null == sessionId || null == playerId) {
                throw new RuntimeException("illigle udp conv,conv:" + conv);
            }
            kcp = convKcpMap.get(conv);
            if (null == kcp) {
                kcp = new KCPNettyWrapper(conv, this, msg.sender(), (ByteBufAllocator) WrapperUtil.getByteBufAllocator());
                kcpMap.put(msg.sender(), kcp);
                kcp.setSessionId(sessionId);
                kcp.setPlayerId(playerId);

                //put convKcpMap
                convKcpMap.put(conv, kcp);
                KCPScheduler.getInstance().schedule(kcp);
            } else {
                kcpMap.put(msg.sender(), kcp);
            }
        }
        return kcp;
    }

    /**
     * 写数据
     *
     * @param conv
     * @param buf
     */
    public synchronized void write(long conv, ByteBuf buf) {
        KCPNettyWrapper kcp = convKcpMap.get(conv);
        if (null != kcp) {
            kcp.send(buf);
        }
    }

    public synchronized void newKCP(String sessionId, InetSocketAddress address) {
        long conv = 1;
        KCPNettyWrapper kcp = new KCPNettyWrapper(conv, this, address, (ByteBufAllocator) WrapperUtil.getByteBufAllocator());
        kcp.setSessionId(sessionId);

        convKcpMap.put(conv, kcp);
        KCPScheduler.getInstance().schedule(kcp);
    }


    /**
     * 为玩家分配conv
     *
     * @param sessionId
     * @param playerId
     * @return
     */
    public synchronized long allocUDPConvWithPlayerId(String sessionId, long playerId) {
        releaseSession(sessionId);
        long curr = System.currentTimeMillis() / 1000;
        long conv = 0l;
        if (curr == lastAllocTime) {
            conv = curr + id.incrementAndGet();
            sessionKcpMap.put(conv, sessionId);
            playerKcpMap.put(conv, playerId);
        } else {
            conv = curr;
            sessionKcpMap.put(conv, sessionId);
            playerKcpMap.put(conv, playerId);
            id.set(0);
            lastAllocTime = curr;
        }
        return conv;

    }

    private void releaseSession(String sessionId) {
        Long conv = sessionKcpMap.inverse().remove(sessionId);
        if (null != conv) {
            KCPNettyWrapper kcp = convKcpMap.remove(conv);
            if (null != kcp) kcp.close();
        }

    }

    /**
     * 分配conv
     *
     * @param sessionId
     * @param conv
     * @return
     */
    public synchronized long allocUDPConv(String sessionId, long conv) {
        releaseSession(sessionId);
        sessionKcpMap.put(conv, sessionId);
        return conv;
    }

    public synchronized void releaseKCP(KCPNettyWrapper kcp) {
        sessionKcpMap.remove(kcp.getSessionId());
        convKcpMap.remove(kcp.getConv());
        kcpMap.remove(kcp.getUser());
        playerKcpMap.remove(kcp.getConv());
    }


    @Override
    public void handleWriteData(KCPNettyWrapper kcp, ByteBuf buf) {
        DatagramPacket packet = new DatagramPacket(buf, (InetSocketAddress) kcp.getUser());
        udpChannel.writeAndFlush(packet);
    }

    @Override
    public void handleReceiveData(KCPNettyWrapper kcp, ByteBuf in) {
        try {
            //不完整的包
            if (in.readableBytes() < 4) return;
            int dataLen = in.readInt();
            if (in.readableBytes() < dataLen) {
                return;
            }

            in.skipBytes(9);
            RequestMessage r = new RequestMessage();
            byte[] commandArray = new byte[32];
            in.readBytes(commandArray);
            r.setCommand(new String(commandArray).trim());
            r.setRequestId(in.readInt());
            byte[] contentBytes = new byte[dataLen - 45];
            in.readBytes(contentBytes);
            r.setContent(contentBytes);
            r.setKcp(kcp);
            r.setSessionId(kcp.getSessionId());

            //置playerid为了去到playeDto
            r.setGlobalKeyValue(kcp.getPlayerId());

            udpChannel.pipeline().fireChannelRead(r);

        } finally {
            ReferenceCountUtil.release(in);
        }

    }

    @Override
    public void handleException(KCPNettyWrapper kcp, Throwable t) {
        t.printStackTrace();
    }
}
