package com.reign.framework.protocol;

import com.reign.framework.core.servlet.*;

/**
 * @ClassName: ServletBootStrap
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-29 17:56
 **/
public class ServletBootStrap {


    private Servlet servlet;

    private ServletConfig sc;

    private ServletContext servletContext;

    private NettyConfig nc;

    private ServletChildHandler tcpServletChildHandler;

    private ServletChildHandler httpServletChildHandler;

    private static final String CONFIG_FILE_NAME = "conf.xml";

    private boolean isCluster = false;

}
