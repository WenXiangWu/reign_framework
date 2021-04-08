package com.reign.framework.memorydb;

import com.reign.framework.jdbc.orm.BaseDao;
import org.springframework.beans.factory.InitializingBean;

import java.io.Serializable;

/**
 * @ClassName: BaseManager
 * @Description: 基础管理器
 * @Author: wuwx
 * @Date: 2021-04-02 10:42
 **/
public abstract class BaseManager<T extends AbstractDomain,K extends Serializable> extends BaseDao<T,K> implements InitializingBean {
}
