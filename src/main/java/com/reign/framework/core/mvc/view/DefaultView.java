package com.reign.framework.core.mvc.view;

import com.reign.framework.core.mvc.result.Result;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.util.WrapperUtil;

import java.io.IOException;

/**
 * @ClassName: DefaultView
 * @Description: 默认视图
 * @Author: wuwx
 * @Date: 2021-04-19 17:48
 **/
public abstract class DefaultView implements ResponseView {

    @Override
    public void render(Result<?> result, Request request, Response response) throws Exception {
        prepareRender(response);
        doRender(result, request, response);
    }


    /**
     * 打包
     *
     * @param request
     * @param body
     * @return
     * @throws IOException
     */
    public Object convert(Request request, byte[] body) throws IOException {
        return WrapperUtil.wrapper(request.getCommand(), request.getRequestId(), body, compress());
    }

    /**
     * 视图展现之前的准备工作
     *
     * @param response
     */
    public abstract void prepareRender(Response response);

    /**
     * 展示视图
     *
     * @param result
     * @param request
     * @param response
     * @throws IOException
     */
    public abstract void doRender(Object result, Request request, Response response) throws IOException;
}
