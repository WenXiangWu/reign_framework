package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: IgnoreField
 * @Description: 忽略的字段
 * @Author: wuwx
 * @Date: 2021-04-07 12:12
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface IgnoreField {
}
