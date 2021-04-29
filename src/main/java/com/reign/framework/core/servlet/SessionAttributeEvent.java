package com.reign.framework.core.servlet;

/**
 * @ClassName: SessionAttributeEvent
 * @Description: 属性事件
 * @Author: wuwx
 * @Date: 2021-04-15 12:08
 **/
public class SessionAttributeEvent extends SessionEvent {


    public String key;

    public Object value;

    public SessionAttributeEvent(String key, Object value, Session session) {
        super(session);
        this.key = key;
        this.value = value;
    }
}
