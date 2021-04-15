package com.reign.framework.protocol.udp.kcp;

/**
 * @ClassName: Segment
 * @Description: 一个UDP包
 * @Author: wuwx
 * @Date: 2021-04-15 16:56
 **/
public class Segment {
    //conv
    public long conv = 0;
    //包的指令
    public long cmd = 0;
    //分片标识
    public long frg = 0;
    //自己的接收滑动窗口大小（接收滑动窗口大小（rev_wnd） - 已接收队列大小nrcv_que.size()）
    public long wnd = 0;
    //发送时间戳
    public long ts = 0;
    //包的编号
    public long sn = 0;
    //此前编号的包均已收到
    public long una = 0;
    //预计开始传输时间
    public long resendts = 0;
    //重传超时时间
    public long rto = 0;

    public long fastack = 0;
    public long xmit = 0;
    public byte[] data;

    public Segment(int size) {
        this.data = new byte[size];
    }

    /**
     * encode a segment into buffer ;将一个udp包编码到buffer
     *
     * @param ptr
     * @param offset
     * @return
     */
    public int encode(byte[] ptr, int offset) {
        int offset_ = offset;
        KCPHelper.ikcp_encode32u(ptr, offset, conv);
        offset += 4;
        KCPHelper.ikcp_encode8u(ptr, offset, (byte) cmd);
        offset += 1;
        KCPHelper.ikcp_encode8u(ptr, offset, (byte) frg);
        offset += 1;
        KCPHelper.ikcp_encode16u(ptr, offset, (byte) wnd);
        offset += 2;

        KCPHelper.ikcp_encode32u(ptr, offset, ts);
        offset += 4;
        KCPHelper.ikcp_encode32u(ptr, offset, sn);
        offset += 4;
        KCPHelper.ikcp_encode32u(ptr, offset, una);
        offset += 4;
        KCPHelper.ikcp_encode32u(ptr, offset, (long) data.length);
        offset += 4;

        return offset - offset_;
    }
}
