#KCP网络包头结构
传输层，可靠性，ARQ协议。

*目的：解决在网络拥堵情况下tcp协议的网络速度慢的问题。可靠性，传输速度。*

一般用udp作为下层传输协议，udp报文+控制头。
![avatar](com/reign/framework/protocol/udp/kcp/kcpexplain/kcp_layer.jpg)

传输数据大于mss(最大报文段)时，kcp将数据分片存储在多个kcp包(Segment)中。
kcp包头总共占用了24个字节。给sn分配了4个字节，可以不用考虑序号越界的问题。
![avatar](com/reign/framework/protocol/udp/kcp/kcpexplain/kcp-header.jpg)

```
1. conv :连接号。UDP是⽆连接的，conv⽤于表示来⾃于哪个客户端。对连接的⼀种替代, 因为有 conv , 所
以KCP也是⽀持多路复⽤的。
2. cmd :命令类型，只有四种
    (1)IKCP_CMD_PUSH 数据推送命令
    (2)IKCP_CMD_ACK 确认命令
    (3)IKCP_CMD_WASK 接收窗⼝⼤⼩询问命令
    (4)IKCP_CMD_WINS 接收窗⼝⼤⼩告知命令
    IKCP_CMD_PUSH 和 IKCP_CMD_ACK 关联
    IKCP_CMD_WASK 和 IKCP_CMD_WINS 关联

3. frg :分⽚，⽤户数据可能会被分成多个KCP包，发送出去,
在 xtaci/kcp-go 的实现中，这个字段始终为0，以及没有意义了, 详⻅issues/121
4. wnd :接收窗⼝⼤⼩，发送⽅的发送窗⼝不能超过接收⽅给出的数值, （其实是接收窗⼝的剩余⼤⼩，这个
⼤⼩是动态变化的)
5. ts : 时间序列
6. sn : 序列号
7. una :下⼀个可接收的序列号。其实就是确认号，收到sn=10的包，una为11
8. len :数据⻓度(DATA的⻓度)
9. data :⽤户数据
```

##一、KCP工作流程

![avatar](com/reign/framework/protocol/udp/kcp/kcpexplain/kcp1.png)

![avatar](com/reign/framework/protocol/udp/kcp/kcpexplain/kcp2.jpg)
```$xslt
1. conv:会话id
2. cmd：命令，如
(1)IKCP_CMD_ACK确认命令，
(2) IKCP_CMD_WASK接收窗口大小询问命令；
3. frg：分片，用户数据可能会被分为多个KCP包发送出去
4. ts:时间序列
5. sn:序列号
6. una:下一个可接收的序列号，其实就是确认好，收到sn =10的包，una为11
7. len：数据长度
8. data：用户数据

数据先进入发送队列snd_queue，在进入发送缓存snd_buf，为窗口
```
http://192.144.167.243/blog/93/

###1.发送数据

kcp首先将要发送的数据存到kcp->buffer中，如果需要发送的数据总量的大小大于kcp->mtu，则将buffer中的数据调用output函数发送出去，output函数由用户传入。

####kcp数据包发送顺序：
```
IKCP_CMD_PUSH : 传输的数据包
IKCP_CMD_ACK : ACK包，类似于 TCP中的 ACK，通知对⽅收到了哪些包
IKCP_CMD_WASK : ⽤来探测远端窗⼝⼤⼩
IKCP_CMD_WINS : 告诉对⽅⾃⼰窗⼝⼤⼩
```

在发送PUSH类型的数据时，首先需要将数据从sen_que移动到sen_buf中（在移动时会检测拥塞窗口的大小，sen_que可以理解为发送数据的缓冲队列）。

*kcp在发送sen_buf队列中的数据时会检测是否是第一次发送：*

1. 如果该segment的发送次数为0，则直接发送。
2. 如果发送次数大于0并且已经超时，则再次发送并调整rto和下次超时时间
3. 如果没有超时但是达到了快速重传的条件（被跳过了几个包），则发送并且更新下次超时时间。



##二、函数解析
###Input
   ```
int ikcp_input(ikcpcb *kcp, const char *data, long size)
```
处理接收到的数据，data即用户传入的数据。kcp不负责网络数据的接收，需要用户将接收到的数据传入。

在接收到数据后，解析数据得到segment的sn，una等数据包头信息。首先根据una清除掉kcp->snd_buf中已经确认接收到的segment（una即表示该seg之前数据包均已收到），随后根据kcp->sen_buf更新kcp->snd_una。

1. ack: IKCP_CMD_ACK，首先根据该segment的rtt更新计算kcp的rtt和rto，删除掉kcp->snd_buf中相应的segment，更新kcp的sed_una(下一个未确认的segment)。
2. push:IKCP_CMD_PUSH，收到push segment后需要发送ack，将该segment的sn和ts放入kcp->acklist中。 如果该seg符合滑动窗口的范围，则将该segment放入kcp->rev_buf中。 如果kcp->queue的大小小于kcp->rev_wnd(滑动窗口的大小)，则将kcp->rev_buf符合条件的segment放入kcp->rcv_queue中(保证序号连续的seg)。
3. window ask: IKCP_CMD_WASK,将kcp->probe中相应位置为1，发送segment时向远端发送相应接收窗口大小。
4. IKCP_CMD_WINS，对应远端的窗口更新包，无需做额外的操作。

随后遍历kcp->sed_buf，更新每个segment的seg->fastack(被跳过的次数，用于判断是否需要快速重传)。

如果远端接收状态有更新，则更新本地拥塞窗口的大小。

###Receive
```$xslt
int ikcp_recv(ikcpcb *kcp, char *buffer, int len)
```
用户层面的数据读取，从rcv_queue中读取一个packet（如果该packet在发送前分段的话，则将fragement合并后放入buffer中）。input的操作保证了rcv_queue中的segment都是有序且连续的。

随后如果rcv_queue大小小于rcv_wnd（接收窗口）的大小，则将rcv_buf中合适的segment放入rcv_queue中。

如果需要告知远端主机窗口大小，则

```$xslt
kcp->probe |= IKCP_ASK_TELL
```
将ICKP_ASK_TELL置为1。





























