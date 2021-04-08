package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: InsertIgnoreField
 * @Description: 插入时忽略的字段
 * @Author: wuwx
 * @Date: 2021-04-07 14:12
 **/

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface InsertIgnoreField {
}
