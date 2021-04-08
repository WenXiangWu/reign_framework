package com.reign.framework.jdbc.orm;

import com.reign.framework.jdbc.Param;

import java.util.List;

/**
 * @ClassName: IndexEntity
 * @Description: 缓存index的实体
 * @Author: wuwx
 * @Date: 2021-04-02 14:50
 **/
public interface IndexEntity {

    /**
     * 初始化实体
     */
    void  init();

    /**
     * 获取依靠索引用于查询语句
     * @return
     */
    String selectSQL();

    /**
     * 获取依靠索引用于查询需要构建的参数列表
     * @param args
     * @return
     */
    List<Param> builderParams(Object... args);


    /**
     * 用于缓存
     * @param args
     * @return
     */
    String getKeyValueByParams(Object... args);


    /**
     * 用于缓存
     * @param obj
     * @return
     */
    String getKeyValueByObject(Object obj);

}
