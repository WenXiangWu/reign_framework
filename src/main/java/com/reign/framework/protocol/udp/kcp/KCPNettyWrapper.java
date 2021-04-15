package com.reign.framework.protocol.udp.kcp;

import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import io.netty.buffer.ByteBufAllocator;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @ClassName: KCPNettyWrapper
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:56
 **/
public class KCPNettyWrapper implements KCPOutput {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.framework.udp");

    private KCP kcp;

    private KCPNettyOutput output;

    private ByteBufAllocator allocator;

    //sessionid
    private String sessionId;

    //玩家id
    private Long playerId;
    //是否关闭了
    private boolean isClosed;

    //上次收到数据时间
    private long lastTime;

    //数据接收超时时间
    private long timeout;

    //发送队列
    private final Queue<byte[]> sendQueue;

    //接收输入嘟咧
    private final Queue<byte[]> inputQueue;

    public Long getPlayerId() {
        return playerId;
    }

    public void setPlayerId(Long playerId) {
        this.playerId = playerId;
    }

    public KCPNettyWrapper(long conv,KCPNettyOutput output,Object user,ByteBufAllocator allocator){
        this.kcp = new KCP(conv,this,user,log);
        this.output = output;
        this.allocator = allocator;
        this.sendQueue = new LinkedBlockingQueue<>();
        this.inputQueue = new LinkedBlockingQueue<>();
        this.kcp.wndSize(128,128);
        this.kcp.noDelay(1,2,3,1);
        this.timeout=180000;

        //加入schedule
        KCPScheduler.getInstance().schedule(this);
    }

    @Override
    public void handleWriteData(KCP kcp, byte[] data, int size) {

    }

    @Override
    public void handleReceiveData(KCP kcp, byte[] data) {

    }
}
