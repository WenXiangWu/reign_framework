package com.reign.framework.protocol.udp.kcp;

/**
 * @ClassName: Segment
 * @Description: 一个UDP包
 * @Author: wuwx
 * @Date: 2021-04-15 16:56
 **/
public class Segment {

    /**----------------------------包头结构-------------------------------*/
    //conv :连接号。UDP是⽆连接的，conv⽤于表示来⾃于哪个客户端。对连接的⼀种替代, 因为有 conv , 所以KCP也是⽀持多路复⽤的
    public long conv = 0;
    //cmd :命令类型，只有四种
    //    (1)IKCP_CMD_PUSH 数据推送命令
    //    (2)IKCP_CMD_ACK 确认命令
    //    (3)IKCP_CMD_WASK 接收窗⼝⼤⼩询问命令
    //    (4)IKCP_CMD_WINS 接收窗⼝⼤⼩告知命令
    //    IKCP_CMD_PUSH 和 IKCP_CMD_ACK 关联
    //    IKCP_CMD_WASK 和 IKCP_CMD_WINS 关联
    public long cmd = 0;
    //分⽚，⽤户数据可能会被分成多个KCP包，发送出去,
    public long frg = 0;
    //接收窗⼝⼤⼩，发送⽅的发送窗⼝不能超过接收⽅给出的数值, （其实是接收窗⼝的剩余⼤⼩，这个⼤⼩是动态变化的)
    public long wnd = 0;
    //发送时间戳
    public long ts = 0;
    //序列号
    public long sn = 0;
    //下⼀个可接收的序列号。其实就是确认号，收到sn=10的包，una为11； 此编号之前的包均已收到
    public long una = 0;
    //⽤户数据
    public byte[] data;

    // 包长  length




    //预计开始传输时间
    public long resendts = 0;
    //重传超时时间
    public long rto = 0;
    //是否开启快速重传
    public long fastack = 0;
    //发送segment的次数，当segment的xmit增加时，xmit增加（第⼀次或重传除外）；
    public long xmit = 0;

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
