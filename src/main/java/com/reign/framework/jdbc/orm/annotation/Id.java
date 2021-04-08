package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Id
 * @Description: 主键类
 * @Author: wuwx
 * @Date: 2021-04-07 12:12
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface Id {
}
