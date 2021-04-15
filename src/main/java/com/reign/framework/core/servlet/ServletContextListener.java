package com.reign.framework.core.servlet;

/**
 * @ClassName: ServletContextListener
 * @Description: 监听器
 * @Author: wuwx
 * @Date: 2021-04-15 10:04
 **/
public interface ServletContextListener {

    /**
     * context初始化
     * @param sc
     */
    void contextInitialized(ServletContext sc);

    /**
     * context销毁
     * @param sc
     */
    void contextDestroyed(ServletContext sc);

}
