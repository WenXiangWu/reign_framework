package com.reign.framework.core.mvc.annotation;

import java.lang.annotation.*;

/**
 * @ClassName: GlobalCacheParam
 * @Description: 全局缓存;用在参数上，标识其应该被加到全局缓存中；
 * @Author: wuwx
 * @Date: 2021-04-19 16:34
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface GlobalCacheParam {
}
