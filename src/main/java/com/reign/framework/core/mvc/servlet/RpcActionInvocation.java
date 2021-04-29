package com.reign.framework.core.mvc.servlet;

import com.reign.framework.common.Lang;
import com.reign.framework.common.util.Tuple;
import com.reign.framework.core.mvc.adaptor.RpcAdaptor;
import com.reign.framework.core.mvc.annotation.View;
import com.reign.framework.core.mvc.annotation.Views;
import com.reign.framework.core.mvc.interceptor.Interceptor;
import com.reign.framework.core.mvc.result.Result;
import com.reign.framework.core.mvc.validation.Rule;
import com.reign.framework.core.mvc.validation.Validation;
import com.reign.framework.core.mvc.view.ResponseView;
import com.reign.framework.core.mvc.view.RpcResponseView;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

import java.lang.reflect.Method;
import java.util.Iterator;

/**
 * @ClassName: RpcActionInvocation
 * @Description: RPC调用
 * @Author: wuwx
 * @Date: 2021-04-19 18:41
 **/
public class RpcActionInvocation extends ActionInvocation {

    //异常视图
    public static final String EXCEPTION = "exception";

    //适配器class
    private Class<? extends RpcAdaptor> adaptorClass;

    //参数适配器
    private RpcAdaptor adaptor;

    //视图名称
    private String viewName;

    public RpcActionInvocation(ServletContext context, Object obj, Method method, boolean compress,
                               Class<? extends RpcAdaptor> adaptorClass, String viewName) {
        super(context, obj, method, compress);
        this.adaptorClass = adaptorClass;
        this.viewName = viewName;
    }

    @Override
    public void init() throws Exception {
        initAdaptor();
        initView();
        initPrintArgs();
    }

    @Override
    protected void initView() throws Exception {
        //获取类的默认视图
        Views views = method.getAnnotation(Views.class);
        if (null != views) {
            for (View view : views.value()) {
                vm.addView(view.name(), getView(view));
            }
        } else {
            View view = method.getAnnotation(View.class);
            if (null != view) {
                vm.addView(view.name(), getView(view));
            }
        }
    }

    @Override
    protected ResponseView getView(View view) throws Exception {
        Class<? extends ResponseView> viewType = view.type();
        RpcResponseView _view = (RpcResponseView) Lang.createObject(context, viewType);
        _view.setCompress(globalCompress);
        return _view;
    }

    @Override
    protected void initAdaptor() throws Exception {
        adaptor = adaptorClass.newInstance();
        adaptor.init(method, globalCompress);
    }

    @Override
    public Object invoke(Iterator<Interceptor> interceptors, Request request, Response response) throws Exception {
        return _invoke(interceptors, request, response);
    }

    @Override
    protected Object _invoke(Iterator<Interceptor> interceptors, Request request, Response response) throws Exception {
        if (null != interceptors && interceptors.hasNext()) {
            Interceptor interceptor = interceptors.next();
            return interceptor.intercept(this, interceptors, request, response);
        } else {
            Object[] params = adaptor.adapt(getServletContext(), request, response);
            request.setRequestArgs(params);
            return method.invoke(obj, params);
        }
    }


    @Override
    public void render(Object result, Request request, Response response) throws Exception {
        RpcResponseView view = (RpcResponseView) getView(viewName);
        view.renderRpc(result, request, response);
    }
}
