package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Action
 * @Description: 用于类上，标识是一个处理类
 * @Author: wuwx
 * @Date: 2021-04-19 16:33
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Action {
}
