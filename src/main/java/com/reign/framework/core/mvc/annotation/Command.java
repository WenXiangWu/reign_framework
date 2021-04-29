package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Command
 * @Description: 命令;用于方法上，表明是一个处理方法
 * @Author: wuwx
 * @Date: 2021-04-19 16:34
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Command {
    public String value();
}
