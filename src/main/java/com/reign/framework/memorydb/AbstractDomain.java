package com.reign.framework.memorydb;

import com.reign.framework.jdbc.orm.annotation.Cache;
import com.reign.framework.jdbc.orm.annotation.DynamicUpdate;
import com.reign.framework.jdbc.orm.JdbcModel;

/**
 * @ClassName: AbstractDomain
 * @Description: 抽象实体
 * @Author: wuwx
 * @Date: 2021-04-02 10:42
 **/
@Cache(disable = true)
@DynamicUpdate
public abstract class AbstractDomain implements JdbcModel {

    private static final long serialVersionUid = 1L;

    /**老值*/
    protected AbstractDomain old;

    /**是否已经是托管对象*/
    public boolean managed;

    /**是否被标记过*/
    public boolean marked;


    /**
     * 做一个标记，当更新到索引的时候需要用到
     */
    public void mark(){
        if (!managed || marked){
            return;
        }
        marked =true;
        markOld();
    }

    /**
     * 设置老值
     */
    public void markOld() {
        if (null!=old){
            return;
        }
        old = (AbstractDomain) clone();
    }


    /**
     * 重置
     */
    public void reset(){
        if (!managed){
            return;
        }
        marked = false;
        old = null;
        markOld();

    }


    /**
     * 获取老值
     * @return
     */
    public AbstractDomain oldDomain(){
        return old;
    }

    public abstract Object clone();
}
