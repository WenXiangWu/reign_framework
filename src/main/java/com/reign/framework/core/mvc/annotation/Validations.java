package com.reign.framework.core.mvc.annotation;


import java.lang.annotation.*;

/**
 * @ClassName: Validations
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface Validations {
    Validation[] value();
}
