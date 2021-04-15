package com.reign.framework.protocol.udp.kcp;

import com.reign.framework.log.Logger;

import java.util.ArrayList;

/**
 * @ClassName: KCP
 * @Description: KCP协议
 * @Author: wuwx
 * @Date: 2021-04-15 16:55
 **/
public class KCP {

    private Logger log;
    //ip地址
    private Object user;
    //KCP输出器
    private KCPOutput output;

    /**
     * --------------KCP变量-----------------------
     **/
    //会话id
    private long conv = 0;
    //第一个待确认的ack
    private long snd_una = 0;
    //下一个要发送包的编号
    private long snd_nxt = 0;
    //下一个要接受包的编号
    private long rcv_nxt = 0;

    private long ts_recent = 0;

    private long ts_lastack = 0;

    private long ts_probe = 0;

    private long probe_wait = 0;
    //发送窗口
    private long snd_wnd = 0;
    //接收窗口
    private long rcv_wnd = 0;
    //远端滑动窗口大小
    private long rmt_wnd = 0;

    private long cwnd = 0;

    private long incr = 0;

    private long probe = 0;
    //mtu 大小
    private int mtu = 0;
    //一个mtu数据的大小
    private int mss = 0;
    //是否开启stream模式
    private boolean stream;
    //默认缓冲区，3个mtu
    private byte[] buffer = null;
    //接受缓冲区
    private ArrayList<Segment> rcv_buf = new ArrayList<>(128);
    //发送缓冲区
    private ArrayList<Segment> snd_buf = new ArrayList<>(128);
    //已接收包队列
    private ArrayList<Segment> rcv_que = new ArrayList<>(128);
    //已发送包队列
    private ArrayList<Segment> snd_que = new ArrayList<>(128);
    //ack列表
    private ArrayList<Long> ackList = new ArrayList<>(256);

    private long state = 0;
    private long ackblock = 0;
    private long ackcount = 0;
    //往返传输时间
    private long rx_srtt = 0;
    //往返传输时间中间值
    private long rx_rttval = 0;
    private long rx_rto = 0;
    private long rx_minrto = 0;
    private long current = 0;
    private long interval = 0;
    private long ts_flush = 0;
    private long nodelay = 0;
    private long updated = 0;
    private long ssthresh = 0;
    //快速重发
    private long fastresend = 0;
    private long nocwnd = 0;
    private long xmit = 0;
    private long dead_link = 0;

    public KCP(long conv, KCPOutput output, Object user, Logger log) {
        this.conv = conv;
        this.log = log;
        this.output = output;
        this.user = user;

        //变量初始化
        snd_wnd = KCPConstants.IKCP_WND_SND;
        rcv_wnd = KCPConstants.IKCP_WND_RCV;
        rmt_wnd = KCPConstants.IKCP_WND_RCV;

        mtu = KCPConstants.IKCP_MTU_DEF;
        mss = mtu - KCPConstants.IKCP_OVERHEAD;
        rx_rto = KCPConstants.IKCP_RTO_DEF;
        rx_minrto = KCPConstants.IKCP_RTO_MIN;
        interval = KCPConstants.IKCP_INTERVAL;
        ts_flush = KCPConstants.IKCP_INTERVAL;
        ssthresh = KCPConstants.IKCP_INTERVAL;
        dead_link = KCPConstants.IKCP_DEALLINK;
        //默认缓冲区  3个mtu
        buffer = new byte[(int) (mtu + KCPConstants.IKCP_OVERHEAD) * 3];
    }



}
