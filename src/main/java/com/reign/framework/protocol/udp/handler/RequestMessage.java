package com.reign.framework.protocol.udp.handler;

import com.reign.framework.protocol.udp.kcp.KCPNettyWrapper;

/**
 * @ClassName: RequestMessage
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 16:54
 **/
public class RequestMessage {

    private KCPNettyWrapper kcp;

    private int requestId;

    private byte[] content;

    private String command;

    private String sessionId;

    private Object globalKeyValue;

    private long createTime;


    public RequestMessage() {
    }

    public RequestMessage(KCPNettyWrapper kcp, int requestId, String command,byte[] content) {
        super();
        this.kcp = kcp;
        this.requestId = requestId;
        this.command = command;
        this.command =command;
        this.createTime = System.currentTimeMillis();
    }

    public KCPNettyWrapper getKcp() {
        return kcp;
    }

    public void setKcp(KCPNettyWrapper kcp) {
        this.kcp = kcp;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Object getGlobalKeyValue() {
        return globalKeyValue;
    }

    public void setGlobalKeyValue(Object globalKeyValue) {
        this.globalKeyValue = globalKeyValue;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
}

