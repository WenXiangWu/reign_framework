package com.reign.framework.core.servlet;

/**
 * @ClassName: SessionEvent
 * @Description: session事件
 * @Author: wuwx
 * @Date: 2021-04-15 10:04
 **/
public class SessionEvent {

    public Session session;

    public SessionEvent(Session session) {
        this.session = session;
    }
}
