package com.reign.framework.common;

/**
 * @ClassName: ObjectFactory
 * @Description: 对象工厂类，用于创建类
 * @Author: wuwx
 * @Date: 2021-04-10 10:06
 **/
public class ObjectFactory {

    /**
     * 创建一个类
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Object buildBean(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        Object o = clazz.newInstance();
        return o;
    }
}
