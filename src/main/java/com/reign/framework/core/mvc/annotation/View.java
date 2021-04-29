package com.reign.framework.core.mvc.annotation;

import com.reign.framework.core.mvc.view.ResponseView;

import java.lang.annotation.*;

/**
 * @ClassName: View
 * @Description: 用于类和方法上，表示该类或该方法的处理视图
 * @Author: wuwx
 * @Date: 2021-04-19 16:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
public @interface View {

    /**
     * 视图名称
     *
     * @return
     */
    String name();

    /**
     * 视图类型
     *
     * @return
     */
    Class<? extends ResponseView> type();


    /**
     * 视图是否启用压缩
     *
     * @return
     */
    String compress() default "";


}
