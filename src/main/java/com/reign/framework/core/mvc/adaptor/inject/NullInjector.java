package com.reign.framework.core.mvc.adaptor.inject;

import com.reign.framework.common.Lang;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

/**
 * @ClassName: NullInjector
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:31
 **/
public class NullInjector implements ParamInjector {
    private Class<?> type;

    public NullInjector(Class<?> type) {
        this.type = type;
    }

    @Override
    public Object get(ServletContext servletContext, Request request, Response response) {
        return Lang.getDefaultValue(type);
    }
}
