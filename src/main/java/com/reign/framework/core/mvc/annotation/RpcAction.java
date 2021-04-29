package com.reign.framework.core.mvc.annotation;

import com.reign.framework.core.mvc.adaptor.RpcAdaptor;

import java.lang.annotation.*;

/**
 * @ClassName: RpcAction
 * @Description: 用于类上，标识是一个RPC处理类
 * rpc的compress配置将覆盖具体视图配置，视图配置下的compress在rpc模式下无效
 * @Author: wuwx
 * @Date: 2021-04-19 16:35
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface RpcAction {

    /**
     * 适配器
     *
     * @return
     */
    Class<? extends RpcAdaptor> adaptor();

    /**
     * 视图名称
     *
     * @return
     */
    String viewName();

    /**
     * 视图是否采用压缩
     *
     * @return
     */
    String compress() default "";
}
