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
    //连接号。UDP是⽆连接的，conv⽤于表示来⾃于哪个客户端。对连接的⼀种替代, 因为有 conv , 所以KCP也是⽀持多路复⽤的。
    private long conv = 0;

    //可⻅ snd_una 和 snd_nxt 之间的橘⾊区域就是所有没有被确认的Segment(粗略的认为)。这部分
    //Segment⽬前正在发送端和接收端之间的链路上”⻜”，为了避免链路拥塞，KCP希望限制这部分的区域应该⼩于 cwnd

    //第⼀个未确认的包
    private long snd_una = 0;
    //下⼀个要发送的包的序号；
    private long snd_nxt = 0;
    //待接收消息序号。为了保证包的顺序，接收⽅会维护⼀个接收窗⼝，接收窗⼝有⼀个起始序号rcv_nxt（待接收消息序号）以及尾序号 rcv_nxt + rcv_wnd（接收窗⼝⼤⼩）；
    private long rcv_nxt = 0;


    //TCP 跟踪下一个 ACK 中将要发送的时间戳的值（tsrecent 变量）以及最后发送的 ACK 中的确认序号（lastack 变量）。这个序号就是接收方期望的序号。
    //当一个包含有字节号 lastack 的报文段到达时，则该报文段中的时间戳被保存在 tsrecent 中。
    //无论何时发送一个时间戳选项，tsrecent 就作为时间戳回显应答字段被发送，而序号字段被保存在 lastack 中。


    /****--------时间序列相关 time sequence-------------*/
    //TCP 跟踪下一个 ACK 中将要发送的时间戳的值
    private long ts_recent = 0;
    //最后发送的 ACK 中的确认序号
    private long ts_lastack = 0;
    //下次探查窗口的时间戳
    private long ts_probe = 0;
    //探查窗口需要等待的时间
    private long probe_wait = 0;


    /****----------窗口相关参数--------------**/
    //发送窗口大小
    private long snd_wnd = 0;
    //接收窗⼝⼤⼩，发送⽅的发送窗⼝不能超过接收⽅给出的数值, （其实是接收窗⼝的剩余⼤⼩，这个⼤⼩是动态变化的)
    private long rcv_wnd = 0;
    //远端接收窗口大小
    private long rmt_wnd = 0;
    //拥塞窗口，表示发送方可发送多少个KCP数据包。与接收方窗口有关，与网络状况（拥塞控制）有关，与发送窗口大小有关。
    private long cwnd = 0;
    //可发送的最大数据量
    private long incr = 0;
    //探查变量
    private long probe = 0;
    //最大传输单元
    private int mtu = 0;
    //最大分片大小
    private int mss = 0;
    //是否采用流传输模式
    private boolean stream;
    //储存消息字节流的内存,默认缓冲区，3个mtu
    private byte[] buffer = null;

    //接收消息的队列
    private ArrayList<Segment> rcv_que = new ArrayList<>(128);
    //接收消息的缓存
    private ArrayList<Segment> rcv_buf = new ArrayList<>(128);

    //发送消息的队列
    private ArrayList<Segment> snd_que = new ArrayList<>(128);
    //发送消息的缓存
    private ArrayList<Segment> snd_buf = new ArrayList<>(128);


    //待发送的ack的列表
    private ArrayList<Long> ackList = new ArrayList<>(256);

    //连接状态
    private long state = 0;
    //acklist的大小
    private long ackblock = 0;
    //ack数量
    private long ackcount = 0;
    //ack接收rtt平滑值(smoothed)
    private long rx_srtt = 0;
    //ack接收rtt浮动值
    private long rx_rttval = 0;
    //由ack接收延迟计算出来的复原时间
    private long rx_rto = 0;
    //不管是 TCP还是 KCP计算 RTO时都有最小 RTO的限制，即便计算出来RTO为40ms，由于默认的 RTO是100ms，协议只有在100ms后才能检测到丢包，快速模式下为30ms，可以手动更改该值：kcp->rx_minrto = 10;
    private long rx_minrto = 0;
    //当前的时间戳
    private long current = 0;
    //内部flush刷新间隔
    private long interval = 0;
    //下次flush刷新时间戳
    private long ts_flush = 0;
    //是否启动无延迟模式
    private long nodelay = 0;
    //是否调用过update函数的标识(kcp需要上层通过不断的ikcp_update和ikcp_check来驱动kcp的收发过程)
    private long updated = 0;
    //拥塞窗⼝阈值，以包为单位（TCP以字节为单位）；
    private long ssthresh = 0;
    //触发快速重传的重复ack个数
    private long fastresend = 0;
    //取消拥塞控制
    private long nocwnd = 0;
    //发送segment的次数，当segment的xmit增加时，xmit增加（第⼀次或重传除外）；
    private long xmit = 0;
    //最大重传次数
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


    public int send(byte[] buffer, int dataLen) {
        if (0 == dataLen) {
            return -1;
        }
        //stream 模式
        int offset = 0;
        if (stream && snd_que.size() > 0) {
            Segment seg = snd_que.get(snd_que.size() - 1);
            if (seg.data != null && seg.data.length < mss) {
                int capacity = mss - seg.data.length;
                int extend = (seg.data.length < capacity) ? seg.data.length : capacity;

                Segment newseg = new Segment(extend + seg.data.length);
                newseg.frg = 0;
                System.arraycopy(seg.data, 0, newseg.data, 0, seg.data.length);
                System.arraycopy(buffer, 0, newseg.data, seg.data.length, extend);
                offset += extend;

                snd_que.set(snd_que.size() - 1, newseg);

            }
            if (offset >= dataLen) return 0;
        }

        //计算mtu个数
        int count;
        if (dataLen < mss) {
            count = 1;
        } else {
            count = (dataLen + mss - 1) / mss;
        }
        if (count > 255) {
            return -2;
        }
        if (count == 0) {
            count = 1;
        }

        //划分mtu
        for (int i = 0; i < count; i++) {
            int size = dataLen > mss ? mss : dataLen;
            Segment seg = new Segment(size);
            System.arraycopy(buffer, offset, seg.data, 0, size);
            offset += size;
            seg.frg = (this.stream) ? 0 : count - i - 1;
            snd_que.add(seg);
            dataLen -= size;
        }
        return 0;

    }

    /**
     * 计算接收队列中有多少可用的数据  分片的编号排列  3,2,1,0;
     *
     * @return
     */
    public int peekSize() {
        if (0 == rcv_que.size()) {
            return -1;
        }

        Segment segment = rcv_que.get(0);
        if (0 == segment.frg) {
            //如果数据没有分片，即该数据只用一个segment传输
            return segment.data.length;
        }

        //分片未全部到达
        if (rcv_que.size() < segment.frg + 1) {
            return -1;
        }

        //数据被分片了，到这里说明分片全部到齐了，计算一个合包的大小
        int length = 0;
        for (Segment item : rcv_que) {
            length += item.data.length;
            //说明这是最后一个分片 TODO 如何确保分片全都到达了;
            if (0 == item.frg) {
                break;
            }
        }
        return length;
    }


    /**
     * 将接受队列中的数据传递给上层引用
     *
     * @return
     */
    public int recv() {
        //接受队列没有数据
        if (rcv_que.size() == 0) {
            return -1;
        }

        //包没有来齐
        int peekSize = peekSize();
        if (peekSize < 0) {
            return -2;
        }

        //接受缓冲区长度不够
        byte[] buffer = new byte[peekSize];
        if (peekSize > buffer.length) {
            return -3;
        }

        //判断是否启用快速恢复模式
        //接受队列长度大于远程滑动窗口
        boolean recover = false;
        if (rcv_que.size() >= rcv_wnd) {
            recover = true;
        }

        //合并包
        int count = 0;
        int dataLen = 0;
        for (Segment segment : rcv_que) {
            System.arraycopy(segment.data, 0, buffer, dataLen, segment.data.length);
            dataLen += segment.data.length;
            count++;
            if (log.isDebugEnabled()) {
                log.debug("recv sn :{}", segment.sn);
            }
            if (0 == segment.frg) {
                break;
            }
        }

        if (dataLen != peekSize) {
            throw new RuntimeException("peek data error,except len:" + peekSize + ",real len:" + dataLen);
        }

        if (count > 0) {
            KCPHelper.slice(rcv_que, count, rcv_que.size());
        }

        //将可用数据从rcv_buf 移动到rcv_queue，交给上层处理
        count = 0;
        for (Segment seg : rcv_buf) {
            if (seg.sn == rcv_nxt && rcv_que.size() < rcv_wnd) {
                rcv_que.add(seg);
                rcv_nxt++;
                count++;
            } else {
                break;
            }
        }

        if (count > 0) {
            KCPHelper.slice(rcv_buf, count, rcv_buf.size());
        }

        //快恢复
        if (rcv_que.size() < rcv_wnd && recover) {
            //准备回复 IKCP_CMD_WINDS in  ikcp_flush,告诉远端服务器自己的窗口大小
            probe |= KCPConstants.IKCP_ASK_TELL;
        }
        //接受数据
        output.handleReceiveData(this, buffer);
        return dataLen;
    }


    /**
     * 通过对端传回的una 将已经确认发送成功包从发送缓存中移除; 发送端 滑动窗口右移；  从窗口中(发送缓存)移除已经ack的数据包
     *
     * @param una
     */
    private void parse_una(long una) {
        int count = 0;
        for (Segment segment : snd_buf) {
            if (KCPHelper._itimediff(una, segment.sn) > 0) {
                count++;
            } else {
                break;
            }

        }
        if (0 < count) {
            KCPHelper.slice(snd_buf, count, snd_buf.size());
        }
    }


    private void ack_push(long sn, long ts) {
        //C语言原版中将 *2扩大容量，java不用
        ackList.add(sn);
        ackList.add(ts);
    }


    /**
     * 解析用户数据包
     * 1.判断收到的包编号是否落在滑动窗口内，不是则丢弃
     * 2.判断收到的包编号是否重复，重复则丢弃
     * 3.将该包按照包编号插入到接收缓存滑动窗口合适的位置
     * 4.滑动接收方窗口；判断接收缓存中是否有完整的包，有则将其从接受缓存中移动到接收对列中，并将这部分数据从接受缓存中删除
     *
     * @param newSeg
     */
    private void parse_data(Segment newSeg) {
        long sn = newSeg.sn;

        //包编号不落在滑动窗口范围内，或者是我不需要的包
        if (KCPHelper._itimediff(sn, rcv_nxt + rcv_wnd) >= 0 || KCPHelper._itimediff(sn, rcv_nxt) < 0) {
            return;
        }

        int n = rcv_buf.size() - 1;
        int after_idx = -1;
        boolean repeat = false;

        //判断是否是重复报，并且计算插入位置
        for (int i = n; i >= 0; i--) {
            Segment segment = rcv_buf.get(i);
            if (segment.sn == sn) {
                //包编号重复了，需要丢弃
                repeat = true;
                break;
            }

            //在队列中找到某个包编号正好比收到的包编号大的 包编号的位置    4  5  7  8  9   收到的包编号为6 ，则插在5和7之间
            if (KCPHelper._itimediff(sn, segment.sn) > 0) {
                after_idx = i;
                break;
            }
        }


        //如果不是重复包，则插入
        if (!repeat) {
            if (after_idx == -1) {
                rcv_buf.add(0, newSeg);
            } else {
                rcv_buf.add(after_idx + 1, newSeg);
            }
        }

        //从接受缓存中将连续包加入到接收队列; 滑动接收方的窗口
        int count = 0;
        for (Segment segment : rcv_buf) {
            if (segment.sn == rcv_nxt && rcv_que.size() < rcv_wnd) {
                rcv_que.add(segment);
                //接收方滑动窗口右移
                rcv_nxt++;
                count++;
            } else {
                break;
            }
        }

        //从接受缓存中移除
        if (count > 0) {
            KCPHelper.slice(rcv_buf, count, rcv_buf.size());
        }
    }


    /**
     * 底层收到包后调用，再由上层通过recv获得处理后的数据；
     * 接收到UDP包后的处理
     *
     * @param data
     * @param dataLen
     * @return
     */
    public int input(byte[] data, int dataLen) {
        //已发送的ack
        long s_una = snd_una;

        //空数据
        if (null == data) {
            if (log.isDebugEnabled()) {
                log.debug("data is null");
            }
            return -1;
        }

        //数据长度不足,KCP包体大小为24字节
        if (dataLen < KCPConstants.IKCP_OVERHEAD) {
            if (log.isDebugEnabled()) {
                log.debug("input data length not enough, expect:{} real:{}" + KCPConstants.IKCP_OVERHEAD, dataLen);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("input {} bytes", dataLen);
        }

        //解析包
        int offset = 0;
        while (true) {
            long ts, sn, length, una, conv_;
            int wnd;
            byte cmd, frg;

            //长度不足包头长度
            if (dataLen - offset < KCPConstants.IKCP_OVERHEAD) {
                break;
            }

            //解析conv标识，即会话标识;  占 4个字节，32 位
            conv_ = KCPHelper.ikcp_decode32u(data, offset);
            offset += 4;

            //会话标识不同
            if (conv_ != conv) {
                return -1;
            }

            //解包， 正常情况下包头 为 24 字节，offset记录的就是包头解析过程中累计的字节数
            //cmd命令类型， 占据 1 个字节，8位
            cmd = KCPHelper.ikcp_decode8u(data, offset);
            offset += 1;
            //frg 分片编号，占据 1 个字节， 8 位
            frg = KCPHelper.ikcp_decode8u(data, offset);
            offset += 1;
            //wnd 对端窗口大小，占据 2 个字节， 16位
            wnd = KCPHelper.ikcp_decode16u(data, offset);
            offset += 2;
            //ts 发送时间， 占据 4 个字节，32 位
            ts = KCPHelper.ikcp_decode32u(data, offset);
            offset += 4;
            //sn 包序列号，占据 4 个字节， 32 位
            sn = KCPHelper.ikcp_decode32u(data, offset);
            offset += 4;
            //una :下⼀个可接收的序列号。其实就是确认号，收到sn=10的包，una为11  占据 4 个字节， 32 位
            una = KCPHelper.ikcp_decode32u(data, offset);
            offset += 4;
            //length : 包长度，  占据 4 个字节， 32 位
            length = KCPHelper.ikcp_decode32u(data, offset);
            offset += 4;


            //数据包不全; 如果整个包长度减去 包头长度 ，剩余的就是包体长度；    如果该长度小于 length，即包实际长度记录，表明包不全
            if (dataLen - offset < length) {
                return -2;
            }

            //判断指令是否合法
            if (cmd != KCPConstants.IKCP_CMD_ACK && cmd != KCPConstants.IKCP_CMD_PUSH && cmd != KCPConstants.IKCP_CMD_WASK && cmd != KCPConstants.IKCP_CMD_WINS) {
                return -3;
            }


            //更新远端滑动窗口大小
            rmt_wnd = (long) wnd;

            //更新una
            parse_una(una);

            //更新自己的ack
            shrink_buf();

            //1.如果收到的是ACK回复包
            if (KCPConstants.IKCP_CMD_ACK == cmd) {
                if (KCPHelper._itimediff(current, ts) >= 0) {
                    //根据ack，调整rto
                    update_ack(KCPHelper._itimediff(current, ts));
                }
                parse_ack(sn);
                shrink_buf();
                if (log.isDebugEnabled()) {
                    log.debug("input ack: sn:{} ts:{} una:{} rtt:{} rto:{}", sn, ts, una, KCPHelper._itimediff(current, ts), rx_rto);
                }

            } else if (KCPConstants.IKCP_CMD_PUSH == cmd) {
                //2.如果收到的是传输的数据包
                if (log.isDebugEnabled()) {
                    log.debug("input push: sn:{} ts:{} una:{}", sn, ts, una);
                }

                //数据包编号落在滑动窗口左侧
                if (KCPHelper._itimediff(sn, rcv_nxt + rcv_wnd) < 0) {
                    //push一个ack
                    ack_push(sn, ts);
                    //数据落在滑动窗口中
                    if (KCPHelper._itimediff(sn, rcv_nxt) >= 0) {
                        Segment seg = new Segment((int) length);
                        seg.conv = conv_;
                        seg.cmd = cmd;
                        seg.frg = frg;
                        seg.wnd = wnd;
                        seg.ts = ts;
                        seg.sn = sn;
                        seg.una = una;
                        //将数据copy 到新的Segment中
                        if (length > 0) {
                            System.arraycopy(data, offset, seg.data, 0, (int) length);
                        }
                        parse_data(seg);
                    }
                }

            } else if (KCPConstants.IKCP_CMD_WASK == cmd) {
                //3.⽤来探测远端窗⼝⼤⼩， 这里是对面发请求给我探测我的窗口大小
                //准备向对端回复IKCP_CMD_WINS ，告诉对面我的窗口大小
                probe |= KCPConstants.IKCP_ASK_TELL;

                if (log.isDebugEnabled()) {
                    log.debug("input probs: sn:{} ts:{} una:{}", sn, ts, una);
                }
            } else if (KCPConstants.IKCP_CMD_WINS == cmd) {
                //4.对端告诉我它的窗口大小
                //啥都不干，因为前面已经更新了它的窗口大小
                if (log.isDebugEnabled()) {
                    log.debug("input wins: sn:{} ts:{} una:{} wnd:{}", sn, ts, una, wnd);
                }
            } else {
                //5.cmd命令格式错误
                return -3;
            }
            offset += (int) length;
        }

        //snd_una 更新了，TODO 不知道做什么
        if (KCPHelper._itimediff(snd_una, s_una) > 0) {
            //自己的滑动窗口小于远端的
            if (cwnd < rmt_wnd) {
                long mss_ = mss;
                if (cwnd < ssthresh) {
                    cwnd++;
                    incr += mss_;
                } else {
                    if (incr < mss_) {
                        incr = mss_;
                    }
                    incr += (mss_ * mss_) / incr + (mss_ / 16);
                    if ((cwnd + 1) * mss_ <= incr) {
                        cwnd++;
                    }
                }
                if (cwnd > rmt_wnd) {
                    cwnd = rmt_wnd;
                    incr = rmt_wnd * mss_;
                }
            }
        }
        return 0;
    }

    /**
     * 对端返回的ack，确认发送成功时，对应包从发送缓存中删除
     *
     * @param sn
     */
    private void parse_ack(long sn) {
        if (KCPHelper._itimediff(sn, snd_una) < 0 || KCPHelper._itimediff(sn, snd_nxt) >= 0) {
            return;
        }
        int index = 0;
        for (Segment segment : snd_buf) {
            if (sn == segment.sn) {
                snd_buf.remove(index);
                break;
            } else {
                //收到后面的包ack，之前的包的 faskack++;
                segment.fastack++;
            }
            index++;
        }
    }

    /**
     * 根据ack返回，更新rto
     * rtt：一次ack的来回时间
     *
     * @param rtt
     */
    private void update_ack(int rtt) {
        if (0 == rx_srtt) {
            rx_srtt = rtt;
            rx_rttval = rtt / 2;
        } else {
            //和上一次对比
            int delta = (int) (rtt - rx_srtt);
            if (delta < 0) {
                delta = -delta;
            }
            rx_rttval = (3 * rx_rttval + delta) / 4;
            rx_rttval = (3 * rx_srtt + rtt) / 8;
            if (rx_srtt < 1) {
                rx_srtt = 1;
            }
        }
        int rto = (int) (rx_srtt + KCPHelper._imax_(interval, 4 * rx_rttval));
        rx_rto = KCPHelper._ibound_(rx_minrto, rto, KCPConstants.IKCP_RTO_MAX);
    }


    /**
     * 接收窗口可用大小
     *
     * @return
     */
    private int wnd_unused() {
        if (rcv_que.size() < rcv_wnd) {
            return (int) rcv_wnd - rcv_que.size();
        }
        return 0;
    }


    /**
     * flush pending data
     */
    private void flush() {
        long current_ = current;
        byte[] buffer_ = buffer;
        int change = 0;
        int lost = 0;

        if (0 == updated) {
            return;
        }

        Segment seg = new Segment(0);
        seg.conv = conv;
        seg.cmd = KCPConstants.IKCP_CMD_ACK;
        seg.wnd = (long) wnd_unused();
        seg.una = rcv_nxt;

        //发送ack；flush ack
        int count = ackList.size() / 2;
        int offset = 0;
        for (int i = 0; i < count; i++) {
            if (offset + KCPConstants.IKCP_OVERHEAD > mtu) {
                output(buffer, offset);
                offset = 0;
            }

            //ikcp_ack_get
            seg.sn = ackList.get(i * 2 + 0);
            seg.ts = ackList.get(i * 2 + 1);
            offset += seg.encode(buffer, offset);
        }
        ackList.clear();

        //probe window size (if remote window size equial zero)
        if (0 == rmt_wnd) {
            if (0 == probe_wait) {
                probe_wait = KCPConstants.IKCP_PROBE_INIT;
                ts_probe = current + probe_wait;
            } else {
                if (KCPHelper._itimediff(current, ts_probe) >= 0) {
                    if (probe_wait < KCPConstants.IKCP_PROBE_INIT) {
                        probe_wait = KCPConstants.IKCP_PROBE_INIT;
                    }
                    //TODO 猜测这里是超时重发 rto计算
                    probe_wait += probe_wait / 2;
                    if (probe_wait > KCPConstants.IKCP_PROBE_LIMIT) {
                        probe_wait = KCPConstants.IKCP_PROBE_LIMIT;
                    }
                    ts_probe = current + probe_wait;
                    probe |= KCPConstants.IKCP_ASK_SEND;
                }
            }
        } else {
            ts_probe = 0;
            probe_wait = 0;
        }

        //flush window probing commands
        if ((probe & KCPConstants.IKCP_ASK_SEND) != 0) {
            seg.cmd = KCPConstants.IKCP_CMD_WASK;
            if (offset + KCPConstants.IKCP_OVERHEAD > mtu) {
                output(buffer, offset);
                offset = 0;
            }
            offset += seg.encode(buffer, offset);
        }

        //flush window probing commands
        if ((probe & KCPConstants.IKCP_ASK_TELL) != 0) {
            seg.cmd = KCPConstants.IKCP_CMD_WINS;
            if (offset + KCPConstants.IKCP_OVERHEAD > mtu) {
                output(buffer, offset);
                offset = 0;
            }
            offset += seg.encode(buffer, offset);
        }
        probe = 0;

        //计算窗口大小
        long cwnd_ = KCPHelper._imin_(snd_wnd, rmt_wnd);
        if (0 == nocwnd) {
            cwnd_ = KCPHelper._imin_(cwnd, cwnd_);
        }

        count = 0;
        for (Segment nsnd_quel : snd_que) {
            if (KCPHelper._itimediff(snd_nxt, snd_una + cwnd_) >= 0) {
                break;
            }
            Segment newSeg = nsnd_quel;
            newSeg.conv = conv;
            newSeg.cmd = KCPConstants.IKCP_CMD_PUSH;
            newSeg.wnd = seg.wnd;
            newSeg.ts = current_;
            newSeg.sn = snd_nxt;
            newSeg.una = rcv_nxt;
            newSeg.resendts = current_;
            newSeg.rto = rx_rto;
            newSeg.fastack = 0;
            newSeg.xmit = 0;
            snd_buf.add(newSeg);
            snd_nxt++;
            count++;
        }

        if (0 < count) {
            KCPHelper.slice(snd_que, count, snd_que.size());
        }

        //calculate resent
        long resent = (fastresend > 0) ? fastresend : 0xffffffff;
        long rtomin = (nodelay == 0) ? (rx_rto >> 3) : 0;

        //flush data segments
        for (Segment segment : snd_buf) {
            boolean needsend = false;
            if (0 == segment.xmit) {
                needsend = true;
                segment.xmit++;
                segment.rto = rx_rto;
                segment.resendts = current_ + segment.rto + rtomin;
            } else if (KCPHelper._itimediff(current_, segment.resendts) >= 0) {
                needsend = true;
                segment.xmit++;
                xmit++;
                if (0 == nodelay) {
                    segment.rto += rx_rto;
                } else {
                    segment.rto += rx_rto / 2;
                }
                segment.resendts = current_ + segment.rto;
                lost = 1;
            } else if (segment.fastack >= resent) {
                needsend = true;
                segment.xmit++;
                segment.fastack = 0;
                segment.resendts = current_ + segment.rto;
                change++;
            }

            if (needsend) {
                segment.ts = current_;
                segment.wnd = seg.wnd;
                segment.una = rcv_nxt;

                int need = KCPConstants.IKCP_OVERHEAD + segment.data.length;
                if (offset + need > mtu) {
                    output(buffer, offset);
                    offset = 0;
                }

                offset += segment.encode(buffer, offset);
                if (segment.data.length > 0) {
                    System.arraycopy(segment.data, 0, buffer, offset, segment.data.length);
                    offset += segment.data.length;
                }

                if (segment.xmit >= dead_link) {
                    state = -1; //state = 0(c#)
                }
            }
        }

        // flush remain segments
        if (offset > 0) {
            output(buffer, offset);
        }

        //update ssthresh
        if (change != 0) {
            long inflight = snd_nxt - snd_una;
            ssthresh = inflight / 2;
            if (ssthresh < KCPConstants.IKCP_THRESH_MIN) {
                ssthresh = KCPConstants.IKCP_THRESH_MIN;
            }
            cwnd = ssthresh + resent;
            incr = cwnd * mss;
        }

        if (lost != 0) {
            ssthresh = cwnd / 2;
            if (ssthresh < KCPConstants.IKCP_THRESH_MIN) {
                ssthresh = KCPConstants.IKCP_THRESH_MIN;
            }
            cwnd = 1;
            incr = mss;
        }
        if (cwnd < 1) {
            cwnd = 1;
            incr = mss;
        }
    }

    /**
     * update after (call it repeatedly,every 10ms - 100ms),or you can ask ikcp_check when to call it again
     * (without ikcp_input/_send calling)
     * 'current' - current timestamp in millisec
     *
     * @param current_
     */
    public void update(long current_) {
        //更新当前时间戳
        current = current_;
        //是否做过更新
        if (0 == updated) {
            updated = 1;
            ts_flush = current;
        }

        int slap = KCPHelper._itimediff(current, ts_flush);

        //偏差在10s~ -10s
        if (slap >= 10 * 1000 || slap < -10 * 1000) {
            ts_flush = current;
            slap = 0;
        }

        //有一定间隔了
        if (slap >= 0) {
            ts_flush += interval;
            if (KCPHelper._itimediff(current, ts_flush) >= 0) {
                ts_flush = current + interval;
            }
            flush();
        }
    }

    /**
     * 决定何时应该调用ikcp_update,返回当你应该调用的毫秒数
     * 如果这里没有ikcp_input或者_send调用，可以调用update;
     *
     * @param current_
     * @return
     */
    public long check(long current_) {
        long ts_flush_ = ts_flush;
        long tm_flush;
        long tm_packet = 0x7fffffff;
        long minimal;

        if (0 == updated) {
            return current_;
        }
        if (KCPHelper._itimediff(current_, ts_flush_) >= 10 * 1000 || KCPHelper._itimediff(current_, ts_flush_) < -10 * 1000) {
            ts_flush_ = current_;
        }
        if (KCPHelper._itimediff(current_, ts_flush) >= 0) {
            return current_;
        }
        tm_flush = KCPHelper._itimediff(ts_flush_, current_);
        for (Segment seg : snd_buf) {
            int diff = KCPHelper._itimediff(seg.resendts, current_);
            if (diff <= 0) {
                return current_;
            }
            if (diff < tm_packet) {
                tm_packet = diff;
            }
        }

        minimal = tm_packet < tm_flush ? tm_packet : tm_flush;
        if (minimal >= interval) {
            minimal = interval;
        }
        return current_ + minimal;
    }

    /**
     * 设置mtu 大小，默认 1400
     *
     * @param mtu_
     * @return
     */
    public int setMtu(int mtu_) {
        //如果小于最小mtu或者小于包头大小24，则不合法
        if (mtu_ < 50 || mtu_ < KCPConstants.IKCP_OVERHEAD) {
            return -1;
        }
        byte[] buffer_ = new byte[mtu_ + KCPConstants.IKCP_OVERHEAD * 3];
        mtu = mtu_;
        mss = mtu - KCPConstants.IKCP_OVERHEAD;
        buffer = buffer_;
        return 0;
    }


    /**
     * 设置间隔
     *
     * @param interval_
     * @return
     */
    public int interval(int interval_) {
        if (interval_ > 5000) {
            interval_ = 5000;
        } else if (interval_ < 10) {
            interval_ = 2;
        }
        interval = (long) interval_;
        return 0;
    }


    /**
     * @param nodelay_  1 开启； 0 禁用
     * @param interval_ interval update timer interval in millisec,默认100ms
     * @param resend_   1 启动快速重传； 0 关闭快速重传
     * @param nc_       0 normal congestion control, 默认；     1：disable congestion control
     * @return
     */
    public int noDelay(int nodelay_, int interval_, int resend_, int nc_) {
        if (nodelay_ > 0) {
            nodelay = nodelay_;
            if (nodelay_ != 0) {
                rx_minrto = KCPConstants.IKCP_RTO_NDL;
            } else {
                rx_minrto = KCPConstants.IKCP_RTO_MIN;
            }
        }
        if (interval_ >= 0) {
            if (interval_ > 5000) {
                interval_ = 5000;
            } else if (interval_ < 10) {
                interval_ = 2;
            }
            interval = interval_;
        }
        if (resend_ >= 0) {
            fastresend = resend_;
        }
        if (nc_ >= 0) {
            nocwnd = nc_;
        }
        return 0;

    }

    /**
     * 设置最大窗口大小， 默认sndwnd = 32,rcvwnd=32
     *
     * @param sndwnd 发送窗口大小
     * @param rcvwnd 接收窗口大小
     * @return
     */
    public int wndSize(int sndwnd, int rcvwnd) {
        if (sndwnd > 0) {
            snd_wnd = (long) sndwnd;
        }

        if (rcvwnd > 0) {
            rcv_wnd = (long) rcvwnd;
        }
        return 0;
    }

    /**
     * 获取待发送的包个数
     *
     * @return
     */
    public int waitSnd() {
        return snd_buf.size() + snd_que.size();
    }

    /**
     * 发送数据
     *
     * @param buffer
     * @param size
     */
    private void output(byte[] buffer, int size) {
        if (log.isDebugEnabled()) {
            log.debug("[RO] {} bytes", size);
        }
        output.handleWriteData(this, buffer, size);
        //需具体实现
    }

    /**
     * 获取user
     *
     * @return
     */
    public Object getUser() {
        return user;
    }

    /**
     * 获取会话id
     *
     * @return
     */
    public long getConv() {
        return conv;
    }

    /**
     * 计算本地真实snd_una, snd_una就是发送队列中第一个未确认ack的包sn
     */
    private void shrink_buf() {
        if (snd_buf.size() > 0) {
            snd_una = snd_buf.get(0).sn;
        } else {
            snd_una = snd_nxt;
        }
    }

}
