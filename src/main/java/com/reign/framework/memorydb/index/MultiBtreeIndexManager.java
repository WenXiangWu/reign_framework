package com.reign.framework.memorydb.index;

import com.reign.framework.memorydb.MultiBPlusTree;
import com.reign.framework.jdbc.orm.IdEntity;
import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.JdbcField;
import com.reign.framework.jdbc.orm.JdbcModel;
import com.reign.framework.memorydb.MemoryTable;
import com.reign.framework.memorydb.annotation.BTreeIndex;

import java.util.*;

/**
 * @ClassName: MultiBtreeIndexManager
 * @Description: 管理器
 * @Author: wuwx
 * @Date: 2021-04-02 17:17
 **/
public class MultiBtreeIndexManager<V extends JdbcModel> implements IndexManager<V> {

    /**
     * 实体
     */
    private JdbcEntity entity;

    /**
     * 索引名称
     */
    private String name;

    /**
     * 索引列
     */
    private JdbcField[] fields;

    /**
     * 索引树
     */
    private MultiBPlusTree<String, String> indexTree;

    /**
     * 主键索引
     */
    private IdEntity id;

    /**
     * 主表
     */
    private MemoryTable memoryTable;

    /**
     * 最左前缀匹配器
     */
    private Comparator<String> leftComparator;


    public MultiBtreeIndexManager(MemoryTable memoryTable, JdbcEntity entity, BTreeIndex index) {
        this.entity = entity;
        this.memoryTable = memoryTable;
        this.name = index.name();
        String[] columns = index.value();
        List<JdbcField> indexFields = new ArrayList<>();
        for (String column : columns) {
            JdbcField temp = null;
            for (JdbcField field : entity.getFields()) {
                if (column.equals(field.propertyName)) {
                    temp = field;
                    break;
                }
            }
            if (null == temp) {
                throw new RuntimeException("can not found index column ,index :" + column);
            }
            indexFields.add(temp);
        }
        this.fields = indexFields.toArray(new JdbcField[0]);
        this.indexTree = new MultiBPlusTree<>();
        this.id = entity.getId();
        this.leftComparator = new Comparator<String>() {
            @Override
            public int compare(String key, String searchKey) {
                if (key.startsWith(searchKey)) {
                    return 0;
                }
                return key.compareTo(searchKey);
            }
        };
    }

    /**
     * 根据对象获取索引key值
     *
     * @param obj
     * @return
     */
    private String getKeyValueByObject(Object obj) {
        try {
            Object[] array = new Object[fields.length];
            int index = 0;
            for (JdbcField field : fields) {
                field.field.setAccessible(true);
                array[index++] = field.field.get(obj);
            }
            return toString(array);
        } catch (Throwable t) {
            throw new RuntimeException("get Key error" + t);
        }

    }

    /**
     * 将数组转换为key
     *
     * @param array
     * @return
     */
    private String toString(Object[] array) {
        StringBuilder builder = new StringBuilder();
        for (Object obj : array) {
            builder.append(obj.toString()).append(":");
        }
        return builder.toString();
    }

    /**
     * 根据参数获取索引key值
     *
     * @param args
     * @return
     */
    private String getKeyValueByParams(Object[] args) {
        return toString(args);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void insert(V value) {
        String idKey = id.getKeyValueByObject(value);
        String indexKey = getKeyValueByObject(value);
        indexTree.insert(indexKey, idKey);
    }

    @Override
    public void remove(V value) {
        String indexKey = getKeyValueByObject(value);
        String idKey = id.getKeyValueByObject(value);
        Collection<String> keyCollection = indexTree.find(indexKey);
        keyCollection.remove(idKey);
        if (keyCollection.isEmpty()) {
            indexTree.remove(indexKey);
        }
    }

    @Override
    public void remove(Object... args) {
        String indexKey = getKeyValueByParams(args);
        String idKey = id.getKeyValuesByParams(args);
        Collection<String> keyCollection = indexTree.find(indexKey);
        keyCollection.remove(idKey);
        if (keyCollection.isEmpty()) {
            indexTree.remove(indexKey);
        }
    }

    @Override
    public void update(V oldValue, V newValue) {
        if (oldValue == null) return;
        String oldIndexKey = getKeyValueByObject(oldValue);
        String newIndexKey = getKeyValueByObject(newValue);
        String idKey = id.getKeyValueByObject(newValue);
        if (!oldIndexKey.equals(newIndexKey)) {
            remove(oldIndexKey);
            indexTree.insert(newIndexKey, idKey);
        }
    }

    @Override
    public Object find(V value) {
        String indexKey = getKeyValueByObject(value);
        Collection<String> keyCol = indexTree.find(indexKey);
        return getResult(keyCol);
    }

    @Override
    public Object find(Object... args) {
        String indexKey = getKeyValueByParams(args);
        Collection<String> keyCol = indexTree.find(indexKey);
        return getResult(keyCol);
    }

    private List<V> getResult(Collection<String> keyCol) {
        if (null == keyCol) return Collections.EMPTY_LIST;
        List<V> resultList = new ArrayList<>(keyCol.size());
        for (String key : keyCol) {
            resultList.add((V) memoryTable.readByIdKey(key));
        }
        return resultList;
    }

    @Override
    public List<V> rangeFind(V start, V end) {
        return null;
    }

    @Override
    public List<V> leftFind(Object... args) {
        String indexKey = getKeyValueByParams(args);
        List<String> keyList = indexTree.find(indexKey, leftComparator);
        return getResult(keyList);
    }

    @Override
    public int getHeight() {
        return indexTree.getHeight();
    }

    @Override
    public void clear() {
        this.indexTree = new MultiBPlusTree<>();
    }
}
