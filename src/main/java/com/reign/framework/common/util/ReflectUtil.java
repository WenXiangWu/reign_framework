package com.reign.framework.common.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @ClassName: ReflectUtil
 * @Description: 反射工具
 * @Author: wuwx
 * @Date: 2021-04-07 11:21
 **/
public class ReflectUtil {

    public static Object invoke(Method method,Object obj){
        try {
            return method.invoke(obj);
        }catch (Throwable t){
            throw new RuntimeException("invoke error",t);
        }
    }

    public static void invoke(Method method,Object obj,Object... args){
        try {
            method.invoke(obj,args);
        }catch (Throwable t){
            throw new RuntimeException("invoke error",t);
        }
    }

    public static Object get(Field field,Object obj){
        try {
            field.setAccessible(true);
            return field.get(obj);
        }catch (Throwable t){
            throw  new RuntimeException("invoke error" ,t);
        }

    }

    public static void set(Field field,Object obj,Object args){
        try {
            field.setAccessible(true);
            field.set(obj,args);
        }catch (Throwable t){
            throw new RuntimeException("set key erro",t);
        }

    }

}
