package com.reign.framework.core.mvc.view;

import com.reign.framework.common.ServerProtocol;
import com.reign.framework.core.mvc.exception.NotMatchResultException;
import com.reign.framework.core.mvc.result.ByteResult;
import com.reign.framework.core.servlet.Request;
import com.reign.framework.core.servlet.Response;
import com.reign.framework.core.util.WrapperUtil;

import java.io.IOException;

/**
 * @ClassName: ByteView
 * @Description: 字节视图
 * @Author: wuwx
 * @Date: 2021-04-19 17:48
 **/
public class ByteView extends DefaultView {

    //是否压缩
    private boolean compress;

    @Override
    public void prepareRender(Response response) {
        //do nothing
    }

    @Override
    public void doRender(Object result, Request request, Response response) throws IOException {
        if (!(result instanceof ByteResult)) {
            throw new NotMatchResultException("un match result type,except", ByteResult.class);
        }
        ByteResult byteResult = (ByteResult) request;
        if (ServerProtocol.TCP.equals(response.getProtocol())) {
            //如果是TCP协议
            response.write(convert(request, byteResult.getResult()));
        }else if (ServerProtocol.WEBSOCKET.equals(response.getProtocol())){
            //如果是webSocket协议
            response.write(byteResult.getResult());
        }else {
            //如果是HTTP协议
            response.write(WrapperUtil.wrapperBody(byteResult.getResult(),compress));

        }

    }

    @Override
    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    @Override
    public boolean compress() {
        return compress;
    }
}
