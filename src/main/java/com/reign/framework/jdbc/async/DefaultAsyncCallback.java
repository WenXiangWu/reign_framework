package com.reign.framework.jdbc.async;

import com.reign.framework.log.Logger;

import java.lang.reflect.Method;

/**
 * @ClassName: DefaultAsyncCallback
 * @Description: 默认异步回调
 * @Author: wuwx
 * @Date: 2021-04-07 18:30
 **/
public class DefaultAsyncCallback implements AsyncCallback {
    private Method method;
    private Object obj;
    private Object[] args;

    public DefaultAsyncCallback(Method method, Object obj, Object[] args) {
        this.method = method;
        this.obj = obj;
        this.args = args;
    }

    @Override
    public void callback() {
        try {
            method.invoke(obj, args);
        } catch (Throwable t) {
            throw new RuntimeException("DefaultAsyncCallback callback failed +" + obj);
        }
    }

    @Override
    public void doLog(Logger log, int type) {

    }
}
