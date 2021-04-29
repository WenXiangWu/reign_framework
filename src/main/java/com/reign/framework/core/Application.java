package com.reign.framework.core;

import com.reign.framework.core.servlet.Servlet;

/**
 * @ClassName: Application
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 10:00
 **/
public class Application {

    private static Servlet servlet;

    public static synchronized void setServlet(Servlet servlet){
        if (Application.servlet!=null){
            throw new RuntimeException("servlet not null, cannot assigned again");
        }
        Application.servlet = servlet;
    }

    public static Servlet getServlet() {
        return servlet;
    }
}
