package com.reign.framework.core.mvc.validation;

/**
 * @ClassName: Rule
 * @Description: 规则
 * @Author: wuwx
 * @Date: 2021-04-19 17:39
 **/

public interface Rule<T> {

    /**
     * 获取规则对象
     *
     * @return
     */
    T getRule();

    /**
     * 解析表达式
     *
     * @param rule
     */
    void parse(String rule);
}
