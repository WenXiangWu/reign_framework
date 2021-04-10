package com.reign.framework.jdbc.orm;

import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.Params;
import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.SqlFactory;
import com.reign.framework.jdbc.handlers.BeanHandler;
import com.reign.framework.jdbc.handlers.BeanListHandler;
import com.reign.framework.jdbc.handlers.ColumnListHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: BaseDao
 * @Description: TODO
 * @Author: wuwx
 * @Date: 2021-04-02 16:36
 **/
public class BaseDao<T extends JdbcModel, PK extends Serializable> implements IBaseDao<T, PK>, InitializingBean {

    //当前类型
    private Class<T> clazz;

    //jdbc实例
    protected JdbcEntity entity;

    protected ResultSetHandler<T> handler;

    protected ResultSetHandler<List<T>> listHandler;

    //mapHandler
    protected ColumnListHandler columnListHandler;

    @Autowired
    protected JdbcFactory jdbcFactory;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected SqlFactory sqlFactory;

    public BaseDao() {

        try {
            clazz = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        } catch (Exception e) {
            clazz = (Class<T>) this.getClass().getGenericSuperclass();
        }

    }

    public void afterPropertiesSet() throws Exception {
        if (null == jdbcFactory) {
            return;
        }
        entity = jdbcFactory.getJdbcEntity(clazz);
        handler = new BeanHandler<>(clazz);
        listHandler = new BeanListHandler<>(clazz);
        columnListHandler = new ColumnListHandler(1);
    }

    public void create(T newInstance) {

    }

    public void create(T newInstance, boolean canDelay) {

    }

    public void create(T newInstance, boolean canDelay, String... keys) {

    }

    public T read(PK id) {
        return null;
    }

    public T readByIndex(Object[] keys) {
        return null;
    }

    public T readForUpdate(PK id) {
        return null;
    }

    public void update(T transientObject) {

    }

    public void update(T transientObject, String... keys) {

    }

    public void delete(PK id) {

    }

    public List<T> getModels() {
        return null;
    }

    public int getModelSize() {
        return 0;
    }

    public T getFirstResultByHQLAndParam(String sql) {
        return null;
    }

    public T getFirstResultByHQLAndParam(String sql, Params params) {
        return null;
    }

    public List<T> getResultByHQLAndParam(String sql) {
        return null;
    }

    public List<T> getResultByHQLAndParam(String sql, Params params) {
        return null;
    }

    public void update(String sql, Params params) {

    }

    public int update(String sql, Params params, boolean canDelay) {

        return 0;
    }

    public void update(String sql, Params params, PK pk, String... keys) {

    }

    public void update(String sql, Params params, boolean canDelay, PK pk, String... keys) {

    }

    public long count(String sql, Params params) {
        return 0;
    }

    public void batch(String sql, List<List<Param>> paramList) {

    }

    public void batch(String sql, List<List<Param>> paramList, String... keys) {

    }

    public List<Map<String, Object>> query(String sql, List<Param> paramList) {
        return null;
    }

    public <E> E query(String sql, List<Param> params, ResultSetHandler<E> handler) {
        return null;
    }
}
