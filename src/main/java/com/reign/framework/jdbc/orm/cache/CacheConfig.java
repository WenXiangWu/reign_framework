package com.reign.framework.jdbc.orm.cache;

/**
 * @ClassName: CacheConfig
 * @Description: 缓存配置
 * @Author: wuwx
 * @Date: 2021-04-09 17:44
 **/
public class CacheConfig {


    /**名称*/
    public String name;


    /**最大存活时间*/
    public int maxLiveTime;

    /**cache类型*/
    public AccessType accessType;

    public CacheConfig(String name, int maxLiveTime) {
        this.name = name;
        this.maxLiveTime = maxLiveTime;
    }

    public int getMaxLiveTime() {
        return maxLiveTime;
    }

    public void setMaxLiveTime(int maxLiveTime) {
        this.maxLiveTime = maxLiveTime;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }
}
