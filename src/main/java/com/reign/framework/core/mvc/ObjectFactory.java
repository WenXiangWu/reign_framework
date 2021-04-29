package com.reign.framework.core.mvc;

/**
 * @ClassName: ObjectFactory
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:12
 **/
public class ObjectFactory {

    public Object buildBean(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Object o = clazz.newInstance();
        return o;
    }
}
