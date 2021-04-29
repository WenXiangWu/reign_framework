package com.reign.framework.core.servlet;

/**
 * @ClassName: InitProjectListener
 * @Description: 项目启动后初始化
 * @Author: wuwx
 * @Date: 2021-04-15 10:02
 **/
public interface InitProjectListener {
    /**
     * 初始化工程
     *
     * @param context
     * @param config
     */
    public void init(ServletContext context, NettyConfig config);
}
