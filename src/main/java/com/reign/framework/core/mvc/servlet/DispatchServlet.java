package com.reign.framework.core.mvc.servlet;

import com.reign.framework.common.Lang;
import com.reign.framework.core.util.Scans;
import com.reign.framework.common.util.StopWatch;
import com.reign.framework.common.util.Tuple;
import com.reign.framework.core.Application;
import com.reign.framework.core.mvc.adaptor.RpcAdaptor;
import com.reign.framework.core.mvc.annotation.*;
import com.reign.framework.exception.ServletConfigException;
import com.reign.framework.exception.ServletException;
import com.reign.framework.core.mvc.interceptor.Interceptor;
import com.reign.framework.core.mvc.result.NoActionResult;
import com.reign.framework.core.mvc.spring.SpringObjectFactory;
import com.reign.framework.core.mvc.view.*;
import com.reign.framework.core.servlet.*;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName: DispatchServlet
 * @Description: 默认转发器
 * @Author: wuwx
 * @Date: 2021-04-19 16:09
 **/
public class DispatchServlet implements Servlet {

    private static final Logger log = InternalLoggerFactory.getLogger(DispatchServlet.class);
    //命令转发器
    private static Map<String, ActionInvocation> handlerMap = new HashMap<>();

    //命令转发器
    private static Map<Pattern, Tuple<String, ActionInvocation>> patternHandlerMap = new HashMap<>();

    protected static ObjectFactory objectFactory;

    protected ServletConfig config;

    protected ServletContext context;
    //是否压缩
    protected boolean compress = false;
    //rpc是否压缩
    protected boolean rpcCompress = false;
    //是否停止
    protected volatile boolean stop = false;
    //默认视图
    protected static ResponseView DEFAULT_VIEW = new NullView();
    //默认rpc视图
    protected static RpcResponseView DEFAULT_RPC_VIEW = new NullRpcView();
    //拦截器列表
    protected static List<Interceptor> interceptors = new ArrayList<>();
    //默认action
    private ActionInvocation defaultAction;
    //不需要拦截的命令列表
    protected static Set<String> WITHOUT_INTERCEPTOR_COMMAND = new HashSet<>();

    static {
        WITHOUT_INTERCEPTOR_COMMAND.add("remoteLog");
    }

    @Override
    public void init(ServletConfig config, ServletContext context) {
        this.config = config;
        this.context = context;
        log.info("Init Servlet Start");
        String scanPath = (String) config.getInitParam(ACTION_SCAN_PATH);
        if (StringUtils.isBlank(scanPath)) {
            log.error("cannot find actionPackage config");
            throw new ServletConfigException("cannot find actionPackage config");
        }

        StopWatch stopWatch = new StopWatch();
        try {
            initCompress();
            initInterceptors();
            initHandlerAction(scanPath);
        } catch (Exception e) {
            log.error("", e);
            throw new ServletConfigException("", e);
        }
        stopWatch.stop();
        this.context.setAttribute(ServletContext.ROOT_WEB_APPLICATION_SERVLET_ATTRIBUTE, this);
        Application.setServlet(this);
        log.info("Init Servlet Success in " + stopWatch.getElapsedTime() + " ms");
    }

    /**
     * 初始化处理类
     *
     * @param scanPath
     * @throws Exception
     */
    private void initHandlerAction(String scanPath) throws Exception {
        Set<Class<?>> set = Scans.getClasses(scanPath);
        for (Class<?> clazz : set) {
            initHandlerAction(clazz);
        }
    }

    /**
     * 初始化处理器
     *
     * @param clazz
     * @return
     * @throws Exception
     */
    private List<Tuple<String, ActionInvocation>> initHandlerAction(Class<?> clazz) throws Exception {
        if (Modifier.isAbstract(clazz.getModifiers()) || Modifier.isInterface(clazz.getModifiers())) return null;
        Action action = Lang.getAnnotation(clazz, Action.class);
        RpcAction rpcAction = Lang.getAnnotation(clazz, RpcAction.class);
        if (action != null) {
            //表明是一个action类
            ViewManager vm = new ViewManager(DEFAULT_VIEW);
            //获取类的默认视图
            Views views = Lang.getAnnotation(clazz, Views.class);
            if (null != views) {
                for (View view : views.value()) {
                    vm.addView(view.name(), getView(view));
                }
            } else {
                View view = Lang.getAnnotation(clazz, View.class);
                if (null != view) {
                    vm.addView(view.name(), getView(view));
                }
            }

            //获取类的默认验证器
            List<Tuple<com.reign.framework.core.mvc.validation.Validation, com.reign.framework.core.mvc.validation.Rule<?>>> validationList = new ArrayList<>();
            Validations validations = Lang.getAnnotation(clazz, Validations.class);
            if (null != validations) {
                for (Validation validation : validations.value()) {
                    Tuple<com.reign.framework.core.mvc.validation.Validation, com.reign.framework.core.mvc.validation.Rule<?>> tuple = new Tuple<>();
                    tuple.left = (com.reign.framework.core.mvc.validation.Validation) Lang.createObject(getServletContext(), validation.handler());
                    //获取验证规则
                    Rule ruleAnnotation = validation.rule();
                    com.reign.framework.core.mvc.validation.Rule<?> rule = (com.reign.framework.core.mvc.validation.Rule<?>) Lang.createObject(ruleAnnotation.rule());
                    rule.parse(ruleAnnotation.expression());
                    tuple.right = rule;
                    validationList.add(tuple);
                }
            } else {
                Validation validation = Lang.getAnnotation(clazz, Validation.class);
                if (validation != null) {
                    Tuple<com.reign.framework.core.mvc.validation.Validation, com.reign.framework.core.mvc.validation.Rule<?>> tuple = new Tuple<>();
                    tuple.left = (com.reign.framework.core.mvc.validation.Validation) Lang.createObject(getServletContext(), validation.handler());
                    //获取验证规则
                    Rule ruleAnnotation = validation.rule();
                    com.reign.framework.core.mvc.validation.Rule<?> rule = (com.reign.framework.core.mvc.validation.Rule<?>) Lang.createObject(ruleAnnotation.rule());
                    rule.parse(ruleAnnotation.expression());
                    tuple.right = rule;
                    validationList.add(tuple);
                }

            }
            //获取是否需要加锁处理
            Sync sync = Lang.getAnnotation(clazz, Sync.class);
            boolean isSync = false;
            if (sync != null && sync.value()) {
                isSync = true;
            }
            //此类是一个处理类
            return createActionInvocation(clazz, vm, validationList, isSync);
        } else if (rpcAction != null) {
            //是一个rpc处理类
            ViewManager vm = new ViewManager(DEFAULT_RPC_VIEW);
            //获取RPC压缩配置
            boolean compress = rpcCompress;
            if (StringUtils.isNotBlank(rpcAction.compress())) {
                compress = Boolean.valueOf(rpcAction.compress());
            }
            //获取类的默认视图
            Views views = Lang.getAnnotation(clazz, Views.class);
            if (null != views) {
                for (View view : views.value()) {
                    ResponseView _view = getView(view);
                    _view.setCompress(compress);
                    vm.addView(view.name(), _view);
                }
            } else {
                View view = Lang.getAnnotation(clazz, View.class);
                if (null != view) {
                    ResponseView _view = getView(view);
                    _view.setCompress(compress);
                    vm.addView(view.name(), getView(view));
                }
            }
            //获取默认视图名称
            String defaultViewName = rpcAction.viewName();
            //此类是一个处理类
            return createRpcActionInvocation(clazz, vm, compress, rpcAction.adaptor(), defaultViewName);

        }
        return null;
    }

    private List<Tuple<String, ActionInvocation>> createActionInvocation(Class<?> clazz, ViewManager vm,
                                                                         List<Tuple<com.reign.framework.core.mvc.validation.Validation, com.reign.framework.core.mvc.validation.Rule<?>>> validationList,
                                                                         boolean isSync) throws Exception {
        List<Tuple<String, ActionInvocation>> list = new ArrayList<>();
        Object obj = Lang.createObject(getServletContext(), clazz);
        Method[] methods = clazz.getDeclaredMethods();
        RpcAction rpcAction = Lang.getAnnotation(clazz, RpcAction.class);
        boolean isRpcAction = rpcAction != null;
        for (Method method : methods) {
            if (Lang.isStaticMethod(method)) {
                continue;
            }
            Command cmd = method.getAnnotation(Command.class);
            if (null != cmd) {
                //这是一个处理请求的方法
                ActionInvocation ai = createInvocationAction(obj, method, vm, validationList, isSync, isRpcAction, cmd);
                if (handlerMap.containsKey(cmd.value())) {
                    throw new ServletConfigException("exists same command handler[command:"
                            + cmd.value() + ",handler1:["
                            + handlerMap.get(cmd.value()).toString()
                            + "]," + "handler2:[" + ai.toString()
                            + "]");
                }
                String cmdValue = cmd.value();
                if (cmdValue.indexOf("$") != -1) {
                    //含有通配符
                    Tuple<String, String> tuple = parsePatternCommand(cmdValue);
                    Pattern pattern = Pattern.compile(tuple.left);
                    patternHandlerMap.put(pattern, new Tuple<>(tuple.right, ai));
                    cmdValue = cmdValue.substring(cmdValue.lastIndexOf("}") + 1);
                    handlerMap.put(cmdValue, ai);
                } else {
                    handlerMap.put(cmd.value(), ai);
                }
                log.info("found command handler [command:" + cmd.value() + ",handler:" + ai.toString());
                list.add(new Tuple<>(cmd.value(), ai));
                if (null == defaultAction) {
                    defaultAction = ai;
                }
            }

        }
        return list;
    }


    private List<Tuple<String, ActionInvocation>> createRpcActionInvocation(Class<?> clazz,
                                                                            ViewManager vm,
                                                                            boolean compress,
                                                                            Class<? extends RpcAdaptor> adaptorClass,
                                                                            String defaultViewName) throws Exception {
        List<Tuple<String, ActionInvocation>> list = new ArrayList<>();
        Object obj = Lang.createObject(getServletContext(), clazz);
        Method[] methods = clazz.getDeclaredMethods();
        RpcAction rpcAction = Lang.getAnnotation(clazz, RpcAction.class);
        boolean isRpcAction = rpcAction != null;
        for (Method method : methods) {
            if (Lang.isStaticMethod(method)) {
                continue;
            }
            Command cmd = Lang.getAnnotation(method, clazz, Command.class);
            RpcCommand rpcCommand = Lang.getAnnotation(method, clazz, RpcCommand.class);
            if (null != cmd || null != rpcCommand) {
                String command = (null == rpcCommand) ? cmd.value() : rpcCommand.value();
                String viewName = defaultViewName;
                if (null != rpcCommand) {
                    viewName = rpcCommand.viewName();
                }


                //这是一个处理请求的方法
                RpcActionInvocation ai = createRpcInvocationAction(obj, method, vm, compress, adaptorClass, viewName);
                if (handlerMap.containsKey(cmd.value())) {
                    throw new ServletConfigException("exists same command handler[command:"
                            + cmd.value() + ",handler1:["
                            + handlerMap.get(cmd.value()).toString()
                            + "]," + "handler2:[" + ai.toString()
                            + "]");
                }

                String cmdValue = command;
                if (cmdValue.indexOf("$") != -1) {
                    //含有通配符
                    Tuple<String, String> tuple = parsePatternCommand(cmdValue);
                    Pattern pattern = Pattern.compile(tuple.left);
                    patternHandlerMap.put(pattern, new Tuple<>(tuple.right, ai));
                    cmdValue = cmdValue.substring(cmdValue.lastIndexOf("}") + 1);
                    handlerMap.put(cmdValue, ai);
                } else {
                    handlerMap.put(cmd.value(), ai);
                }
                log.info("found command handler [command:" + cmd.value() + ",handler:" + ai.toString());
                list.add(new Tuple<>(cmd.value(), ai));
                if (null == defaultAction) {
                    defaultAction = ai;
                }
            }
        }
        return list;
    }

    private RpcActionInvocation createRpcInvocationAction(Object obj, Method method, ViewManager vm, boolean compress, Class<? extends RpcAdaptor> adaptorClass, String viewName) throws Exception {
        RpcActionInvocation rpcActionInvocation = new RpcActionInvocation(context, obj, method, compress, adaptorClass, viewName);
        rpcActionInvocation.setViewManager(vm);
        rpcActionInvocation.init();
        return rpcActionInvocation;
    }

    /**
     * 解析包含通配符的value
     *
     * @param cmdValue
     * @return
     */
    private static Tuple<String, String> parsePatternCommand(String cmdValue) {
        StringBuilder builder = new StringBuilder(cmdValue.length());
        StringBuilder pattern = new StringBuilder();
        boolean flag = true;
        for (int i = 0; i < cmdValue.length(); i++) {
            char c = cmdValue.charAt(i);
            switch (c) {
                case '$':
                    flag = true;
                    builder.append("([\\w-/]*)");
                    break;
                case '{':
                    break;
                case '}':
                    flag = false;
                    break;
                default:
                    if (!flag) {
                        builder.append(c);
                    } else {
                        pattern.append(c);
                    }
                    break;
            }
        }
        return new Tuple<>(builder.toString(), pattern.toString());

    }

    private ActionInvocation createInvocationAction(Object obj,
                                                    Method method,
                                                    ViewManager vm,
                                                    List<Tuple<com.reign.framework.core.mvc.validation.Validation, com.reign.framework.core.mvc.validation.Rule<?>>> validationList,
                                                    boolean isSync,
                                                    boolean isRpcAction,
                                                    Command cmd) throws Exception {
        ActionInvocation invocation = null;
        if (isRpcAction) {
            invocation = new ActionInvocation(context, obj, method, compress);
        } else if (WITHOUT_INTERCEPTOR_COMMAND.contains(cmd.value())) {
            invocation = new ActionInvocationWithOutInterceptor(context, obj, method, compress);
        } else {
            invocation = new ActionInvocation(context, obj, method, compress);
        }
        invocation.setViewManager(vm);
        invocation.setValidatationList(validationList);
        invocation.setSync(isSync);
        invocation.init();
        return invocation;
    }

    /**
     * 获取视图真正的处理类
     *
     * @param view
     * @return
     * @throws Exception
     */
    private ResponseView getView(View view) throws Exception {
        Class<? extends ResponseView> viewType = view.type();
        ResponseView _view = (ResponseView) createObject(viewType);
        String value = view.compress();
        if ("true".equalsIgnoreCase(value)) {
            _view.setCompress(true);
        } else if ("false".equalsIgnoreCase(value)) {
            _view.setCompress(false);
        } else {
            _view.setCompress(compress);
        }
        return _view;
    }

    /**
     * 初始化是否压缩
     */
    private void initCompress() {
        String value = (String) config.getInitParam(ACTION_COMPRESS);
        if (StringUtils.isNotBlank(value)) {
            compress = Boolean.valueOf(value);
        }
        value = (String) config.getInitParam(RPCACTION_COMPRESS);
        if (StringUtils.isNotBlank(value)) {
            rpcCompress = Boolean.valueOf(value);
        }
    }


    /**
     * 初始化拦截器
     *
     * @throws ServletException
     */
    private void initInterceptors() throws ServletException {
        String value = (String) config.getInitParam(ACTION_INTEREPTOR);
        if (StringUtils.isBlank(value)) return;
        String[] classNames = value.split(",");
        for (String className : classNames) {
            try {
                className = trim(className);
                Class<? extends Interceptor> clazz = (Class<? extends Interceptor>) Thread.currentThread().getContextClassLoader().loadClass(className);
                interceptors.add((Interceptor) createObject(clazz));
            } catch (Exception e) {
                throw new ServletException("unknown interceptor " + className);
            }

        }
    }

    private static String trim(String value) {
        return value.trim().replaceAll("\r", "").replaceAll("\n", "").replaceAll("\t", "");
    }

    private Object createObject(Class<?> clazz) throws Exception {
        return getObjectFactory().buildBean(clazz);
    }

    public ObjectFactory getObjectFactory() {
        if (null == objectFactory) {
            SpringObjectFactory factory = new SpringObjectFactory();
            ApplicationContext applicationContext = (ApplicationContext) getServletContext().getAttribute(ServletContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            if (null == applicationContext) {
                log.info("applicationContext could not be found.Action classes will not be autowired");
                objectFactory = new ObjectFactory();
            } else {
                factory.setApplicationContext(applicationContext);
                objectFactory = factory;
            }
        }
        return objectFactory;
    }

    public ServletContext getServletContext() {
        return context;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }


    @Override
    public void service(Request request, Response response) {
        if (stop) {
            return;
        }
        ActionInvocation invocation = handlerMap.get(request.getCommand());
        try {
            //设置线程标识
            ThreadContext.setRequestContext(invocation, request, context);
            if (null != invocation) {
                //找不到的时候找通配的
                for (Map.Entry<Pattern, Tuple<String, ActionInvocation>> entry : patternHandlerMap.entrySet()) {
                    Pattern pattern = entry.getKey();
                    Matcher matcher = pattern.matcher(request.getCommand());
                    if (matcher.find()) {
                        //找到了
                        Tuple<String, ActionInvocation> tuple = entry.getValue();
                        String patternValue = matcher.group(1);
                        request.getParamterMap().put(tuple.left, new String[]{patternValue});
                        invocation = tuple.right;
                        break;
                    }
                }
                //使用默认视图呈现
                if (null == invocation) {
                    defaultAction.render(new NoActionResult(request.getCommand()), request, response);
                    log.warn("handler command error,no such command :{}", request.getCommand());
                    return;
                }
            }
            //调用命令
            Object result = invocation.invoke(interceptors.iterator(), request, response);
            //展现结果
            invocation.render(result, request, response);

        } catch (Throwable t) {
            log.error("handler command error,command:" + request.getCommand(), t);
            //异常处理
            throw new RuntimeException(t);
        } finally {
            //清除线程标识
            ThreadContext.clear();
        }
    }

    @Override
    public void destroy() {
        stop = true;
    }
}
