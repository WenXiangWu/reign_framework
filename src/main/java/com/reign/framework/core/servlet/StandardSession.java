package com.reign.framework.core.servlet;

import com.alibaba.fastjson.JSON;
import com.reign.framework.common.ListenerConstants;
import com.reign.framework.common.util.Tuple;
import com.reign.framework.core.servlet.util.WrapperUtil;
import com.reign.framework.jdbc.orm.BinaryModel;
import com.reign.framework.util.ChannelBuffers;
import com.reign.framework.util.DateUtil;
import com.reign.framework.util.IChannelBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName: StandardSession
 * @Description: 标准session实现
 * @Author: wuwx
 * @Date: 2021-04-15 10:05
 **/
public class StandardSession implements Session, BinaryModel {

    private static enum Type {
        CREATE, DESTROY, ADD, REPLACE, REMOVE
    }

    public ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

    public String id;

    public long createTime;

    public volatile long lastAccessTime;

    public volatile boolean isValid = false;

    public volatile boolean expire = false;

    private volatile boolean discard = false;

    //积累的消息
    public List<Tuple<String, Object>> msgList = null;

    /**
     * 推送通道
     */
    public volatile Push push = null;

    /**
     * udp推送通道
     */
    public volatile Push udpPush = null;

    public List<SessionListener> sessionListeners;

    public List<SessionAttributeListener> sessionAttributeListeners;

    public ServletConfig servletConfig;

    private long sessionTimeOutMillis;

    private long sessionEmptyTimeOutMillis;

    private volatile long sessionInvalidateMillis;

    private Object lock = new Object();

    public StandardSession() {
    }

    public StandardSession(String id, List<SessionListener> sessionListeners, List<SessionAttributeListener> sessionAttributeListeners, ServletConfig servletConfig) {
        this.id = id;
        this.createTime = System.currentTimeMillis();
        this.lastAccessTime = System.currentTimeMillis();
        this.sessionListeners = sessionListeners;
        this.sessionAttributeListeners = sessionAttributeListeners;
        this.servletConfig = servletConfig;
        this.sessionTimeOutMillis = servletConfig.getSessionTimeoutMillis();
        this.sessionEmptyTimeOutMillis = servletConfig.getSessionEmptyTimeOutMillis();
        this.sessionInvalidateMillis = servletConfig.getSessionInvalidateMillis();

        //创建session，通知SessionListener
        notifyListener(sessionListeners, Type.CREATE);

    }

    /**
     * 构造函数，指定数据
     *
     * @param bytes
     */
    public StandardSession(byte[] bytes) {
        //TODO
        //IChannelBuffer channelBuffer = ChannelBuffers.

    }


    @Override
    public String getId() {
        return id;
    }

    @Override
    public Object getAttribute(String key) {
        return map.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        Object obj = map.get(key);
        //识别内容，自动表示是否合法
        if (ListenerConstants.PLAYER.equalsIgnoreCase(key)) {
            setValid(true);
        }
        if (null == obj) {
            //创建session
            notifyListener(sessionAttributeListeners, new SessionAttributeEvent(key, value, this), Type.ADD);
        } else {
            //创建session，通知sessionListener
            notifyListener(sessionAttributeListeners, new SessionAttributeEvent(key, obj, this), Type.REPLACE);

        }
    }


    private void notifyListener(List<SessionListener> sessionListeners, Type type) {
        for (SessionListener listener : sessionListeners) {
            switch (type) {
                case CREATE:
                    listener.sessionCreated(new SessionEvent(this));
                    break;
                case DESTROY:
                    listener.sessionDestroyed(new SessionEvent(this));
                    break;
                default:
                    break;
            }
        }
    }

    private void notifyListener(List<SessionAttributeListener> sessionAttributeListeners, SessionEvent event, Type type) {
        for (SessionAttributeListener listener : sessionAttributeListeners) {
            switch (type) {
                case ADD:
                    listener.attributeAdded((SessionAttributeEvent) event);
                    break;
                case REMOVE:
                    listener.attributeRemoveed((SessionAttributeEvent) event);
                    break;
                case REPLACE:
                    listener.attributeReplaced((SessionAttributeEvent) event);
                    break;
                default:
                    break;
            }

        }
    }

    @Override
    public boolean removeAttribute(String key) {
        Object obj = map.remove(key);
        //移除session，通知listener
        notifyListener(sessionAttributeListeners, new SessionAttributeEvent(key, obj, this), Type.REMOVE);
        return null != obj;
    }

    @Override
    public void invalidate() {
        if (discard) {
            discard();
            return;
        }
        //销毁session，通知listener
        try {
            notifyListener(sessionListeners, Type.DESTROY);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //清理历史消息
        if (null != msgList) {
            msgList.clear();
        }
        //丢失push通道
        if (null != push) {
            try {
                push.discard();
            } catch (Exception e) {
                //ignore
            }
        }

        //清空session内容
        GroupManager.getInstance.leave(id);
        //空session或者未被标记为合法的session
        if (map.size() == 0 || !isValid) {
            map.clear();
            map = null;
            SessionManager.getInstance().sessions.remove(id);
        }
    }

    @Override
    public void destroy() {
        map.clear();
        map = null;
        SessionManager.getInstance().sessions.remove(id);
    }

    private void discard() {
        try {
            notifyListener(sessionListeners, Type.DESTROY);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //清理历史消息
        if (null != msgList) {
            msgList.clear();
        }

        //丢失push通道
        if (null != push) {
            try {
                push.discard();
            } catch (Exception e) {
                //ignore
            }
        }

        //清空session内容
        map.clear();
        map = null;
        //TODO 是否考虑放到listener中
        GroupManager.getInstance.leave(id);
        SessionManager.getInstance().sessions.remove(id);
    }

    /**
     * 处理历史消息
     *
     * @param push
     */
    private void doHistoryMsg(Push push) {
        if (null != msgList && msgList.size() > 0) {
            synchronized (lock) {
                for (Tuple<String, Object> obj : msgList) {
                    if (obj.left != null) {
                        push.push(this, obj.left, (byte[]) obj.right);
                    } else {
                        push.push(this, obj.right);
                    }
                }
                msgList.clear();
            }
        }
    }


    @Override
    public void markDiscard() {
        this.discard = true;
    }

    @Override
    public void access() {
        this.lastAccessTime = System.currentTimeMillis();
    }

    private void addMsgToList(Tuple<String, Object> obj) {
        synchronized (lock) {
            if (null == msgList) {
                msgList = new ArrayList<>(SessionManager.MAX_HISTORY_MSG_LEN);
            }
            if (msgList.size() < SessionManager.MAX_HISTORY_MSG_LEN) {
                msgList.add(obj);
            }
        }
    }

    @Override
    public void setValid(boolean valid) {
        isValid = valid;
    }

    @Override
    public boolean isValid() {
        if (expire) return false;
        if (discard) return false;
        if (isEmpty() && (System.currentTimeMillis() - lastAccessTime) < sessionEmptyTimeOutMillis) {
            return true;
        } else if ((System.currentTimeMillis() - lastAccessTime) < sessionTimeOutMillis) {
            return true;
        } else {
            expire = true;
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return (null != getPush() && getPush().isPushable());
    }

    @Override
    public boolean isExpire() {
        return expire;
    }

    @Override
    public boolean isInvalidate() {
        if (!expire) {
            return false;
        }
        return (System.currentTimeMillis() - lastAccessTime) > sessionInvalidateMillis;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public void reActive() {
        if (!DateUtil.isSameDay(createTime, System.currentTimeMillis())) {
            this.sessionInvalidateMillis = servletConfig.getSessionNextDayInvalidateMillis();
        }
        lastAccessTime = System.currentTimeMillis();
        expire = false;
        discard = false;
    }

    @Override
    public void expire() {
        invalidate();
    }

    @Override
    public void setPush(Push push) {
        if (null!=this.push){
            this.push.clear();
        }
        this.push = push;
    }

    @Override
    public void setUDPPush(Push udpPush) {
        if (null!=this.udpPush){
            this.udpPush.clear();
        }
        this.udpPush = udpPush;
    }

    @Override
    public Push getPush() {
        return push;
    }

    @Override
    public Push getUDPPush() {
        return udpPush;
    }

    @Override
    public void push(String command, byte[] body) {
        Push push = this.push;
        if (null != push && push.isPushable()) {
            //处理历史消息
            doHistoryMsg(push);
            //推送消息
            push.push(this, command, body);
        } else {
            saveHistoryMsg(command, body);
        }
    }

    private void saveHistoryMsg(String command, byte[] body) {
        addMsgToList(new Tuple<>(command, body));
    }

    @Override
    public void push(Object buffer) {
        Push push = this.push;
        if (push != null && push.isPushable()) {
            //处理历史消息
            doHistoryMsg(push);
            //推送消息
            push.push(this, buffer);
        } else {
            saveHistoryMsg(WrapperUtil.newWrapper(buffer));
        }


    }

    private void saveHistoryMsg(Object buffer) {
        addMsgToList(new Tuple<String, Object>(null, buffer));
    }

    @Override
    public void push(String command, byte[] body, boolean tryUdp) {
        if (tryUdp && this.udpPush != null && this.udpPush.isPushable()) {
            this.udpPush.push(command, body);
        } else {
            push(command, body);
        }
    }

    @Override
    public void push(Object buffer, boolean tryUdp) {
        if (tryUdp && this.udpPush != null && this.udpPush.isPushable()) {
            this.udpPush.push(this, buffer);
        } else {
            push(buffer);
        }
    }

    @Override
    public byte[] toByte() {
        IChannelBuffer buffer = ChannelBuffers.dynamicBuffer(16);
        buffer.writeString(id);
        buffer.writeString(JSON.toJSONString(map));
        return buffer.array();
    }


}
