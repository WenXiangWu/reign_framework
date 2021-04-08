package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: AsyncMethod
 * @Description: 标识该方法是需要异步执行的方法
 * @Author: wuwx
 * @Date: 2021-04-07 18:37
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface AsyncMethod {

    /**
     * 如果SQL不为空，则会根据方法的参数format sql，记录日志
     * @return
     */
    String sql() default "";

    /**
     * 异步操作的类型
     * @return
     */
    AsyncOp type() default AsyncOp.COMMON;
}
