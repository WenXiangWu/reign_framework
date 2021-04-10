package com.reign.framework.jdbc.orm.session;

import com.reign.framework.jdbc.orm.JdbcEntity;

/**
 * @ClassName: DefaultJdbcSessionTrigger
 * @Description: 默认jdbc触发器
 * @Author: wuwx
 * @Date: 2021-04-08 18:14
 **/
public class DefaultJdbcSessionTrigger implements JdbcSessionTrigger, Comparable<DefaultJdbcSessionTrigger> {

    /**
     * 影响的表名
     */
    private String tableName;

    /**
     * 触发的操作类型
     */
    private int triggerType;

    /**
     * 触发操作
     */
    private JdbcSessionTrigger trigger;

    /**
     * 影响的实体
     */
    private JdbcEntity entity;

    @Override
    public void trigger() {
        try {
            trigger.trigger();
        } finally {
            entity.resetDelaySqlFlag();
        }

    }

    @Override
    public int compareTo(DefaultJdbcSessionTrigger o) {
        if (this.triggerType == o.triggerType) {
            return this.tableName.compareTo(o.tableName);
        } else if (this.triggerType > o.triggerType) {
            return 1;
        } else {
            return -1;
        }
    }

    public DefaultJdbcSessionTrigger(JdbcEntity entity, String tableName, int triggerType, JdbcSessionTrigger trigger) {
        super();
        this.tableName = tableName;
        this.triggerType = triggerType;
        this.trigger = trigger;
        this.entity = entity;
        //启用了延迟执行SQL
        entity.enableDelaySQL();
    }


}
