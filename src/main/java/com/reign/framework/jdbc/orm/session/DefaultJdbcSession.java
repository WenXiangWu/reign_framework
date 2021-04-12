package com.reign.framework.jdbc.orm.session;

import com.reign.framework.common.Lang;
import com.reign.framework.common.util.MessageFormatter;
import com.reign.framework.common.util.Tuple;
import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.orm.IDynamicUpdate;
import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.JdbcFactory;
import com.reign.framework.jdbc.orm.JdbcModel;
import com.reign.framework.jdbc.orm.extractor.BaseJdbcExtractor;
import com.reign.framework.jdbc.orm.transaction.JdbcTransaction;
import com.reign.framework.jdbc.orm.transaction.Transaction;
import com.reign.framework.jdbc.orm.transaction.TransactionListener;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

/**
 * @ClassName: DefaultJdbcSession
 * @Description: 默认的JdbcSession实现
 * @Author: wuwx
 * @Date: 2021-04-08 18:14
 **/
public class DefaultJdbcSession implements JdbcSession, TransactionListener {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.framework.jdbc");

    /**
     * 连接
     */
    private Connection connection;

    /**
     * 连接池
     */
    private DataSource dataSource;

    /**
     * 事务
     */
    private Transaction transaction;

    /**
     * 是否存在事务
     */
    private boolean hasTransaction;

    /**
     * 关闭标识
     */
    private boolean closed;

    /**
     * 一级缓存
     */
    private Map<JdbcEntity, Map<String, Object>> cache;

    /**
     * 原始对象
     */
    private Map<JdbcEntity, Map<String, Object>> oldCache;

    /**
     * jdbcExtractor
     */
    private BaseJdbcExtractor jdbcExtractor;

    /**
     * factory
     */
    private JdbcFactory jdbcFactory;

    /**
     * 缓存禁用更改过的entity
     */
    private Set<JdbcEntity> entitySet;

    /**
     * 用于延迟执行SQL
     */
    private List<DefaultJdbcSessionTrigger> triggerList;

    /**
     * 触发器 表set
     */
    private Set<String> triggerTableSet;


    public DefaultJdbcSession(DataSource dataSource, JdbcFactory jdbcFactory) {
        this.connection = DataSourceUtils.getConnection(dataSource);
        this.dataSource = dataSource;
        this.jdbcFactory = jdbcFactory;
        this.jdbcExtractor = jdbcFactory.getBaseJdbcExtractor();
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public Transaction getTransaction() {
        if (transaction == null) {
            transaction = new JdbcTransaction(this, connection, jdbcFactory);
        }
        hasTransaction = true;
        return transaction;
    }


    /**
     * 添加到实体set
     *
     * @param entity
     */
    private void addToEntitySet(JdbcEntity entity) {
        if (null == entitySet) {
            entitySet = new HashSet<>();
        }
        entitySet.add(entity);
    }

    private void addTrigger(String tableName, int triggerType, boolean triggerNow, JdbcEntity entity, JdbcSessionTrigger trigger) {
        if (triggerNow) {
            trigger.trigger();
            return;
        }
        if (triggerList == null) {
            triggerList = new ArrayList<>(8);
        }

        if (triggerTableSet == null) {
            triggerTableSet = new HashSet<>(8);
        }

        triggerList.add(new DefaultJdbcSessionTrigger(entity, tableName, triggerType, trigger));
        if (triggerTableSet.contains(tableName)) {
            //存在同一张表的SQL还未执行，则立即触发执行
            triggerNow();

        } else {
            triggerTableSet.add(tableName);
        }
    }

    /**
     * 立即触发
     */
    private void triggerNow() {
        if (triggerList == null || triggerList.size() == 0) {
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("trigger delay sql start");
        }

        //排序
        Collections.sort(triggerList);

        //触发触发器
        for (JdbcSessionTrigger trigger : triggerList) {
            trigger.trigger();
        }
        //清理
        triggerList.clear();
        triggerTableSet.clear();
        if (log.isDebugEnabled()) {
            log.debug("trigger delay sql end");
        }
    }


    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (isClosed()) {
            throw new RuntimeException("session is already closed");
        }
        //清理缓存
        //清理缓存
        if (null != cache) {
            cache.clear();
            cache = null;
        }

        //清理缓存
        if (null != oldCache) {
            oldCache.clear();
            oldCache = null;
        }

        //关闭事务
        if (null != transaction && transaction.isActive()) {
            transaction.commit();
        }

        //清理连接
        DataSourceUtils.releaseConnection(connection, dataSource);

        //重置缓存状态
        if (null != entitySet) {
            for (JdbcEntity entity : entitySet) {
                entity.getCacheManger().resetCacheFlag();
            }
        }

        //清理未触发的SQL
        if (null != triggerList) {
            triggerList.clear();
        }
        if (null != triggerTableSet) {
            triggerTableSet.clear();
        }
        //设置关闭完成
        closed = true;
    }

    @Override
    public void evict(String key, JdbcEntity entity) {
        if (null != cache) {
            Map<String, Object> map = cache.get(entity);
            if (null != map) {
                cache.remove(key);
            }
        }
        if (null != oldCache) {
            Map<String, Object> map = oldCache.get(entity);
            if (null != map) {
                oldCache.remove(key);
            }
        }
    }

    @Override
    public void evictAll() {
        //清理缓存
        if (null != cache) {
            cache.clear();
        }

        //清理缓存
        if (null != oldCache) {
            oldCache.clear();
        }
    }


    /**
     * 从本地缓存查询
     *
     * @param key
     * @param entity
     * @param <T>
     * @return
     */
    private <T> T getFromLocalCache(String key, JdbcEntity entity) {
        if (cache == null) return null;
        Map<String, Object> map = cache.get(key);
        if (null == map) return null;
        return (T) map.get(key);
    }

    private <T> T getFromOldLocalCache(String key, JdbcEntity entity) {
        if (oldCache == null) return null;
        Map<String, Object> map = oldCache.get(key);
        if (null == map) return null;
        return (T) map.get(key);
    }


    @Override
    public void clear() {
        //清理缓存
        if (null != cache) {
            cache.clear();
        }

        //清理缓存
        if (null != oldCache) {
            oldCache.clear();
        }
    }

    @Override
    public void clear(JdbcEntity entity) {
        //清理缓存
        if (null != cache) {
            cache.remove(entity);
        }

        //清理缓存
        if (null != oldCache) {
            oldCache.remove(entity);
        }
    }

    @Override
    public JdbcFactory getJdbcFactory() {
        return jdbcFactory;
    }

    @Override
    public void begin(Transaction transaction) {
        // do nothing
    }

    @Override
    public void beforeCommit(Transaction transaction, boolean succ) {
        if (succ) {
            triggerNow();
        }
    }

    /**
     * 检查是否要触发触发器
     *
     * @param entity
     */
    private void checkTrigger(JdbcEntity entity) {
        if (entity.isDelaySQLEnable()) {
            triggerNow();
        }
    }

    @Override
    public void commit(Transaction transaction, boolean succ) {
        this.transaction = null;
        this.hasTransaction = false;

    }

    @Override
    public <T, PK> T read(PK pk, JdbcEntity entity, ResultSetHandler<T> handler) {
        //检查是否要触发触发器
        checkTrigger(entity);
        String key = entity.getKeyValue(String.valueOf(pk));
        T t = getFromLocalCache(key, entity);
        if (null != t) return t;

        t = jdbcExtractor.read(pk, entity, handler);

        if (null != t) {
            if (entity.isEnhance()) {
                //获取主键
                String id = entity.getKeyValue(t);
                //创建一个增强实例
                Object obj = Lang.createObject(entity.getEnhanceClazz());
                entity.copy(t, obj);

                //放入本地缓存
                putToLocalCache(id, obj, entity);
                //放入旧的缓存
                putToOldLocalCache(id, obj, entity);
                return (T) obj;
            } else {
                //放入本地缓存
                putToLocalCache(key, t, entity);
            }
        }

        return t;
    }

    @Override
    public <T, PK> T readByIndex(Object[] indexs, JdbcEntity entity, ResultSetHandler<T> handler) {
        //检查是否要触发触发器
        checkTrigger(entity);
        String key = entity.getIndex().getKeyValueByParams(indexs);
        T t = getFromLocalCache(key, entity);
        if (null != t) return t;

        t = jdbcExtractor.readByIndex(indexs, entity, handler);

        if (null != t) {
            if (entity.isEnhance()) {
                //获取主键
                String id = entity.getKeyValue(t);
                //创建一个增强实例
                Object obj = Lang.createObject(entity.getEnhanceClazz());
                entity.copy(t, obj);

                //放入本地缓存
                putToLocalCache(id, obj, entity);
                //放入旧的缓存
                putToOldLocalCache(id, obj, entity);
                return (T) obj;
            } else {
                //放入本地缓存
                putToLocalCache(key, t, entity);
            }
        }

        return t;
    }

    /**
     * 插入数据到db
     *
     * @param newInstance 插入的实体
     * @param entity      实体
     * @param keys        要清除的查询缓存，如果为null，表示清理所有查询缓存
     * @param <T>
     * @param <PK>
     */
    @Override
    public <T, PK> void insert(final T newInstance, final JdbcEntity entity, final String... keys) {
        insert(newInstance, entity, false, keys);
    }

    /**
     * 插入数据到dB
     *
     * @param newInstance
     * @param entity
     * @param canDelay
     * @param keys
     * @param <T>
     * @param <PK>
     */
    private <T, PK> void insert(final T newInstance, final JdbcEntity entity, final boolean canDelay, final String... keys) {
        addTrigger(entity.getTableName(), JdbcSessionTrigger.INSERT, !canDelay || entity.isAutoGenerator() || !hasTransaction, entity, new JdbcSessionTrigger() {
            @Override
            public void trigger() {
                if (entity.isEnhance()) {
                    //插入数据库
                    jdbcExtractor.insert(newInstance, entity, keys);
                    //创建一个增强实例
                    Object obj = Lang.createObject(entity.getEnhanceClazz());
                    entity.copy(newInstance, obj);

                    //获取主键
                    String id = entity.getKeyValue(newInstance);
                    //放入本地缓存
                    putToLocalCache(id, obj, entity);
                    //放入旧的缓存
                    putToOldLocalCache(id, obj, entity);

                    //禁用查询缓存
                    entity.getCacheManger().disableQueryCache();
                    addToEntitySet(entity);

                } else {
                    //一般方式执行
                    jdbcExtractor.insert(newInstance, entity, keys);
                    //放入本地缓存
                    putToLocalCache(entity.getKeyValue(newInstance), newInstance, entity);
                    //禁用查询缓存
                    entity.getCacheManger().disableQueryCache();
                    addToEntitySet(entity);
                }

            }
        });

    }

    private <T> void putToOldLocalCache(String key, T t, JdbcEntity entity) {
        if (null == oldCache) {
            oldCache = new HashMap<>();
        }
        Map<String, Object> map = oldCache.get(key);
        if (null == map) {
            map = new HashMap<>();
            oldCache.put(entity, map);
        }
        map.put(key, t);
    }

    private <T> void putToLocalCache(String key, T t, JdbcEntity entity) {
        if (null == cache) {
            cache = new HashMap<>();
        }
        Map<String, Object> map = cache.get(key);
        if (null == map) {
            map = new HashMap<>();
            cache.put(entity, map);
        }
        map.put(key, t);
    }

    @Override
    public <T, PK> void insertDelay(T newInstance, JdbcEntity entity, String... keys) {
        insert(newInstance, entity, true, keys);
    }

    @Override
    public <T> void update(T transientObject, JdbcEntity entity) {
        if (entity.isEnhance()) {
            if (transientObject instanceof IDynamicUpdate) {
                final IDynamicUpdate update = (IDynamicUpdate) transientObject;
                //获取key值
                final String pk = entity.getKeyValue(transientObject);
                //获取旧的对象
                final JdbcModel old = getFromOldLocalCache(pk, entity);
                if (null != old) {
                    //和旧的对象比较，生成动态SQL
                    final Tuple<String, List<Param>> tuple = update.dynamicUpdateSQL(entity.getTableName(), old);
                    if (StringUtils.isNotBlank(tuple.left)) {
                        //执行动态更新
                        StringBuilder builder = new StringBuilder(tuple.left);
                        String[] idColumns = entity.getId().getIdColumnName();
                        int index = 1;
                        for (String str : idColumns) {
                            if (index == 1) {
                                builder.append(" WHERE ");
                            } else {
                                builder.append(" AND ");
                            }
                            builder.append(str).append(" = ? ");

                        }

                        Object[] idValues = entity.getId().getIdValue(transientObject);
                        for (Object obj : idValues) {
                            tuple.right.add(new Param(obj));
                        }

                        //添加触发器
                        addTrigger(entity.getTableName(), DefaultJdbcSessionTrigger.UPDATE, !hasTransaction, entity, new JdbcSessionTrigger() {
                            @Override
                            public void trigger() {
                                //执行更新
                                String sql = MessageFormatter.format(builder.toString(), entity.getTableName());
                                jdbcExtractor.update(sql, tuple.right, entity, pk);

                                //获取主键
                                String id = entity.getKeyValue(transientObject);
                                //放入本地缓存
                                putToLocalCache(id, transientObject, entity);
                                //更新旧的缓存
                                entity.copy(transientObject, old);
                                putToLocalCache(id, old, entity);
                                //禁用查询缓存
                                entity.getCacheManger().disableQueryCache();
                                addToEntitySet(entity);
                            }
                        });
                        return;
                    } else {
                        //没有变更
                        return;
                    }

                }
            }

        }

    }

    /**
     * 执行删除
     *
     * @param id
     * @param entity
     * @param <PK>
     */
    public <PK> void delete(final PK id, final JdbcEntity entity) {
        //添加触发器
        addTrigger(entity.getTableName(), JdbcSessionTrigger.UPDATE, !hasTransaction, entity, new JdbcSessionTrigger() {
            @Override
            public void trigger() {
                //执行删除
                jdbcExtractor.delete(id, entity);
                //从本地缓存移除
                String key = entity.getKeyValue(String.valueOf(id));
                removeFromLocalCache(key, entity);
                if (entity.isEnhance()) {
                    removeFromOldLocalCache(key, entity);
                }
                //禁用查询缓存
                entity.getCacheManger().disableQueryCache();
                addToEntitySet(entity);

            }
        });

    }

    @Override
    public <T> List<T> query(String selectKey, String sql, List<Param> params, JdbcEntity entity, ResultSetHandler<List<T>> handler) {
        //检查是否要触发触发器
        checkTrigger(entity);

        List<T> list = jdbcExtractor.query(selectKey, sql, params, entity, handler);
        if (null != list && list.size() > 0) {
            for (T t : list) {
                putToLocalCache(entity.getKeyValue(t), t, entity);
            }
        }
        return list;
    }

    @Override
    public <PK> int update(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        //触发所有触发器
        triggerNow();
        int result = jdbcExtractor.update(sql, params, entity, pk, keys);
        //update不一定有返回影响行数
        if (null != pk) {
            removeFromLocalCache(entity.getKeyValue(String.valueOf(pk)), entity);
        } else {
            clear(entity);
        }

        //禁用查询缓存
        entity.getCacheManger().disableQueryCache();
        //禁用二级缓存
        entity.getCacheManger().disableObjCache();
        addToEntitySet(entity);

        return 0;
    }

    @Override
    public <PK> void updateDelay(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        //添加触发器
        addTrigger(entity.getTableName(), JdbcSessionTrigger.UPDATE, !hasTransaction, entity, new JdbcSessionTrigger() {
            @Override
            public void trigger() {
                jdbcExtractor.update(sql, params, entity, pk);
                //update不一定有返回影响行数
                if (null != pk) {
                    removeFromLocalCache(entity.getKeyValue(String.valueOf(pk)), entity);
                } else {
                    clear(entity);
                }

                //禁用查询缓存
                entity.getCacheManger().disableQueryCache();
                //禁用二级缓存
                entity.getCacheManger().disableObjCache();
                addToEntitySet(entity);
            }
        });

    }

    @Override
    public void batch(String sql, List<List<Param>> params, JdbcEntity entity, String... keys) {
        //触发所有触发器
        triggerNow();
        jdbcExtractor.batch(sql,params,entity,keys);
        //禁用查询缓存
        entity.getCacheManger().disableQueryCache();;
        //禁用二级缓存
        entity.getCacheManger().disableObjCache();
        addToEntitySet(entity);
    }


    @Override
    public <T> T query(String sql, List<Param> params, JdbcEntity entity, ResultSetHandler<T> handler) {
        //触发所有触发器
        triggerNow();
        return jdbcExtractor.query(sql, params, handler);
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params, JdbcEntity entity) {
        //触发所有触发器
        triggerNow();
        return jdbcExtractor.query(sql, params, entity);
    }

    /**
     * 从缓存中移除对象
     *
     * @param key
     * @param entity
     */
    private void removeFromLocalCache(String key, JdbcEntity entity) {
        if (null == cache) {
            return;
        }
        Map<String, Object> map = cache.get(entity);
        if (null == map) {
            return;
        }
        map.remove(key);
    }


    private void removeFromOldLocalCache(String key, JdbcEntity entity) {
        if (null == oldCache) {
            return;
        }
        Map<String, Object> map = oldCache.get(entity);
        if (null == map) {
            return;
        }
        map.remove(key);
    }

    @Override
    public <T> T query(String sql, List<Param> params, ResultSetHandler<T> handler) {
        //触发所有触发器
        triggerNow();
        return jdbcExtractor.query(sql, params, handler);
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params) {
        //触发所有触发器
        triggerNow();
        return jdbcExtractor.query(sql, params);
    }

    @Override
    public int update(String sql, List<Param> params) {
        throw new UnsupportedOperationException("");
    }

    @Override
    public int insert(String sql, List<Param> params, boolean autoGenerator) {
        return jdbcExtractor.insert(sql, params, autoGenerator);
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void batch(String sql, List<List<Param>> paramList, int batchSize) {
        throw new UnsupportedOperationException();

    }

    @Override
    public void batch(List<String> sqlList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void batch(List<String> sqlList, int batchSize) {
        throw new UnsupportedOperationException();

    }
}
