package com.reign.framework.jdbc.orm.extractor;

import com.reign.framework.jdbc.JdbcExtractor;
import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.orm.JdbcEntity;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: BaseJdbcExtractor
 * @Description: BaseJdbcExtractor
 * @Author: wuwx
 * @Date: 2021-04-08 18:13
 **/
public interface BaseJdbcExtractor extends JdbcExtractor {

    /**
     * 按照主键从数据库中检索结果
     *
     * @param pk
     * @param entity
     * @param handler
     * @param <T>
     * @param <PK>
     */
    <T, PK> T read(final PK pk, final JdbcEntity entity, final ResultSetHandler<T> handler);


    /**
     * 按照索引从数据库中检索结果
     *
     * @param indexs
     * @param entity
     * @param handler
     * @param <T>
     * @param <PK>
     * @return
     */
    <T, PK> T readByIndex(final Object[] indexs, final JdbcEntity entity, final ResultSetHandler<T> handler);


    /**
     * 插入
     *
     * @param newInstance 插入的实体
     * @param entity      实体
     * @param keys        要清除的查询缓存，如果为null，表示清理所有查询缓存
     * @param <T>         实体类
     * @param <PK>        主键值
     */
    <T, PK> void insert(final T newInstance, final JdbcEntity entity, final String... keys);


    <T, PK> void insertDelay(final T newInstance, final JdbcEntity entity, final String... keys);


    <T> void update(final T transientObject, final JdbcEntity entity);

    <PK> void delete(final PK id, final JdbcEntity entity);

    <T> List<T> query(final String selectKey, final String sql, final List<Param> params, final JdbcEntity entity, final ResultSetHandler<T> handler);

    <PK> int update(final String sql, final List<Param> params, final JdbcEntity entity, final PK pk, final String... keys);

    /**
     * 更新db，可以延迟执行
     *
     * @param sql
     * @param params
     * @param entity
     * @param pk
     * @param keys
     * @param <PK>
     * @return
     */
    <PK> int updateDelay(final String sql, final List<Param> params, final JdbcEntity entity, final PK pk, final String... keys);


    /**
     * batch处理
     *
     * @param sql
     * @param params
     * @param entity
     * @param keys
     */
    void batch(final String sql, final List<Param> params, final JdbcEntity entity, final String... keys);


    <T> T query(String sql, List<Param> params, final JdbcEntity entity, final ResultSetHandler<T> handler);

    /**
     * 查询结果返回list
     * @param sql
     * @param params
     * @param entity
     * @return
     */
    List<Map<String,Object>> query(String sql,List<Param> params,final JdbcEntity entity);
}
