package com.reign.framework.core.util;

import com.reign.framework.core.servlet.Wrapper;

/**
 * @ClassName: WrapperUtil
 * @Description: 包装器工具类
 * @Author: wuwx
 * @Date: 2021-04-12 16:51
 **/
public final class WrapperUtil {

    /**
     * CRLF
     */
    public static final byte[] CRLF = "\r\n".getBytes();

    /**
     * 空字节
     */
    public static final byte[] EMPTY_BYTE = "".getBytes();

    /**
     * 打包工具
     */
    public static Wrapper wrapper;

    public static synchronized void setWrapper(Wrapper wrapper) {
        if (wrapper != null) {
            throw new RuntimeException("wrapper has set already");
        }
        wrapper = wrapper;
    }

    public static Object wrapper(byte[] bytes) {
        return wrapper.wrapper(bytes);
    }


    public static Object wrapper(String command, int requestId, byte[] body) {
        return wrapper.wrapper(command, requestId, body);
    }

    public static byte[] wrapperBody(byte[] bytes, boolean compress) {
        return wrapper.wrapperBody(bytes, compress);
    }

    public static Object wrapper(String command, int requestId, byte[] body, boolean compress) {
        return wrapper.wrapper(command, requestId, body, compress);
    }


    public static Object wrapper(int requestId, byte[] body, boolean compress) {
        return wrapper.wrapper(requestId, body, compress);
    }

    public static Object wrapperChunk(byte[] bytes) {
        return wrapper.wrapperChunk(bytes);
    }

    public static String getContentType() {
        return wrapper.getContentType();
    }

    public static Object newWrapper(Object buffer) {
        return wrapper.newWrapper(buffer);
    }


    public static Object getByteBufAllocator() {
        return wrapper.getByteBufAllocator();
    }
}
