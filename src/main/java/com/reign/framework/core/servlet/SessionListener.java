package com.reign.framework.core.servlet;

import java.util.EventListener;

/**
 * @ClassName: SessionListener
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 10:05
 **/
public interface SessionListener extends EventListener {


    void sessionCreated(SessionEvent e);

    void sessionDestroyed(SessionEvent e);
}
