package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: NoCache
 * @Description: 禁用缓存
 * @Author: wuwx
 * @Date: 2021-04-08 15:15
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Deprecated
public @interface NoCache {
}
