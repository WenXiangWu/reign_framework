package com.reign.framework.core.mvc.view;

import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;

/**
 * @ClassName: RpcResponseView
 * @Description: 响应视图类
 * @Author: wuwx
 * @Date: 2021-04-19 17:49
 **/
public interface RpcResponseView extends ResponseView{

    /**
     * 展现RpcResult
     *
     * @param result
     * @param request
     * @param response
     * @throws Exception
     */
    void renderRpc(Object result, Request request, Response response) throws Exception;
}
