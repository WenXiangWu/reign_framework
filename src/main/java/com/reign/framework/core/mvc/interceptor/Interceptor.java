package com.reign.framework.core.mvc.interceptor;

import com.reign.framework.core.mvc.servlet.ActionInvocation;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;

import java.util.Iterator;

/**
 * @ClassName: Interceptor
 * @Description: 拦截器接口
 * @Author: wuwx
 * @Date: 2021-04-29 15:09
 **/
public interface Interceptor {

    Object intercept(ActionInvocation actionInvocation, Iterator<Interceptor> interceptors, Request request, Response response) throws Exception;
}
