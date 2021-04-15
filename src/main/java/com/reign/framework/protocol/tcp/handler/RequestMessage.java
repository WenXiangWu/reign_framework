package com.reign.framework.protocol.tcp.handler;

/**
 * @ClassName: RequestMessage
 * @Description: 请求消息
 * @Author: wuwx
 * @Date: 2021-04-14 18:36
 **/
public class RequestMessage {

    private int requestId;

    private byte[] content;

    private String command;

    private String sessionId;

    /**全局缓存key值*/
    private Object globalKeyValue;

    private long createTime;

    public RequestMessage() {
        this.createTime = System.currentTimeMillis();
    }


    public RequestMessage(int requestId, String command, byte[] content) {
        super();
        this.requestId = requestId;
        this.content = content;
        this.command = command;
        this.createTime= System.currentTimeMillis();
    }

    public int getRequestId() {
        return requestId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
