package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Views
 * @Description: 用于类或方法上，可以配置多种视图
 * @Author: wuwx
 * @Date: 2021-04-19 16:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Views {
    View[] value();
}
