package com.reign.framework.core.servlet;

import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName: Group
 * @Description: 组，适用于需要将特定的一些人圈在一个范围里面，这里面的人一个人产生了消息，一般需要组里面的人都知晓
 * @Author: wuwx
 * @Date: 2021-04-15 10:02
 **/
public interface Group {

    /**
     * 创建一个组
     * @param groupId
     * @return
     */
    Group createGroup(String groupId);

    /**
     * 创建一个组
     * @param groupId
     * @param canAutoLeave  玩家掉线时系统自动帮你退组
     * @return
     */
    Group createGroup(String groupId,boolean canAutoLeave);

    /**
     * 加入一个组
     * @param session
     * @return
     */
    boolean join(Session session);

    /**
     * 离开组
     * @param sessionId
     * @return
     */
    boolean leave(String sessionId);

    /**
     * 自动退出
     * @param sessionId
     * @return
     */
    boolean autoLeave(String sessionId);


    /**
     * 清除
     */
    void clear();

    /***
     * 产生通知，通知给组内其他人
     * @param command
     * @param body
     */
    void notify(String command,byte[] body);

    /**
     * 通知组内除sessionid外的所有人
     * @param sessionId
     * @param command
     * @param body
     */
    void notify(String sessionId,String command,byte[] body);

    /**
     * 推送给与指定group有交集的人
     * @param group
     * @param command
     * @param body
     */
    void notifyMix(Group group,String command,byte[] body);

    /**
     * 将消息发送给指定的人，这人一定在组内
     * @param command
     * @param body
     * @param sessionIds
     * @return
     */
    int[] notify(String command,byte[] body,String... sessionIds);

    /**
     * 获取组编号
     * @return
     */
    String getGroupId();

    /**
     * 获取userMap
     * @return
     */
    ConcurrentMap<String,Session> getUserMap();

}
