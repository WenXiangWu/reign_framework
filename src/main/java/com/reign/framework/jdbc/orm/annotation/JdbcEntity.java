package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: JdbcEntity
 * @Description: 实体类
 * @Author: wuwx
 * @Date: 2021-04-07 12:12
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface JdbcEntity {
}
