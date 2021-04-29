package com.reign.framework.core.mvc.result;

/**
 * @ClassName: NullResult
 * @Description: 空视图
 * @Author: wuwx
 * @Date: 2021-04-19 17:52
 **/
public class NullResult implements Result<Object> {
    @Override
    public String getViewName() {
        return "null";
    }

    @Override
    public Object getResult() {
        return null;
    }

    @Override
    public int getBytesLength() {
        return 0;
    }
}
