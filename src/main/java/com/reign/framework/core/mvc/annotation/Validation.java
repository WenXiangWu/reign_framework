package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Validation
 * @Description: 验证annotation
 * @Author: wuwx
 * @Date: 2021-04-19 16:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface Validation {

    /**
     * 处理器
     * @return
     */
    Class<? extends com.reign.framework.core.mvc.validation.Validation> handler();

    /**
     * 验证规则
     */
    Rule rule();
}
