package com.reign.framework.core.mvc.result;

import com.reign.framework.core.mvc.servlet.ActionInvocation;

/**
 * @ClassName: ExceptionResult
 * @Description: 异常视图
 * @Author: wuwx
 * @Date: 2021-04-19 17:52
 **/
public class ExceptionResult implements Result<Throwable> {

    private Throwable t;

    public ExceptionResult(Throwable t) {
        this.t = t;
    }

    @Override
    public String getViewName() {
        return ActionInvocation.EXCEPTION;
    }

    @Override
    public Throwable getResult() {
        return t;
    }

    @Override
    public int getBytesLength() {
        return 0;
    }
}
