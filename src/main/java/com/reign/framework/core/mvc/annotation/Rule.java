package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: Rule
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 16:35
 **/

public  @interface  Rule {

    /**
     * 规则
     * @return
     */
    Class<? extends com.reign.framework.core.mvc.validation.Rule<?>> rule();

    /**
     * 表达式
     * @return
     */
    String expression();
}
