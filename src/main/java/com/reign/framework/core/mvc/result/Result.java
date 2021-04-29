package com.reign.framework.core.mvc.result;

/**
 * @ClassName: Result
 * @Description: 视图接口
 * @Author: wuwx
 * @Date: 2021-04-19 17:50
 **/
public interface Result<T> {

    /**
     * 视图名称
     * @return
     */
    String getViewName();

    /**
     * 返回结果视图
     * @return
     */
    T getResult();

    /**
     * 获取字节长度
     * @return
     */
    int getBytesLength();
}
