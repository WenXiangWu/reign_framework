package com.reign.framework.jdbc.orm.extractor;

import com.reign.framework.jdbc.AbstractJdbcExtractor;
import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.orm.JdbcEntity;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: CommonJdbcExtractor
 * @Description: 普通的JdbcExtractor不走缓存通道
 * @Author: wuwx
 * @Date: 2021-04-08 18:13
 **/
public class CommonJdbcExtractor extends AbstractJdbcExtractor implements BaseJdbcExtractor {


    @Override
    public <T, PK> T read(PK pk, JdbcEntity entity, ResultSetHandler<T> handler) {
        return query(entity.getSelectSQL(false), entity.builderSelectParams(pk), handler);
    }

    @Override
    public <T, PK> T readByIndex(Object[] indexs, JdbcEntity entity, ResultSetHandler<T> handler) {
        return query(entity.getIndex().selectSQL(), entity.getIndex().builderParams(indexs), handler);
    }

    @Override
    public <T, PK> void insert(T newInstance, JdbcEntity entity, String... keys) {
        int result = insert(entity.getInsertSQL(), entity.builderInsertParams(newInstance), entity.isAutoGenerator());
    }

    @Override
    public <T, PK> void insertDelay(T newInstance, JdbcEntity entity, String... keys) {
        insert(newInstance, entity, keys);
    }

    @Override
    public <T> void update(T transientObject, JdbcEntity entity) {
        update(entity.getUpdateSQL(), entity.builderUpdateParams(transientObject));
    }

    @Override
    public <PK> void delete(PK id, JdbcEntity entity) {
        update(entity.getDeleteSQL(), entity.builderSelectParams(id));
    }

    @Override
    public <T> List<T> query(String selectKey, String sql, List<Param> params, JdbcEntity entity, ResultSetHandler<List<T>> handler) {
        return query(sql, params, handler);
    }

    @Override
    public <T> T query(String sql, List<Param> params, ResultSetHandler<T> handler) {
        return super.query(sql, params, handler);
    }

    @Override
    public <PK> int update(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        return update(sql, params);
    }

    @Override
    public <PK> void updateDelay(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        update(sql, params, entity, pk, keys);
    }

    @Override
    public void batch(String sql, List<List<Param>> params, JdbcEntity entity, String... keys) {
        batch(sql, params);
    }

    @Override
    public <T> T query(String sql, List<Param> params, JdbcEntity entity, ResultSetHandler<T> handler) {
        return query(sql, params, handler);
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params, JdbcEntity entity) {
        return query(sql, params);
    }
}
