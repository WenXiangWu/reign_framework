package com.reign.framework.core.servlet;

import java.util.Map;

/**
 * @ClassName: NettyConfig
 * @Description: netty配置
 * @Author: wuwx
 * @Date: 2021-04-15 10:03
 **/
public interface NettyConfig {

    /**
     * 获取参数
     * @param paramName
     * @return
     */
    Object getInitParam(String paramName);

    /**
     * 获取初始化参数
     * @return
     */
    Map<String,Object> getInitParams();


    /**
     * 获取tcp初始化参数
     * @return
     */
    Map<String,Object> getTcpParams();

}
