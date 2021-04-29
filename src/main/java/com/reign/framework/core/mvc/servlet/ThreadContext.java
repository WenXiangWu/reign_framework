package com.reign.framework.core.mvc.servlet;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.ServletContext;

/**
 * @ClassName: ThreadContext
 * @Description: 线程环境变量
 * @Author: wuwx
 * @Date: 2021-04-19 18:42
 **/
public class ThreadContext {
    //请求上下文
    private static final ThreadLocal<RequestContext> threadContext = new ThreadLocal<>();

    /**
     * 是否是用户请求线程
     *
     * @return
     */
    public static boolean isRequestThread() {
        RequestContext requestContext = threadContext.get();
        return null != requestContext;
    }

    public static ThreadLocal<RequestContext> getThreadContext() {
        return threadContext;
    }

    /**
     * 设置线程上下文
     *
     * @param invocation
     * @param request
     * @param context
     */
    static void setRequestContext(ActionInvocation invocation, Request request, ServletContext context) {
        threadContext.set(new RequestContext(invocation, request, context));
    }

    /**
     * 清除线程标记
     */
    static void clear() {
        threadContext.remove();
    }

}
