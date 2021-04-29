package com.reign.framework.exception;

/**
 * @ClassName: ServletException
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-29 15:50
 **/
public class ServletException extends RuntimeException {

    private static final long serialVersionUID = -1L;

    public ServletException(String message) {
        super(message);
    }
}
