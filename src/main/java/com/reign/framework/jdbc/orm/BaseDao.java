package com.reign.framework.jdbc.orm;

import com.reign.framework.jdbc.*;
import com.reign.framework.jdbc.handlers.BeanHandler;
import com.reign.framework.jdbc.handlers.BeanListHandler;
import com.reign.framework.jdbc.handlers.ColumnListHandler;
import com.reign.framework.jdbc.orm.cache.CacheStatistics;
import com.reign.framework.jdbc.orm.extractor.CacheLoader;
import com.reign.framework.jdbc.orm.page.PagingData;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: BaseDao
 * @Description: dao层实现
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

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.framework.jdbc.cache");

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
        create(newInstance, false, ALL_QUERY_CACHE);
    }

    public void create(T newInstance, boolean canDelay) {
        create(newInstance, canDelay, ALL_QUERY_CACHE);
    }

    public void create(T newInstance, boolean canDelay, String... keys) {
        if (canDelay) {
            jdbcTemplate.insertDelay(newInstance, entity, keys);
        } else {
            jdbcTemplate.insert(newInstance, entity, keys);
        }
    }

    public T read(PK id) {
        return jdbcTemplate.read(id, entity, handler);
    }

    public T readByIndex(Object[] keys) {
        return jdbcTemplate.readByIndex(keys, entity, handler);
    }

    public T readForUpdate(PK id) {
        return jdbcTemplate.query(entity.getSelectSQL(true), entity.builderSelectParams(id), entity, handler);
    }

    public void update(T transientObject) {
        jdbcTemplate.update(transientObject, entity);
    }

    public void update(T transientObject, String... keys) {
        jdbcTemplate.update(entity.getUpdateSQL(), entity.builderUpdateParams(transientObject), entity, entity.getId().getIdValue(transientObject), keys);
    }

    public void delete(PK id) {
        jdbcTemplate.delete(id, entity);
    }

    public List<T> getModels() {
        return jdbcTemplate.query(entity.getSelectAllSQL(), entity.getSelectAllSQL(), Params.EMPTY, entity, listHandler);
    }

    public Long getModelSize() {
        List<Object> resultList = jdbcTemplate.query(entity.getSelectAllCountSQL(), Params.EMPTY, entity, columnListHandler);
        if (resultList.size() > 0) {
            return (Long) resultList.get(0);
        }
        return 0L;
    }

    @Override
    public T getFirstResultByHQLAndParam(String sql) {
        List<T> resultList = jdbcTemplate.query(sql, sql, Params.EMPTY, entity, listHandler);
        if (resultList.size() > 0) {
            return resultList.get(0);
        }
        return null;
    }

    public T getFirstResultByHQLAndParam(String sql, Params params) {
        List<T> resultList = jdbcTemplate.query(sql, sql, params, entity, listHandler);
        if (resultList.size() > 0) {
            return resultList.get(0);
        }
        return null;
    }

    public List<T> getResultByHQLAndParam(String sql) {
        return jdbcTemplate.query(sql, sql, Params.EMPTY, entity, listHandler);

    }

    public List<T> getResultByHQLAndParam(String sql, Params params) {
        return jdbcTemplate.query(sql, sql, params, entity, listHandler);
    }

    public List<T> getResultByHQLAndParam(String sql, PagingData pagingData, Params params) {
        String temp = sqlFactory.get(sql);
        String selectKey = sql;
        if (null != temp) {
            sql = temp.trim();
        }
        //先查询分页
        String countSql = getCountSql(sql);
        long count = getCount(countSql, params);
        //更新分页
        pagingData.setRowsCount((int) count);
        pagingData.setPagesCount();

        sql += " LIMIT ?,?";
        params.addParam(pagingData.getCurrentPage() * pagingData.getRowsPerPage(), Type.Int);
        params.addParam(pagingData.getRowsPerPage(), Type.Int);
        return jdbcTemplate.query(selectKey, sql, params, entity, listHandler);
    }

    @Override
    public List<Map<String, Object>> query(String sql, PagingData page, Params params) {
        String temp = sqlFactory.get(sql);
        if (null != temp) {
            sql = temp.trim();
        }
        //先查询分页
        String countSql = getCountSql(sql);
        long count = getCount(countSql, params);
        //更新分页
        page.setRowsCount((int) count);
        page.setPagesCount();

        sql += " LIMIT ?,?";
        params.addParam(page.getCurrentPage() * page.getRowsPerPage(), Type.Int);
        params.addParam(page.getRowsPerPage(), Type.Int);
        return jdbcTemplate.query(sql, params, entity);
    }

    public void update(String sql, Params params) {
        update(sql, params, true);
    }

    public int update(String sql, Params params, boolean canDelay) {
        return update(sql, params, canDelay, null, ALL_QUERY_CACHE);
    }

    public void update(String sql, Params params, PK pk, String... keys) {
        update(sql, params, true, pk, keys);
    }

    public int update(String sql, Params params, boolean canDelay, PK pk, String... keys) {
        if (canDelay) {
            jdbcTemplate.updateDelay(sql, params, null, keys);
            return 0;
        }
        return jdbcTemplate.update(sql, params, entity, pk, keys);
    }

    public long count(String sql, Params params) {
        return 0;
    }

    /**
     * count查询
     *
     * @param sql
     * @param params
     * @return
     */
    private long getCount(String sql, Params params) {
        //先从查询缓存中获取
        String selectKeys = builderSelectKey(sql, params);
        String[] result = CacheLoader.getFromQueryCache(entity, selectKeys);
        if (null != result && result.length == 1) {
            if (log.isDebugEnabled()) {
                log.debug("querycache hit,table:{},index:{}", entity.getTableName(), selectKeys);
            }
            CacheStatistics.addQueryHits(entity.getTableName());
            return Long.valueOf(result[0]);
        }
        //从数据库中获取
        List<Object> resultList = jdbcTemplate.query(sql, params, entity, columnListHandler);
        Long value = 0L;
        if (resultList.size() > 0) {
            value = (Long) resultList.get(0);
        }
        //放入查询缓存
        if (log.isDebugEnabled()) {
            log.debug("querycache miss,table:{},index:{}", entity.getTableName(), selectKeys);
        }
        CacheLoader.putToQueryCache(entity, selectKeys, new String[]{String.valueOf(value)});
        CacheStatistics.addQueryMiss(entity.getTableName());
        return value;
    }

    private String getCountSql(String sql) {
        return "SELECT COUNT(1) " + sql.toUpperCase().substring(sql.indexOf("FROM"));
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList) {
        jdbcTemplate.batch(sql, paramList, entity, ALL_QUERY_CACHE);
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList, String... keys) {
        jdbcTemplate.batch(sql, paramList, entity, keys);
    }

    public List<Map<String, Object>> query(String sql, List<Param> paramList) {
        return jdbcTemplate.query(sql, paramList, entity);
    }

    public <E> E query(String sql, List<Param> params, ResultSetHandler<E> handler) {
        return jdbcTemplate.query(sql, params, entity, handler);
    }

    /**
     * 构建查询缓存key
     *
     * @param sql
     * @param params
     * @return
     */
    protected final String builderSelectKey(String sql, List<Param> params) {
        StringBuilder builder = new StringBuilder();
        builder.append(sql).append("::");
        for (Param param : params) {
            builder.append(param.obj.toString()).append(",");
        }
        return builder.toString();
    }
}
