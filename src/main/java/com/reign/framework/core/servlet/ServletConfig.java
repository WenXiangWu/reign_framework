package com.reign.framework.core.servlet;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: ServletConfig
 * @Description: servlet配置
 * @Author: wuwx
 * @Date: 2021-04-15 10:03
 **/
public interface ServletConfig {


    String getServletName();

    Class<? extends Servlet> getServletClass();


    List<Class<?>> getListeners();

    Object getInitParam(String paramName);


    Map<String,Object> getInitParams();


    long getSessionTimeoutMillis();


    long getSessionInvalidateMillis();

    /**
     * 获取session隔天失效时间
     * @return
     */
    long getSessionNextDayInvalidateMillis();

    /**
     * session检测时间
     * @return
     */
    int getSessionTickTime();

    /**
     * 获取空session默认超时时间
     * @return
     */
    long getSessionEmptyTimeOutMillis();
}
