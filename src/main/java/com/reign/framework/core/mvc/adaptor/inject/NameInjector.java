package com.reign.framework.core.mvc.adaptor.inject;

import com.reign.framework.common.Lang;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

/**
 * @ClassName: NameInjector
 * @Description: 名称注入器
 * @Author: wuwx
 * @Date: 2021-04-19 16:19
 **/
public class NameInjector implements ParamInjector {

    protected String name;

    protected Class<?> type;

    public NameInjector(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Object get(ServletContext servletContext, Request request, Response response) {
        String params[] = request.getParamterValues(name);
        return Lang.castTo(params, type);
    }
}
