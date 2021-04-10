package com.reign.framework.jdbc.orm.extractor;

/**
 * @ClassName: TransCallBack
 * @Description: 事务回调函数
 * @Author: wuwx
 * @Date: 2021-04-08 18:13
 **/
public interface TransCallBack {

    /**
     * 执行缓存函数
     * @throws Exception
     */
    void execute() throws Exception;
}
