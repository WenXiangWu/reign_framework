package com.reign.framework.core.mvc.servlet;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.ServletContext;

/**
 * @ClassName: RequestContext
 * @Description: 请求上下文
 * @Author: wuwx
 * @Date: 2021-04-19 18:41
 **/
public class RequestContext {

    public ActionInvocation invocation;

    public Request request;

    public ServletContext context;

    public RequestContext(ActionInvocation invocation, Request request, ServletContext context) {
        this.invocation = invocation;
        this.request = request;
        this.context = context;
    }

    public ActionInvocation getActionInvocation() {
        return invocation;
    }

    public void setActionInvocation(ActionInvocation invocation) {
        this.invocation = invocation;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public ServletContext getContext() {
        return context;
    }

    public void setContext(ServletContext context) {
        this.context = context;
    }
}
