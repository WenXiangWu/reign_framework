package com.reign.framework.core.mvc.view;

import com.reign.framework.core.mvc.result.Result;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;

/**
 * @ClassName: NullView
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 17:49
 **/
public class NullView implements ResponseView {
    @Override
    public void render(Result<?> result, Request request, Response response) throws Exception {
        //do nothing

    }

    @Override
    public void setCompress(boolean compress) {
        return;
    }

    @Override
    public boolean compress() {
        return false;
    }
}
