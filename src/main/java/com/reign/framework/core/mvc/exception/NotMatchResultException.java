package com.reign.framework.core.mvc.exception;

/**
 * @ClassName: NotMatchResultException
 * @Description: 不匹配的视图返回结果
 * @Author: wuwx
 * @Date: 2021-04-19 18:03
 **/
public class NotMatchResultException extends RuntimeException {

    public NotMatchResultException(String message, Class<?> clazz) {
        super(message + clazz.getName());
    }
}
