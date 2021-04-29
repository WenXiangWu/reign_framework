package com.reign.framework.core.mvc.adaptor;

import com.reign.framework.core.mvc.adaptor.inject.*;
import com.reign.framework.core.mvc.annotation.GlobalCacheParam;
import com.reign.framework.core.mvc.annotation.RequestParam;
import com.reign.framework.core.mvc.annotation.SessionParam;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.core.servlet.Session;
import com.reign.framework.core.util.GlobalCache;
import com.reign.framework.jdbc.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @ClassName: AbstractAdaptor
 * @Description: 适配器抽象实现
 * @Author: wuwx
 * @Date: 2021-04-19 16:17
 **/
public abstract class AbstractAdaptor implements HttpAdaptor {

    //参数解释器
    protected ParamInjector[] injectors;

    @Override
    public void init(ServletContext servletContext, Method method) {
        Class<?>[] argTypes = method.getParameterTypes();
        injectors = new ParamInjector[argTypes.length];
        Annotation[][] annss = method.getParameterAnnotations();
        for (int i = 0; i < annss.length; i++) {
            Annotation[] anns = annss[i];
            RequestParam requestParam = null;
            SessionParam sessionParam = null;
            GlobalCacheParam globalCacheParam = null;
            for (int x = 0; x < anns.length; x++) {
                if (anns[x] instanceof RequestParam) {
                    requestParam = (RequestParam) anns[x];
                    break;
                } else if (anns[x] instanceof SessionParam) {
                    sessionParam = (SessionParam) anns[x];
                    break;
                } else if (anns[x] instanceof GlobalCacheParam) {
                    globalCacheParam = (GlobalCacheParam) anns[x];
                    break;
                }
            }
            if (null != sessionParam) {
                injectors[i] = new SessionInjector(sessionParam.value());
            } else if (null != globalCacheParam) {
                injectors[i] = new GlobalCacheInjector();
            }
            injectors[i] = evalInjectorByParamType(argTypes[i]);
            if (null != injectors[i]) {
                continue;
            }
            injectors[i] = evalInjector(argTypes[i], requestParam);

        }

    }

    @Override
    public Object[] adapt(ServletContext servletContext, Request request, Response response) {
        Object[] args = new Object[injectors.length];
        for (int i = 0; i < injectors.length; i++) {
            args[i] = injectors[i].get(servletContext, request, response);
        }
        return args;
    }

    public abstract ParamInjector evalInjector(Class<?> clazz,RequestParam requestParam);

    private ParamInjector evalInjectorByParamType(Class<?> clazz){
        if (Request.class.isAssignableFrom(clazz)){
            return new RequestInjector();
        }else if (Response.class.isAssignableFrom(clazz)){
            return new ResponseInjector();
        }
        return null;
    }
}
