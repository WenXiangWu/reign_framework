package com.reign.framework.core.mvc.adaptor.inject;

import com.reign.framework.common.Lang;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

/**
 * @ClassName: ArrayInjector
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:18
 **/
public class ArrayInjector implements ParamInjector {

    protected String name;

    protected Class<?> type;

    public ArrayInjector(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Object get(ServletContext servletContext, Request request, Response response) {
        String[] params = request.getParamterValues(name);
        return Lang.castTo(params,type);
    }
}
