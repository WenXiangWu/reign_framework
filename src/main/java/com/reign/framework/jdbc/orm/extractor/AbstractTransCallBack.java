package com.reign.framework.jdbc.orm.extractor;

/**
 * @ClassName: AbstractTransCallBack
 * @Description: 事务回调
 * @Author: wuwx
 * @Date: 2021-04-08 18:12
 **/
public abstract class AbstractTransCallBack implements TransCallBack{

    /**是否需要在事务成功之后执行*/
    public boolean doInTransactionSucc;

    public AbstractTransCallBack(boolean doInTransactionSucc) {
        this.doInTransactionSucc = doInTransactionSucc;
    }
}
