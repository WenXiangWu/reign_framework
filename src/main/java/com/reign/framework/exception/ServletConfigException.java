package com.reign.framework.exception;

/**
 * @ClassName: ServletConfigException
 * @Description: servlet配置异常
 * @Author: wuwx
 * @Date: 2021-04-29 15:39
 **/
public class ServletConfigException extends RuntimeException{

    private static final long serialVersionUID = -1L;

    public ServletConfigException(String message) {
        super(message);
    }

    public ServletConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
