package com.reign.framework.jdbc;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: JdbcExtractor
 * @Description: 执行器
 * @Author: wuwx
 * @Date: 2021-04-08 09:49
 **/
public interface JdbcExtractor {

    /**
     * 查询获得结果集
     * @param sql sql语句
     * @param params 参数列表
     * @param handler 结果处理器
     * @param <T>
     * @return
     */
    <T> T query(String sql, List<Param> params,ResultSetHandler<T> handler);

    /**
     * 查询结果获得List<Map<String,Object>
     * @param sql
     * @param params
     * @return
     */
    List<Map<String,Object>> query(String sql, List<Param> params);

    /**
     * 更新db
     * @param sql
     * @param params
     * @return
     */
    int update(String sql,List<Param> params);

    /**
     * 插入db
     * @param sql
     * @param params
     * @param autoGenerator 主键是否自动生成
     * @return
     */
    int insert(String sql,List<Param> params,boolean autoGenerator);

    /**
     * batch处理
     * @param sql
     * @param paramList
     */
    void batch(String sql,List<List<Param>> paramList);


    /**
     * batch
     * @param sql
     * @param paramList
     * @param batchSize
     */
    void batch(String sql,List<List<Param>> paramList,int batchSize);

    /**
     * 进行batch处理，statement方式
     * @param sqlList
     */
    void batch(List<String> sqlList);


    /**
     * batch处理
     * @param sqlList
     * @param batchSize
     */
    void batch(List<String> sqlList,int batchSize);



}
