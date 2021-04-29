package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: RpcCommand
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public  @interface  RpcCommand {

    String value();

    String viewName();
}
