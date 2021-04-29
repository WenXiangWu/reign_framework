package com.reign.framework.core.mvc.listener;

import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.core.servlet.ServletContextListener;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import org.springframework.context.ApplicationContext;

/**
 * @ClassName: SpringContextLoaderListener
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-19 18:12
 **/
public class SpringContextLoaderListener implements ServletContextListener {


    private static final Logger log = InternalLoggerFactory.getLogger(SpringContextLoaderListener.class);

    @Override
    public void contextInitialized(ServletContext sc) {
        init(sc);
    }

    private void init(ServletContext sc) {
        if (sc.getAttribute(ServletContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
            throw new IllegalStateException("Can not initialize context because there is already exists");
        }
        log.info("Inializing spring boot WebApplicationContext");
        long startTime = System.currentTimeMillis();
        ApplicationContext ac = createApplicationContext(sc);
        sc.setAttribute(ServletContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ac);

        long elapsedTime = System.currentTimeMillis() - startTime;
        log.info("Root WebApplicationContext:initialization completed in " + elapsedTime + " ms");
    }

    /**
     * 初始化spring环境
     *
     * @param sc
     * @return
     */
    private ApplicationContext createApplicationContext(ServletContext sc) {
        MyXmlApplicationContext context = new MyXmlApplicationContext(sc);
        context.refresh();
        return context;
    }

    @Override
    public void contextDestroyed(ServletContext sc) {

    }
}
