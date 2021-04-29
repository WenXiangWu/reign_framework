package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: ChatTransactional
 * @Description: 聊天事务
 * @Author: wuwx
 * @Date: 2021-04-29 14:59
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ChatTransactional {
}
