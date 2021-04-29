package com.reign.framework.core.mvc.adaptor.inject;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.core.util.GlobalCache;

/**
 * @ClassName: GlobalCacheInjector
 * @Description: 全局参数注入器
 * @Author: wuwx
 * @Date: 2021-04-19 16:18
 **/
public class GlobalCacheInjector implements ParamInjector {
    @Override
    public Object get(ServletContext servletContext, Request request, Response response) {
        GlobalCache cache = (GlobalCache) servletContext.getAttribute(ServletContext.ROOT_WEB_APPLICATION_GLOBALCACHE_ATTRIBUTE);
        if (null == cache) return null;
        Object key = request.getGlobalKeyValue();
        if (null != key) {
            return cache.getFromCache(key);
        }
        return null;
    }
}
