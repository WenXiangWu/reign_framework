package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Async
 * @Description: 标识该类中有需要异步执行的方法
 * @Author: wuwx
 * @Date: 2021-04-08 15:14
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Async {
}
