package com.reign.framework.jdbc.async;

import com.reign.framework.log.Logger;

/**
 * @ClassName: AsyncCallback
 * @Description: SQL异步执行回调
 * @Author: wuwx
 * @Date: 2021-04-07 18:22
 **/
public interface AsyncCallback {


    /**
     * 回调函数
     */
    void callback();

    /**
     * 记录日志
     * @param log
     * @param type  类型 1插入队列  2成功执行 3  执行失败
     */
    void doLog(Logger log,int type);
}
