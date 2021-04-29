package com.reign.framework.core.mvc.servlet;

import com.reign.framework.common.util.Tuple;
import com.reign.framework.core.mvc.interceptor.Interceptor;
import com.reign.framework.core.mvc.result.Result;
import com.reign.framework.core.mvc.validation.Rule;
import com.reign.framework.core.mvc.validation.Validation;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * @ClassName: ActionInvocationWithOutInterceptor
 * @Description: 无拦截器实现
 * @Author: wuwx
 * @Date: 2021-04-19 18:41
 **/
public class ActionInvocationWithOutInterceptor extends ActionInvocation {
    public ActionInvocationWithOutInterceptor(ServletContext context, Object obj, Method method, boolean compress) {
        super(context, obj, method, compress);
    }


    @Override
    protected Object _invoke(Iterator<Interceptor> interceptors, Request request, Response response) throws Exception {
        Object[] params = adaptor.adapt(getServletContext(), request, response);
        request.setRequestArgs(params);
        //验证器验证
        if (needValidate) {
            for (Tuple<Validation, Rule<?>> tuple : validatationList) {
                Result<?> result = tuple.left.validate(request, tuple.right);
                if (null != result) {
                    return result;
                }
            }
        }
        return (Result<?>)method.invoke(obj, params);
    }
}
