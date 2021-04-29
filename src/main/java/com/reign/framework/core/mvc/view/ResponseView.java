package com.reign.framework.core.mvc.view;

import com.reign.framework.core.mvc.result.Result;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;

/**
 * @ClassName: ResponseView
 * @Description: 响应视图类
 * @Author: wuwx
 * @Date: 2021-04-19 17:45
 **/
public interface ResponseView {

    /**
     * 展现视图
     *
     * @param result
     * @param request
     * @param response
     * @throws Exception
     */
    void render(Result<?> result, Request request, Response response) throws Exception;

    /**
     * 设置是否需要压缩
     *
     * @param compress
     */
    void setCompress(boolean compress);

    /**
     * 是否压缩
     *
     * @return
     */
    boolean compress();
}
