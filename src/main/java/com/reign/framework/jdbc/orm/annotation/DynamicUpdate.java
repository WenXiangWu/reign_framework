package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: DynamicUpdate
 * @Description: 动态更新
 * @Author: wuwx
 * @Date: 2021-04-07 11:49
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface DynamicUpdate {
}
