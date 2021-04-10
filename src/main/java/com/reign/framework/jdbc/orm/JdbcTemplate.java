package com.reign.framework.jdbc.orm;

import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.orm.extractor.BaseJdbcExtractor;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: JdbcTemplate
 * @Description: jdbc模板类
 * @Author: wuwx
 * @Date: 2021-04-02 14:51
 **/
public class JdbcTemplate implements BaseJdbcExtractor {
    @Override
    public <T, PK> T read(PK pk, JdbcEntity entity, ResultSetHandler<T> handler) {
        return null;
    }

    @Override
    public <T, PK> T readByIndex(Object[] indexs, JdbcEntity entity, ResultSetHandler<T> handler) {
        return null;
    }

    @Override
    public <T, PK> void insert(T newInstance, JdbcEntity entity, String... keys) {

    }

    @Override
    public <T, PK> void insertDelay(T newInstance, JdbcEntity entity, String... keys) {

    }

    @Override
    public <T> void update(T transientObject, JdbcEntity entity) {

    }

    @Override
    public <PK> void delete(PK id, JdbcEntity entity) {

    }

    @Override
    public <T> List<T> query(String selectKey, String sql, List<Param> params, JdbcEntity entity, ResultSetHandler<T> handler) {
        return null;
    }

    @Override
    public <PK> int update(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        return 0;
    }

    @Override
    public <PK> int updateDelay(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        return 0;
    }

    @Override
    public void batch(String sql, List<Param> params, JdbcEntity entity, String... keys) {

    }

    @Override
    public <T> T query(String sql, List<Param> params, JdbcEntity entity, ResultSetHandler<T> handler) {
        return null;
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params, JdbcEntity entity) {
        return null;
    }

    @Override
    public <T> T query(String sql, List<Param> params, ResultSetHandler<T> handler) {
        return null;
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params) {
        return null;
    }

    @Override
    public int update(String sql, List<Param> params) {
        return 0;
    }

    @Override
    public int insert(String sql, List<Param> params, boolean autoGenerator) {
        return 0;
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList) {

    }

    @Override
    public void batch(String sql, List<List<Param>> paramList, int batchSize) {

    }

    @Override
    public void batch(List<String> sqlList) {

    }

    @Override
    public void batch(List<String> sqlList, int batchSize) {

    }
}
