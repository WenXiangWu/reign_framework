package com.reign.framework.core.mvc.servlet;

import com.reign.framework.common.Lang;
import com.reign.framework.common.util.Tuple;
import com.reign.framework.core.mvc.adaptor.HttpAdaptor;
import com.reign.framework.core.mvc.adaptor.PairAdaptor;
import com.reign.framework.core.mvc.annotation.*;
import com.reign.framework.core.mvc.interceptor.Interceptor;
import com.reign.framework.core.mvc.result.Result;
import com.reign.framework.core.mvc.validation.Rule;
import com.reign.framework.core.mvc.validation.Validation;
import com.reign.framework.core.mvc.view.ResponseView;
import com.reign.framework.core.mvc.view.ViewManager;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.servlet.ServletContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @ClassName: ActionInvocation
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:14
 **/
public class ActionInvocation {
    //异常视图
    public static final String EXCEPTION = "exception";
    //目标Action
    protected Object obj;
    //目标方法
    protected Method method;
    //action名称
    protected String actionName;
    //方法名称
    protected String methodName;
    //参数适配器
    protected HttpAdaptor adaptor;

    protected ServletContext context;

    protected ViewManager vm;
    //是否要同步
    protected boolean sync;
    //是否需要验证，用于快速判断
    protected boolean needValidate = false;
    //是否在聊天事务中
    protected boolean isInChatTransactional = false;
    //是否需要打印日志
    protected boolean printArgs = true;
    //验证器
    protected List<Tuple<Validation, Rule<?>>> validatationList;
    //全局压缩配置
    protected boolean globalCompress;

    public ActionInvocation(ServletContext context, Object obj, Method method, boolean compress) {
        this.obj = obj;
        this.method = method;
        this.context = context;
        this.actionName = obj.getClass().getName();
        this.globalCompress = compress;
    }

    public void init() throws Exception {
        initAdaptor();
        initView();
        initSync();
        initValidation();
        initChatTransactional();
        initPrintArgs();
    }

    protected void initAdaptor() throws Exception {
        this.adaptor = new PairAdaptor();
        adaptor.init(context, method);
    }

    protected void initView() throws Exception {
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

    protected void initSync() throws Exception {
        Sync sync = method.getAnnotation(Sync.class);
        if (null != sync && sync.value()) {
            setSync(true);
        }
    }

    protected void initValidation() throws Exception {
        //获取类的验证器
        List<Tuple<Validation, Rule<?>>> validationList = new ArrayList<>();
        Validations validations = method.getAnnotation(Validations.class);
        if (validations != null) {
            for (com.reign.framework.core.mvc.annotation.Validation validation : validations.value()) {
                Tuple<Validation, Rule<?>> tuple = new Tuple<>();
                tuple.left = (Validation) Lang.createObject(getServletContext(), validation.handler());

                //获取验证规则
                com.reign.framework.core.mvc.annotation.Rule ruleAnnotation = validation.rule();
                Rule<?> rule = (Rule<?>) Lang.createObject(ruleAnnotation.rule());
                rule.parse(ruleAnnotation.expression());
                tuple.right = rule;
                validationList.add(tuple);
            }
        } else {
            com.reign.framework.core.mvc.annotation.Validation validationAnnotation = method.getAnnotation(com.reign.framework.core.mvc.annotation.Validation.class);
            if (null != validationAnnotation) {
                Tuple<Validation, Rule<?>> tuple = new Tuple<>();
                tuple.left = (Validation) Lang.createObject(getServletContext(), validationAnnotation.handler());
                //获取验证规则
                com.reign.framework.core.mvc.annotation.Rule ruleAnnotation = validationAnnotation.rule();
                Rule<?> rule = (Rule<?>) Lang.createObject(ruleAnnotation.rule());
                rule.parse(ruleAnnotation.expression());
                tuple.right = rule;
                validationList.add(tuple);
            }
        }
        if (validationList.size() > 0) {
            setValidatationList(validationList);
        }

    }

    protected void initChatTransactional() throws Exception {
        if (method.getClass().getAnnotation(ChatTransactional.class) != null) {
            setInChatTransactional(true);
        } else if (method.getAnnotation(ChatTransactional.class) != null) {
            setInChatTransactional(true);
        }
    }

    /**
     * 初始化是否需要打印日志配置
     *
     * @throws Exception
     */
    protected void initPrintArgs() throws Exception {
        if (method.getClass().getAnnotation(PrintArgsOff.class) != null) {
            setPrintArgs(true);
        } else if (method.getAnnotation(PrintArgsOff.class) != null) {
            setPrintArgs(true);
        }
    }

    /**
     * 获取视图真正的处理类
     *
     * @param view
     * @return
     * @throws Exception
     */
    protected ResponseView getView(View view) throws Exception {
        Class<? extends ResponseView> viewType = view.type();
        ResponseView _view = (ResponseView) Lang.createObject(context, viewType);
        String value = view.compress();
        if ("true".equalsIgnoreCase(value)) {
            _view.setCompress(true);
        } else if ("false".equalsIgnoreCase(value)) {
            _view.setCompress(false);
        } else {
            _view.setCompress(globalCompress);
        }
        return _view;
    }


    /**
     * 方法调用
     *
     * @param interceptors
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public Object invoke(Iterator<Interceptor> interceptors, Request request, Response response) throws Exception {
        if (isSync()) {
            //需要做同步处理，对用户的session进行锁定
            synchronized (request.getSession()) {
                return _invoke(interceptors, request, response);
            }
        } else {
            return _invoke(interceptors, request, response);
        }
    }

    protected Object _invoke(Iterator<Interceptor> interceptors, Request request, Response response) throws Exception {
        if (null != interceptors && interceptors.hasNext()) {
            Interceptor interceptor = interceptors.next();
            return interceptor.intercept(this, interceptors, request, response);
        } else {
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
            return method.invoke(obj, params);
        }

    }

    /**
     * 呈现结果
     *
     * @param result
     * @param request
     * @param response
     * @throws Exception
     */
    public void render(Object result, Request request, Response response) throws Exception {
        Result<?> _result = (Result<?>) result;
        getView(null != _result ? _result.getViewName() : null).render(_result, request, response);
    }

    public ResponseView getView(String viewName) {
        return vm.getView(viewName);
    }

    public void setViewManager(ViewManager vm) {
        this.vm = vm;
    }

    @Override
    public String toString() {
        return actionName + " " + methodName;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public ServletContext getServletContext() {
        return context;
    }

    public void setValidatationList(List<Tuple<Validation, Rule<?>>> validatationList) {
        this.validatationList = validatationList;
        this.needValidate = validatationList.size() > 0;
    }

    public void setInChatTransactional(boolean inChatTransactional) {
        isInChatTransactional = inChatTransactional;
    }

    public void setPrintArgs(boolean printArgs) {
        this.printArgs = printArgs;
    }
}
