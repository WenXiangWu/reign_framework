package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: PrintArgsOff
 * @Description: 打印参数日志关闭注解
 * @Author: wuwx
 * @Date: 2021-04-19 16:34
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface PrintArgsOff {
}
