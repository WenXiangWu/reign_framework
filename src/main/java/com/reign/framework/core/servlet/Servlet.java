package com.reign.framework.core.servlet;

import java.io.Serializable;

/**
 * @ClassName: Servlet
 * @Description: servlet
 * @Author: wuwx
 * @Date: 2021-04-13 10:11
 **/
public interface Servlet extends Serializable {
    /**
     * 扫描路径参数配置
     */
    public static final String ACTION_SCAN_PATH = "actionPackage";
    /**
     * 拦截器配置参数
     */
    public static final String ACTION_INTEREPTOR = "actionInterceptor";
    /**
     * 压缩参数配置
     */
    public static final String ACTION_COMPRESS = "compress";

    /**
     * 压缩参数配置
     */
    public static final String RPCACTION_COMPRESS = "rpcCompress";

    /**
     * 初始化servlet配置
     *
     * @param config
     * @param context
     */
    void init(ServletConfig config, ServletContext context);

    /**
     * 获取servlet配置
     *
     * @return
     */
    ServletConfig getServletConfig();

    /**
     * 获取服务器配置
     *
     * @return
     */
    ServletContext getServletContext();

    /**
     * 处理请求
     *
     * @param request
     * @param response
     */
    void service(Request request, Response response);

    /**
     * 销毁
     */
    void destroy();
}
