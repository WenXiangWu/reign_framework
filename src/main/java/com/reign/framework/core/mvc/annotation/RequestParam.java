package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: RequestParam
 * @Description: 参数注解，用于方法参数上
 * @Author: wuwx
 * @Date: 2021-04-19 16:34
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface RequestParam {
    String value();
}
