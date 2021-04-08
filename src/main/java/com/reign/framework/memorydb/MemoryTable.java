package com.reign.framework.memorydb;

import com.reign.framework.common.Lang;
import com.reign.framework.common.util.ReflectUtil;
import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.Params;
import com.reign.framework.jdbc.Type;
import com.reign.framework.jdbc.handlers.ColumnListHandler;
import com.reign.framework.jdbc.orm.*;
import com.reign.framework.jdbc.orm.annotation.AsyncOp;
import com.reign.framework.memorydb.index.IndexManager;
import com.reign.framework.memorydb.index.IndexManagerFactory;
import com.reign.framework.memorydb.sequence.ISequenceDao;
import com.reign.framework.memorydb.sequence.Sequence;
import com.reign.framework.jdbc.orm.SingleIdEntity;
import com.reign.framework.memorydb.annotation.AutoId;
import com.reign.framework.memorydb.annotation.SyncDBConfig;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName: MemoryTable
 * @Description: 内存表
 * @Author: wuwx
 * @Date: 2021-04-01 18:37
 **/
public class MemoryTable<V extends AbstractDomain, K extends Serializable> {

    /**
     * 主表
     */
    private Map<String, V> mainTable = new HashMap<>();


    /**
     * 索引表
     */
    private Map<String, IndexManager<V>> indexTable = new HashMap<>();
    /**
     * 存储需要清理的key值
     */
    private Set<String> clearKeyTable = new HashSet<>();

    //jdbc相关
    private JdbcEntity entity;
    private IBaseDao<V, K> dao;
    private ISequenceDao sequenceDao;
    private Lock readLock;
    private Lock writeLock;
    private AtomicInteger id;
    private boolean autoId;
    private JdbcField idField;
    private long syncInterval;
    private AtomicInteger order;


    /**
     * 初始化
     *
     * @param dao
     * @param entity
     */
    public void init(IBaseDao<V, K> dao, JdbcEntity entity) {
        this.dao = dao;
        this.entity = entity;
        //建立索引
        IndexManagerFactory.initIndex(entity.getEntityClass(), this, entity, indexTable);

        //检验DB结构
        doTableValidation();

        //设置主键
        setId();

        //解释同步周期
        SyncDBConfig config = Lang.getAnnotation(entity.getEntityClass(), SyncDBConfig.class);
        if (null != config) {
            syncInterval = config.interval();
        } else {
            syncInterval = AsyncDBExecutor.INTERVAL;
        }

        this.order = new AtomicInteger(1);
        ReadWriteLock lock = new ReentrantReadWriteLock(false);
        this.readLock = lock.readLock();
        this.writeLock = lock.writeLock();
    }


    /**
     * 全表扫描
     *
     * @param matcher
     * @return
     */
    public List<V> allFind(Matcher<V> matcher) {
        try {
            List<V> resultList = new ArrayList<>();
            this.readLock.lock();
            for (V entry : mainTable.values()) {
                if (matcher.match(entry)) {
                    resultList.add(entry);
                }
            }
            return resultList;
        } finally {
            this.readLock.unlock();
        }

    }

    /**
     * 异步执行SQL
     *
     * @param sql
     * @param params
     */
    public void asyncSql(String sql, List<Param> params) {
        AsyncDBExecutor.getInstance().addSQL(sql, params, entity, order.getAndIncrement(), syncInterval);
    }

    /**
     * 清理数据
     */
    public void clear() {
        this.writeLock.lock();
        try {
            //清理索引
            for (IndexManager manager : indexTable.values()) {
                manager.clear();
            }
            //清理主表
            this.mainTable.clear();
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * 删除对象
     *
     * @param key
     */
    public void delete(K key) {
        delete(key, true);
    }

    /**
     * 删除对象
     *
     * @param key
     * @param deleteFromDB
     */
    public void delete(K key, boolean deleteFromDB) {
        this.writeLock.lock();
        try {
            String idKey = this.entity.getId().getKeyValuesByParams(key);
            //从内存中删除
            deleteFromMemory(idKey, deleteFromDB);
        } finally {
            this.writeLock.lock();
        }
    }

    /**
     * 删除对象
     *
     * @param value
     */
    public void delete(V value) {
        delete(value, true);
    }


    /**
     * 通过value删除数据
     *
     * @param value
     * @param deleteFromDB
     */
    public void delete(V value, boolean deleteFromDB) {
        if (value == null) return;
        this.writeLock.lock();
        try {
            String idKey = this.entity.getId().getKeyValueByObject(value);
            //从内存中移除
            deleteFromMemory(idKey, deleteFromDB);
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * 从内存中移除指定key
     *
     * @param idKey
     * @param deleteFromDB
     */
    public void deleteFromMemory(String idKey, boolean deleteFromDB) {
        if (!deleteFromDB && !MemoryDBMonitor.isHealth()) {
            //db不健康了，停止内存清理
            clearKeyTable.add(idKey);
            //托管自身
            MemoryDBMonitor.manageMemoryTable(this);
            return;
        }
        V value = mainTable.remove(idKey);
        if (null == value) {
            //说明已经被删除了
            return;
        }
        //从索引中删除
        for (IndexManager manager : indexTable.values()) {
            manager.remove(value);
        }
        //从db中删除
        if (deleteFromDB) {
            AsyncDBExecutor.getInstance().addSQL(AsyncOp.DELETE, entity, idKey, (K) ReflectUtil.get(idField.field, value), order.getAndIncrement(), syncInterval);
        }

    }

    /**
     * 查找对象
     *
     * @param keyName
     * @param args
     * @return
     */
    public Object find(String keyName, Object... args) {
        IndexManager manager = indexTable.get(keyName);
        if (null == manager) {
            throw new RuntimeException("unknown key:" + keyName);
        }
        this.readLock.lock();
        try {
            return manager.find(args);
        } finally {
            this.readLock.unlock();
        }

    }

    /**
     * 移除需要删除的数据
     */
    public void flushDeleteOp() {
        this.writeLock.lock();
        try {
            if (clearKeyTable.size() <= 0) {
                return;
            }

            //移除需要移除的数据
            Set<String> keySet = new HashSet<>(clearKeyTable);
            clearKeyTable.clear();
            for (String key : keySet) {
                deleteFromMemory(key, false);
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    /**
     * 获取树高
     *
     * @return
     */
    public int getHeight() {
        int height = 0;
        for (IndexManager manager : indexTable.values()) {
            int i = manager.getHeight();
            if (i > height) height = i;
        }
        return height;
    }


    private int getMaxId() {
        StringBuilder sb = new StringBuilder();
        if (!this.autoId) {
            throw new RuntimeException("auto increment not support complex primary key");
        }

        //通过auto_increment查询主键值
        sb.append("SELECT auto_increment as autoId FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = database() AND TABLE_NAME ='")
                .append(entity.getTableName()).append("'");
        List<Object> resultList = dao.query(sb.toString(), Params.EMPTY, new ColumnListHandler(1));
        if (resultList.size() > 0) {
            if (null != resultList.get(0)) {
                return Integer.valueOf(String.valueOf(resultList.get(0))) - 1;
            }
        }

        //通过maxId查询
        sb.setLength(0);
        sb.append("SELECT MAX(")
                .append(idField.columnName)
                .append(") FROM ")
                .append(entity.getTableName());
        resultList = dao.query(sb.toString(), Params.EMPTY, new ColumnListHandler(1));
        if (resultList.size() > 0) {
            if (null != resultList.get(0)) {
                return Integer.valueOf(String.valueOf(resultList.get(0)));
            }
        }
        return 0;
    }


    /**
     * 获取内存中所有实体
     *
     * @return
     */
    public List<V> getModels() {
        this.readLock.lock();
        try {
            return new ArrayList<>(mainTable.values());
        } finally {
            this.readLock.unlock();
        }

    }

    /**
     * 获取下一个可用的id
     *
     * @return
     */
    public int getNextId() {
        if (null != this.sequenceDao) {
            return this.sequenceDao.nextId(entity.getTableName());
        }
        return this.id.incrementAndGet();
    }

    /**
     * 获取表锁
     *
     * @return
     */
    public Lock getTableLock() {
        return writeLock;
    }

    /**
     * 插入对象
     *
     * @param vlaue
     */
    public void insert(V vlaue) {
        insert(vlaue, true);
    }

    public void insert(V value, boolean saveToDB) {
        if (value == null) return;

        this.writeLock.lock();
        try {
            if (saveToDB && this.autoId) {
                ReflectUtil.set(idField.field, value, getNextId());
            }
            //处理增强
            if (entity.isEnhance() && !(value instanceof IDynamicUpdate)) {
                //进行增强
                V enhanceValue;
                try {
                    enhanceValue = (V) entity.getEnhanceClazz().newInstance();
                    entity.copy(value, enhanceValue);
                    value = enhanceValue;
                } catch (Throwable t) {

                }

            }
            String idKey = this.entity.getId().getKeyValueByObject(value);
            if (mainTable.containsKey(idKey)) {
                //已经存在了
                if (!saveToDB && !MemoryDBMonitor.isHealth()) {
                    //db不健康了，重新载入了，从移除队列中删除
                    this.clearKeyTable.remove(idKey);
                }
                return;
            }
            V old = mainTable.put(idKey, value);
            if (old == null) {
                for (IndexManager manager : indexTable.values()) {
                    manager.insert(value);
                }
            } else {
                for (IndexManager manager : indexTable.values()) {
                    manager.update(old, value);
                }
            }

            //如果对象是可以锁定的，将表复制给他
            if (value instanceof AbstractLockableDomain) {
                AbstractLockableDomain domain = (AbstractLockableDomain) value;
                domain.tableLock = this.writeLock;
            }
            value.managed = true;

            //insert to db
            if (saveToDB) {
                AsyncDBExecutor.getInstance().addSQL(AsyncOp.INSERT, entity, idKey, value, order.getAndIncrement(), syncInterval);
            }
        } finally {
            this.writeLock.unlock();
        }

    }

    /**
     * 最左匹配原则查找对象
     *
     * @param keyName
     * @param args
     * @return
     */
    public List<V> leftFind(String keyName, Object... args) {
        IndexManager manager = indexTable.get(keyName);
        if (null == manager) {
            throw new RuntimeException("unknown key:" + keyName);
        }

        try {
            this.readLock.lock();
            return (List<V>) manager.leftFind(args);
        } finally {
            this.readLock.unlock();
        }
    }


    /**
     * 获取当前最大id
     *
     * @return
     */
    public int maxId() {
        if (null != this.sequenceDao) {
            return sequenceDao.maxId(entity.getTableName());
        }
        return this.id.get();

    }

    public V read(K key) {
        if (null == key) {
            return null;
        }

        this.readLock.lock();
        try {
            V v = mainTable.get(this.entity.getId().getKeyValuesByParams(key));
            if (null != v) {
                v.markOld();
            }
            return v;
        } finally {
            this.readLock.unlock();
        }
    }


    /**
     * 根据key获取指定对象
     *
     * @param key
     * @return
     */
    public V readByKey(String key) {
        if (null == key) return null;
        this.readLock.lock();
        try {
            V v = mainTable.get(key);
            if (null != v) {
                v.markOld();
            }
            return v;
        } finally {
            this.readLock.unlock();
        }


    }


    /**
     * 设置主键id
     */
    private void setId() {
        if (!(entity.getId() instanceof SingleIdEntity)) {
            throw new RuntimeException("memory table not support complex primary key");
        }
        this.idField = entity.getIdFields()[0];

        //从db中载入最大主键
        AutoId autoId = Lang.getAnnotation(entity.getEntityClass(), AutoId.class);
        if (null != autoId) {
            this.autoId = true;
            if (null != dao) {
                int dbMaxId = getMaxId();
                if (null == id || id.get() < dbMaxId) {
                    id = new AtomicInteger(dbMaxId);
                }
                setSequence(id.get());
            }
        }

        if (this.autoId && null == id) {
            id = new AtomicInteger(0);
        }

    }

    /**
     * 设置sequence
     *
     * @param maxId
     */
    private void setSequence(int maxId) {
        if (maxId == 0) return;
        if (null != sequenceDao) {
            Sequence sequence = this.sequenceDao.getSequence(entity.getTableName());
            if (null == sequence) {
                //不存在则创建
                sequence = new Sequence();
                sequence.setTableName(entity.getTableName());
                sequence.setSequence(maxId);
                this.sequenceDao.create(sequence);
            } else if (sequence.getSequence() != maxId) {
                sequence.setSequence(maxId);
                this.sequenceDao.update(sequence);
            }
        }

    }

    /**
     * 获得大小
     *
     * @return
     */
    public int size() {
        return mainTable.size();
    }

    /**
     * 更新对象
     *
     * @param value
     */
    public void update(V value) {
        update(value, true);
    }

    /**
     * 更新对象
     *
     * @param value
     * @param syncToDB
     */
    public void update(V value, boolean syncToDB) {
        if (null == value) {
            return;
        } else if (!value.managed) {
            throw new RuntimeException("update value is not managed by memorydb");
        }

        this.writeLock.lock();
        try {
            String idKey = this.entity.getId().getKeyValueByObject(value);
            if (!mainTable.containsKey(idKey)) {
                System.out.println("update value is not exists tablesName:" + entity.getTableName() + "  idKey:" + idKey);
                return;
            }

            mainTable.put(idKey, value);
            //更新索引
            V old = (V) value.oldDomain();
            for (IndexManager manager : indexTable.values()) {
                manager.update(old, value);
            }
            if (syncToDB) {
                AsyncDBExecutor.getInstance().addUpdateSQL(entity, idKey, value, old, order.getAndIncrement(), syncInterval);
            }
        } finally {
            this.writeLock.unlock();
        }
    }


    /**
     * 校验db结构
     */
    public void doTableValidation() {
        try {
            List<Map<String, Object>> resultList = dao.query("DESC " + entity.getTableName(), Params.EMPTY);
            Map<String, Type> columnMap = new HashMap<>();
            for (Map<String, Object> map : resultList) {
                String columnName = (String) map.get("COLUMN_NAME");
                Type type = Lang.getJdbcType((String) map.get("COLUMN_TYPE"));
                columnMap.put(columnName.toLowerCase(), type);
            }

            JdbcField[] fields = entity.getFields();
            int count = 0;
            for (JdbcField field : fields) {
                if (field.ignore) {
                    //忽略的字段不验证
                    continue;
                }
                Type type = columnMap.get(field.columnName.toLowerCase());
                if (null == type) {
                    throw new RuntimeException("doTableValidation table " + entity.getTableName() + " don't has " + field.columnName + "  column");
                }
                if (!type.equals(field.jdbcType)) {
                    throw new RuntimeException("doTableValidation table " + entity.getTableName() + "  " + field.columnName + " type not match");
                }
                count++;
            }

            if (count != columnMap.size()) {
                throw new RuntimeException("doTableValidation table " + entity.getTableName() + " db column not match memory ");

            }


        } catch (RuntimeException e) {
            throw e;
        }

    }

    public V readByIdKey(String idKey) {
        if (null == idKey) return null;
        try {
            this.readLock.lock();
            V v = mainTable.get(idKey);
            if (null != v) {
                v.markOld();
            }
            return v;
        } finally {
            this.readLock.unlock();
        }
    }
}
