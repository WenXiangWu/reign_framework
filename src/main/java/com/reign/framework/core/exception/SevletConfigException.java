package com.reign.framework.core.exception;

/**
 * @ClassName: SevletConfigException
 * @Description: 服务器配置异常
 * @Author: wuwx
 * @Date: 2021-04-15 10:48
 **/
public class SevletConfigException extends RuntimeException{

    public SevletConfigException(String message) {
        super(message);
    }

    public SevletConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}
