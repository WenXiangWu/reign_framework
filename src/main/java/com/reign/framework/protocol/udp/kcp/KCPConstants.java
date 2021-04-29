package com.reign.framework.protocol.udp.kcp;

/**
 * @ClassName: KCPConstants
 * @Description: KCP常量定义
 * @Author: wuwx
 * @Date: 2021-04-15 16:55
 **/
public class KCPConstants {

    /***---------------------KCP超时重传设置--------------*/
    public static final int IKCP_RTO_NDL = 30; //no delay min rto
    public static final int IKCP_RTO_MIN = 100; // normal min rto
    public static final int IKCP_RTO_DEF = 200;
    //最大的重试时间
    public static final int IKCP_RTO_MAX = 60000;

    /***---------------------KCP命令类型，只有四种---------------------*/
    //传输的数据包
    public static final int IKCP_CMD_PUSH = 81;
    //ACK包，类似于 TCP中的 ACK，通知对⽅收到了哪些包
    public static final int IKCP_CMD_ACK = 82;
    //⽤来探测远端窗⼝⼤⼩
    public static final int IKCP_CMD_WASK = 83;
    //告诉对⽅⾃⼰窗⼝⼤⼩
    public static final int IKCP_CMD_WINS = 84;


    //询问对端我的窗口大小
    public static final int IKCP_ASK_SEND = 1;
    //告诉对面我的窗口大小
    public static final int IKCP_ASK_TELL = 2;
    public static final int IKCP_ASK_PAST = 3;

    /***---------------------滑动窗口参数---------------------*/
    public static final int IKCP_WND_SND = 32;
    public static final int IKCP_WND_RCV = 32;

    /***---------------------MTU每个数据包大小---------------------*/
    public static final int IKCP_MTU_DEF = 1400;

    /***---------------------更新周期---------------------*/
    public static final int IKCP_INTERVAL = 100;

    /***---------------------KCP包头大小---------------------*/
    public static final int IKCP_OVERHEAD  =24;

    public static final int IKCP_DEALLINK  =10;

    public static final int IKCP_THRESH_INIT  =2;
    public static final int IKCP_THRESH_MIN  =2;


    public static final int IKCP_PROBE_INIT  =7000; // 7secs to probe window size
    public static final int IKCP_PROBE_LIMIT = 120000; //up to 120 secs to probe window




}
