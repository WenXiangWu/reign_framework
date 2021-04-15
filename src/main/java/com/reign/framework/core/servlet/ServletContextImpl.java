package com.reign.framework.core.servlet;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @ClassName: ServletContextImpl
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 10:03
 **/
public class ServletContextImpl implements ServletContext {

    private ConcurrentMap<String, Object> map = new ConcurrentHashMap<>();

    private ServletConfig config;

    public ServletContextImpl() {
    }

    public ServletContextImpl(ServletConfig config) {
        this.config = config;
    }

    @Override
    public Object getAttribute(String key) {
        return map.get(key);
    }

    @Override
    public Object setAttribute(String key, Object value) {
        return map.put(key, value);
    }

    @Override
    public boolean removeAttribute(String key) {
        return map.remove(key) != null;
    }

    @Override
    public void invalidate() {
        map.clear();
    }

    @Override
    public Object getInitParam(String paramName) {
        if (config == null) {
            return null;
        }
        return config.getInitParam(paramName);
    }
}
