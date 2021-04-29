package com.reign.framework.core.mvc.validation;

import com.reign.framework.core.mvc.result.Result;
import com.reign.framework.core.servlet.Request;

/**
 * @ClassName: Validation
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-19 17:43
 **/
public interface Validation {

    /**
     * 验证结果，如果通过验证则返回null，否则返回Action支持的视图
     *
     * @param request
     * @param rule
     * @return
     */
    Result<?> validate(Request request, Rule<?> rule);
}
