package com.reign.framework.core.mvc.adaptor.inject;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

/**
 * @ClassName: SessionInjector
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:32
 **/
public class SessionInjector implements ParamInjector {

    private String key;

    public SessionInjector(String key) {
        this.key = key;
    }

    @Override
    public Object get(ServletContext servletContext, Request request, Response response) {
        return request.getSession().getAttribute(key);
    }
}
