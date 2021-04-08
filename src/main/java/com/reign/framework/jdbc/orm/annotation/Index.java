package com.reign.framework.jdbc.orm.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Index
 * @Description: 索引
 * @Author: wuwx
 * @Date: 2021-04-07 14:36
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface Index {

    /**
     * 索引字段
     * @return
     */
    String[] value();

}
