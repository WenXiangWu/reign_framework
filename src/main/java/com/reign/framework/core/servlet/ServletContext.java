package com.reign.framework.core.servlet;

/**
 * @ClassName: ServletContext
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 10:03
 **/
public interface ServletContext {

    /**spring context key*/
    public static final String ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE = ServletContext.class.getName()+".Spring.Root";
    /**servlet context key*/
    public static final String ROOT_WEB_APPLICATION_SERVLET_ATTRIBUTE = ServletContext.class.getName()+".Servlet.Root";
    /**Global cache*/
    public static final String ROOT_WEB_APPLICATION_GLOBALCACHE_ATTRIBUTE = ServletContext.class.getName()+".GlobalCache.Root";
    /**Global cache key type*/
    public static final String ROOT_WEB_APPLICATION_GLOBALCACHE_KEY_TYPE_ATTRIBUTE = ServletContext.class.getName()+".GlobalCache.KeyType.Root";


    /**
     * 获取属性
     * @param key
     * @return
     */
    Object getAttribute(String key);

    /**
     * 设置属性
     * @param key
     * @param value
     * @return
     */
    Object setAttribute(String key,Object value);

    /**
     * 移除属性
     * @param key
     */
    boolean removeAttribute(String key);

    /**
     * 使所有属性失效
     */
    void invalidate();

    /**
     * 获取参数
     * @param paramName
     * @return
     */
    Object getInitParam(String  paramName);
}
