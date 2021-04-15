package com.reign.framework.core.servlet;

import com.reign.framework.common.ServerConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

/**
 * @ClassName: AbstractWrapper
 * @Description: 包装器
 * @Author: wuwx
 * @Date: 2021-04-15 10:02
 **/
public abstract class AbstractWrapper implements Wrapper {

    /**
     * 是否开启压缩
     */
    private boolean compress;


    public AbstractWrapper(boolean compress) {
        this.compress = compress;
    }

    @Override
    public boolean compress() {
        return compress;
    }


    @Override
    public String getContentType() {
        if (compress) {
            return ServerConstants.CONTENT_TYPE_COMPRESSED;
        }
        return ServerConstants.CONTENT_TYPE;
    }


    @Override
    public byte[] wrapperBody(byte[] bytes) {
        return wrapperBody(bytes, compress);
    }

    @Override
    public byte[] wrapperBody(byte[] bytes, boolean compress) {
        if (compress) {
            //压缩
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            DeflaterOutputStream dis = new DeflaterOutputStream(out);
            try {
                dis.write(bytes);
                dis.finish();
                dis.close();
                return out.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException("compress error", e);
            } finally {
                if (null != dis) {
                    try {
                        dis.close();
                    } catch (IOException e) {

                    }
                }
            }

        } else {
            return bytes;
        }
    }

    @Override
    public void retain(Object msg) {

    }

    @Override
    public void release(Object msg) {

    }
}
