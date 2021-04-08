package com.reign.framework.jdbc;

import com.reign.framework.common.util.ReflectUtil;
import com.reign.framework.jdbc.async.AsyncManager;
import com.reign.framework.jdbc.async.DbAsyncCallback;
import com.reign.framework.jdbc.async.DefaultAsyncCallback;
import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.JdbcFactory;
import com.reign.framework.jdbc.orm.JdbcField;
import com.reign.framework.jdbc.orm.annotation.AsyncMethod;
import com.reign.framework.jdbc.orm.annotation.AsyncOp;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: JdbcCglibProxy
 * @Description: cglic 动态代理
 * @Author: wuwx
 * @Date: 2021-04-08 14:37
 **/
public class JdbcCglibProxy implements MethodInterceptor {

    /**
     * 实例
     */
    private final Object target;

    /**
     * SQLFactory
     */
    private final SqlFactory factory;

    /**
     * 实体
     */
    private JdbcEntity entity;

    /**
     * 缓存map
     */
    private static Map<Class<?>, Object> cacheMap = new HashMap<>();

    /**
     * 构造函数
     *
     * @param target
     * @param factory
     */
    public JdbcCglibProxy(Object target, SqlFactory factory) {
        this.target = target;
        this.factory = factory;

        try {
            Class<?> superClass = target.getClass().getSuperclass();
            if (null != superClass) {
                Field field = superClass.getDeclaredField("entity");
                if (null != field) {
                    entity = (JdbcEntity) ReflectUtil.get(field, target);
                }
            }
        } catch (Throwable t) {
            //Ignore
        }
    }


    /**
     * 创建一个动态代理
     *
     * @param factory
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> T createCglibProxy(SqlFactory factory, T obj) {
        Class<?> clazz = obj.getClass();
        Object cacheObj = cacheMap.get(clazz);
        if (null == cacheObj) {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(obj.getClass());
            enhancer.setCallback(new JdbcCglibProxy(obj, factory));

            cacheObj = enhancer.create();
            cacheMap.put(clazz, cacheObj);
        }
        return (T) cacheObj;
    }


    @Override
    public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        AsyncMethod async = AnnotationUtils.findAnnotation(method, AsyncMethod.class);
        if (null == async) {
            //非异步方法
            return method.invoke(target, args);
        } else if (async.type().equals(AsyncOp.COMMON) && StringUtils.isBlank(async.sql())) {
            //普通类型的异步执行，并且没有提供SQL
            AsyncManager.getInstance().addAsyncCallBack(new DefaultAsyncCallback(method, target, args));
        } else {
            //异步SQL
            AsyncManager.getInstance().addAsyncCallBack(new DbAsyncCallback(async, entity, factory, method, target, args));
        }
        return null;
    }
}
