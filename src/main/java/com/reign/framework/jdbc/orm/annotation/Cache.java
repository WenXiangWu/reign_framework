package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Cache
 * @Description: 禁用缓存
 * @Author: wuwx
 * @Date: 2021-04-07 11:47
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Cache {

    /**
     * 是否禁用缓存
     * @return
     */
    boolean disable() default false;
}
