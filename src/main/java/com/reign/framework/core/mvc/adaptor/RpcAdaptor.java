package com.reign.framework.core.mvc.adaptor;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

import java.lang.reflect.Method;

/**
 * @ClassName: RpcAdaptor
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:17
 **/
public interface RpcAdaptor {

    /**
     * 为指定方法适配合适的适配器
     * @param method
     * @param compress
     */
    void init(Method method, boolean compress);

    /**
     * 获得调用方法的参数值
     * @param servletContext
     * @param request
     * @param response
     * @return
     */
    Object[] adapt(ServletContext servletContext, Request request, Response response);
}
