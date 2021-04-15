package com.reign.framework.core.servlet;

/**
 * @ClassName: SessionAttributeListener
 * @Description: session属性监听回调
 * @Author: wuwx
 * @Date: 2021-04-15 10:04
 **/
public interface SessionAttributeListener {

    void attributeAdded(SessionAttributeEvent se);

    void attributeRemoveed(SessionAttributeEvent se);

    void attributeReplaced(SessionAttributeEvent se);

}
