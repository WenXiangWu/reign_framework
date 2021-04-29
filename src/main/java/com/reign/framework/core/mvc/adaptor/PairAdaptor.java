package com.reign.framework.core.mvc.adaptor;

import com.reign.framework.core.mvc.adaptor.inject.ArrayInjector;
import com.reign.framework.core.mvc.adaptor.inject.NameInjector;
import com.reign.framework.core.mvc.adaptor.inject.NullInjector;
import com.reign.framework.core.mvc.adaptor.inject.ParamInjector;
import com.reign.framework.core.mvc.annotation.RequestParam;

/**
 * @ClassName: PairAdaptor
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-19 16:32
 **/
public class PairAdaptor extends AbstractAdaptor {
    @Override
    public ParamInjector evalInjector(Class<?> clazz, RequestParam requestParam) {
        if (null == requestParam) return new NullInjector(clazz);
        if (clazz.isArray()){
            return new ArrayInjector(requestParam.value(),clazz);
        }
        return new NameInjector(requestParam.value(),clazz);
    }
}
