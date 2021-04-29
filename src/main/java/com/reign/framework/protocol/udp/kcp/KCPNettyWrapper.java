package com.reign.framework.protocol.udp.kcp;

import com.reign.framework.core.servlet.Session;
import com.reign.framework.core.servlet.SessionManager;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import com.reign.framework.protocol.udp.UDPPush;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.util.ReferenceCountUtil;

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

    public KCPNettyWrapper(long conv, KCPNettyOutput output, Object user, ByteBufAllocator allocator) {
        this.kcp = new KCP(conv, this, user, log);
        this.output = output;
        this.allocator = allocator;
        this.sendQueue = new LinkedBlockingQueue<>();
        this.inputQueue = new LinkedBlockingQueue<>();
        this.kcp.wndSize(128, 128);
        this.kcp.noDelay(1, 2, 3, 1);
        this.timeout = 180000;

        //加入schedule
        KCPScheduler.getInstance().schedule(this);
    }


    /**
     * 发送数据
     *
     * @param byteBuf
     */
    public void send(ByteBuf byteBuf) {
        try {
            if (isClosed) {
                this.output.handleException(this, new IllegalStateException("kcp closed already"));
                return;
            }
            byte[] array = new byte[byteBuf.readableBytes()];
            byteBuf.getBytes(byteBuf.readerIndex(), array);
            this.sendQueue.add(array);
        } finally {
            ReferenceCountUtil.release(byteBuf);
        }

    }

    /**
     * 上层接收到数据
     *
     * @param buf
     */
    public void input(ByteBuf buf) {
        if (isClosed) {
            this.output.handleException(this, new IllegalStateException("kcp closed already"));
            return;
        }
        byte[] array = new byte[buf.readableBytes()];
        buf.getBytes(buf.readerIndex(), array);
        this.inputQueue.add(array);
    }

    public void update(long current) {
        //input
        byte[] array = null;
        int errorCode = 0;
        while ((array = inputQueue.poll()) != null) {
            errorCode = this.kcp.input(array, array.length);
            this.lastTime = current;
            if (errorCode != 0) {
                this.close();
                this.output.handleException(this, new IllegalStateException("input error,code " + errorCode));
                return;
            }
        }

        //recv data
        int dataLen = kcp.recv();
        if (dataLen > 0) {
            this.lastTime = current;
        }

        //send data
        while ((array = sendQueue.poll()) != null) {
            errorCode = this.kcp.send(array, array.length);
            if (errorCode != 0) {
                this.close();
                this.output.handleException(this, new IllegalStateException("send error,code:'" + errorCode));
                return;
            }
        }
        //update
        long nextUpdateTime = kcp.check(current);
        if (nextUpdateTime<=current){
            kcp.update(current);
        }

        //check timeout
        if (this.timeout>0 &&lastTime>0&&current-this.lastTime>this.timeout){
            this.close();
        }


    }

    public Object getUser(){
        return kcp.getUser();
    }

    public long getConv(){
        return kcp.getConv();
    }

    public String getSessionId(){
        return sessionId;
    }

    public void setSessionId(String sessionId){
        this.sessionId =sessionId;
        Session session= SessionManager.getInstance().getSession(sessionId);
        if (null!=session){
            session.setUDPPush(new UDPPush(this));
        }
    }


    public void close(){
        KCPScheduler.getInstance().unschedule(this);
        this.isClosed = true;
    }


    public boolean isClosed() {
        return isClosed;
    }

    /**
     * 处理写树
     * @param kcp
     * @param data
     * @param size
     */
    @Override
    public void handleWriteData(KCP kcp, byte[] data, int size) {
        ByteBuf buf = allocator.buffer(size);
        buf.writeBytes(data,0,size);
        this.output.handleWriteData(this,buf);
    }

    /**
     * 处理接收数据
     * @param kcp
     * @param data
     */
    @Override
    public void handleReceiveData(KCP kcp, byte[] data) {
        ByteBuf buf = allocator.buffer(data.length);
        buf.writeBytes(data,0,data.length);
        this.output.handleReceiveData(this,buf);
    }
}
