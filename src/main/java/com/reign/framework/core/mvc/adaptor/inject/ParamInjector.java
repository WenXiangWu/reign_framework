package com.reign.framework.core.mvc.adaptor.inject;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

/**
 * @ClassName: ParamInjector
 * @Description: 参数注入器
 * @Author: wuwx
 * @Date: 2021-04-19 16:32
 **/
public interface ParamInjector {

    /**
     * 从request中获取参数值
     * @param servletContext
     * @param request
     * @param response
     * @return
     */
    Object get(ServletContext servletContext, Request request, Response response);
}
