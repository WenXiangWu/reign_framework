package com.reign.framework.core.mvc.adaptor;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

import java.lang.reflect.Method;

/**
 * @ClassName: HttpAdaptor
 * @Description: http适配器
 * @Author: wuwx
 * @Date: 2021-04-19 16:15
 **/
public interface HttpAdaptor {

    /**
     * 为指定方法适配合适的适配器
     * @param servletContext
     * @param method
     */
    void init(ServletContext servletContext, Method method);

    /**
     * 获得调用方法的参数值
     * @param servletContext
     * @param request
     * @param response
     * @return
     */
    Object[] adapt(ServletContext servletContext, Request request, Response response);
}
