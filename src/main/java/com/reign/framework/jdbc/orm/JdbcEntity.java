package com.reign.framework.jdbc.orm;

import com.reign.framework.common.Lang;
import com.reign.framework.common.util.ReflectUtil;
import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.Params;
import com.reign.framework.jdbc.orm.annotation.*;
import com.reign.framework.jdbc.orm.asm.JdbcModelEnhancer;
import com.reign.framework.jdbc.orm.cache.CacheFactory;
import com.reign.framework.jdbc.orm.util.JdbcUtil;
import com.reign.framework.jdbc.NameStrategy;
import com.reign.framework.jdbc.orm.cache.CacheManager;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: JdbcEntity
 * @Description: 实体
 * @Author: wuwx
 * @Date: 2021-04-02 14:49
 **/
public class JdbcEntity {

    /**
     * 属性
     */
    private JdbcField[] fields;


    /**
     * id
     */
    private IdEntity id;


    private IndexEntity index;

    /**
     * 类型
     */
    private Class<?> clazz;

    /**
     * 增强
     */
    private Class<?> enhanceClazz;

    /**
     * 命名策略
     */
    private NameStrategy nameStrategy;

    /**
     * 实体名称
     */
    private String entityName;

    /**
     * idFields
     */
    private JdbcField[] idFields;

    /**
     * 是否加强过高
     */
    private boolean enhance;

    /**
     * 插入语句
     */
    private String insertSQL;

    /**
     * 更新语句
     */
    private String updateSQL;

    /**
     * 查询所有SQL
     */
    private String selectAllSQL;

    /**
     * 查询SQL
     */
    private String selectAllCountSQL;

    /**
     * 查询SQL
     */
    private String selectSQL;

    /**
     * 查询SQL
     */
    private String selectForUpdateSQL;

    /**
     * 删除SQL
     */
    private String deleteSQL;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 延迟SQL是否启用
     */
    private ThreadLocal<Boolean> delaySQLEnable;

    /**
     * 缓存管理器
     */
    private CacheManager cacheManger;


    /**
     * 构造插入参数
     *
     * @param obj
     * @return
     */
    public final List<Param> builderInsertParams(Object obj) {
        Params params = new Params();
        for (JdbcField field : fields) {
            if (!field.insertIgnore && !field.ignore) {
                params.addParam(ReflectUtil.get(field.field, obj), field.jdbcType);
            }
        }
        return params;
    }

    /**
     * 构造查询参数
     *
     * @param keys
     * @return
     */
    public final List<Param> builderSelectParams(Object... keys) {
        Params params = new Params();
        int index = 0;
        for (JdbcField field : idFields) {
            params.addParam(keys[index++], field.jdbcType);
        }
        return params;

    }


    /**
     * 是否是自动生成
     *
     * @return
     */
    public final boolean isAutoGenerator() {
        return id.isAutoGenerator();
    }

    /**
     * 是否启动了延迟执行SQL
     *
     * @return
     */
    public final boolean isDelaySQLEnable() {
        return delaySQLEnable.get() != null && delaySQLEnable.get();
    }


    /**
     * 构造更新参数
     *
     * @param obj
     * @return
     */
    public final List<Param> builderUpdateParams(Object obj) {
        Params params = new Params();
        for (JdbcField field : fields) {
            if (!field.ignore && !field.isPrimary) {
                params.addParam(ReflectUtil.get(field.field, obj), field.jdbcType);
            }
        }

        for (JdbcField field : idFields) {
            params.addParam(ReflectUtil.get(field.field, obj), field.jdbcType);
        }
        return params;

    }

    /**
     * 获取拷贝bean的拷贝器
     *
     * @param src
     * @param target
     */
    public final void copy(Object src, Object target) {
        for (JdbcField field : fields) {
            ReflectUtil.set(field.field, target, ReflectUtil.get(field.field, src));
        }
    }

    /**
     * 启动延迟执行SQL
     */
    public final void enableDelaySQL() {
        delaySQLEnable.set(true);
    }

    /**
     * 生成创建的SQL
     *
     * @return
     */
    public static String genratorCreateSql(JdbcEntity entity) {
        StringBuilder sb = new StringBuilder();
        sb.append("DROP TABLE IF EXISTS '").append(entity.tableName).append("';\n");
        sb.append("CREATE TABLE '")
                .append(entity.tableName)
                .append("' (\n");

        JdbcField primary = null;
        for (JdbcField field : entity.fields) {
            if (field.ignore || field.insertIgnore) {
                continue;
            }

            sb.append("   '" + field.columnName + "'").append("  ");
            switch (field.jdbcType) {
                case Int:
                    sb.append("int").append(",\n");
                    break;
                case Float:
                    sb.append("float").append(",\n");
                    break;
                case String:
                    sb.append("varchar(500)").append(",\n");
                    break;
                case SqlDate:
                    sb.append("datetime").append(",\n");
                    break;
                case Double:
                    sb.append("double").append(",\n");
                    break;
                default:
                    break;
            }
            if (field.isPrimary) {
                primary = field;
            }
            if (null != primary) {
                sb.append("    PRIMARY KEY ('").append(primary.columnName).append("')\n");
            }
            sb.append(")ENGINE=InnoDB CHARSET=utf8;");
        }
        return sb.toString();
    }

    /**
     * 生成删除SQL
     *
     * @param entity
     * @return
     */
    private static String generatorDeleteSQL(JdbcEntity entity) {
        StringBuilder builder = new StringBuilder();
        builder.append("DELETE FROM  ")
                .append(entity.tableName)
                .append("  WHERE ");

        int index = 1;
        for (JdbcField field : entity.fields) {
            if (field.isPrimary) {
                if (index != 1) {
                    builder.append(" AND ");
                }
                builder.append(field.columnName)
                        .append("=")
                        .append("?");
                index++;
            }
        }
        return builder.toString();
    }

    /**
     * 生成插入SQL
     *
     * @param entity
     * @return
     */
    private static String generatorInsertSQL(JdbcEntity entity) {
        StringBuilder builder = new StringBuilder();
        builder.append("INSERT INTO ")
                .append(entity.tableName)
                .append("(");
        int index = 1;
        for (JdbcField field : entity.fields) {
            if (field.ignore || field.insertIgnore) {
                continue;
            }

            if (index != 1) {
                builder.append(", ");
            }
            builder.append(field.columnName);
            index++;
        }
        builder.append(") VALUES (");
        index = 1;
        for (JdbcField field : entity.fields) {
            if (field.insertIgnore || field.ignore) {
                continue;
            }
            if (index != 1) {
                builder.append(", ");
            }
            builder.append("?");
            index++;

        }
        builder.append(")");
        return builder.toString();

    }


    /**
     * 生成查询SQL
     *
     * @param entity
     * @return
     */
    private static final String generatorSelectSQL(JdbcEntity entity) {
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT *  FROM ")
                .append(entity.tableName)
                .append("  WHERE ");
        int index = 1;
        for (JdbcField field : entity.fields) {
            if (field.isPrimary) {
                if (index != 1) {
                    builder.append(" AND ");
                }
                builder.append(field.columnName)
                        .append("=")
                        .append("?");
                index++;
            }
        }
        return builder.toString();
    }

    /**
     * 生成更新SQL
     *
     * @param entity
     * @return
     */
    private static String generatorUpdateSQL(JdbcEntity entity) {
        StringBuilder builder = new StringBuilder();
        builder.append("UPDATE ")
                .append(entity.tableName)
                .append(" SET ");
        int index = 1;
        for (JdbcField field : entity.fields) {
            if (field.isPrimary || field.ignore) {
                continue;
            }
            if (index != 1) {
                builder.append(", ");
            }
            builder.append(field.columnName)
                    .append("=")
                    .append("?");
            index++;

        }
        builder.append(" WHERE ");
        index = 1;
        for (JdbcField field : entity.fields) {
            if (field.isPrimary) {
                if (index != 1) {
                    builder.append(" AND ");
                }
                builder.append(field.columnName)
                        .append("=")
                        .append("?");
                index++;
            }
        }
        return builder.toString();
    }


    /**
     * 获取keyvalue
     *
     * @param obj
     * @return
     */
    public final String getKeyValue(Object obj) {
        return cacheManger.getPrefix() + id.getKeyValueByObject(obj);
    }

    /**
     * 获取keyvalue
     *
     * @param key
     * @return
     */
    public final String getKeyValue(String key) {
        return cacheManger.getPrefix() + key;
    }


    public JdbcField[] getFields() {
        return fields;
    }

    public void setFields(JdbcField[] fields) {
        this.fields = fields;
    }

    public IdEntity getId() {
        return id;
    }

    public void setId(IdEntity id) {
        this.id = id;
    }

    public IndexEntity getIndex() {
        return index;
    }

    public void setIndex(IndexEntity index) {
        this.index = index;
    }

    public Class<?> getEntityClass() {
        return clazz;
    }

    public void setEntityClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getEnhanceClazz() {
        return enhanceClazz;
    }

    public void setEnhanceClazz(Class<?> enhanceClazz) {
        this.enhanceClazz = enhanceClazz;
    }

    public NameStrategy getNameStrategy() {
        return nameStrategy;
    }

    public void setNameStrategy(NameStrategy nameStrategy) {
        this.nameStrategy = nameStrategy;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    /**
     * 判断该实体是否经过增强
     *
     * @return
     */
    public boolean isEnhance() {
        return enhance;
    }


    /**
     * 解析Fields
     *
     * @param fields
     * @param nameStrategy
     * @return
     */
    private static JdbcField[] parse(Lang.MyField[] fields, NameStrategy nameStrategy) {
        if (null == fields) return null;
        if (fields.length == 0) return new JdbcField[0];
        JdbcField[] jdbcFields = new JdbcField[fields.length];
        for (int i = 0; i < fields.length; i++) {
            jdbcFields[i].isPrimary = Lang.hasAnnotation(jdbcFields[i].field, Id.class);
            jdbcFields[i].insertIgnore = Lang.hasAnnotation(jdbcFields[i].field, InsertIgnoreField.class);
            jdbcFields[i].ignore = Lang.hasAnnotation(jdbcFields[i].field, IgnoreField.class);
            jdbcFields[i].jdbcType = Lang.getJdbcType(jdbcFields[i].field.getType());
        }
        return jdbcFields;
    }


    /**
     * 解析实体
     *
     * @param clazz
     * @param nameStrategy
     * @param context
     * @param cacheFactory
     * @return
     * @throws IntrospectionException
     */
    public static JdbcEntity resolve(Class<?> clazz, NameStrategy nameStrategy, JdbcFactory context, CacheFactory cacheFactory) throws IntrospectionException {
        JdbcEntity entity = new JdbcEntity();

        //需要动态更新的
        if (Lang.getAnnotation(clazz, DynamicUpdate.class) != null) {
            entity.enhanceClazz = JdbcModelEnhancer.enhance(clazz);
            entity.enhance = true;
        }

        entity.clazz = clazz;
        entity.fields = JdbcUtil.createJdbcFields(entity.clazz, nameStrategy);
        entity.nameStrategy = nameStrategy;
        entity.entityName = clazz.getSimpleName();
        entity.tableName = nameStrategy.propertyNameToColumnName(entity.entityName);
        entity.insertSQL = generatorInsertSQL(entity);
        entity.updateSQL = generatorUpdateSQL(entity);
        entity.selectSQL = generatorSelectSQL(entity);
        entity.deleteSQL = generatorDeleteSQL(entity);
        entity.selectForUpdateSQL = entity.selectSQL + " FOR UPDATE";
        entity.selectAllSQL = "SELECT * FROM " + entity.tableName;
        entity.selectAllCountSQL = "SELECT COUNT(1) AS COUNT FROM " + entity.tableName;
        entity.delaySQLEnable = new ThreadLocal<>();
        //为了使缓存生效
        Introspector.getBeanInfo(entity.clazz);
        JdbcUtil.createBeanMap(entity.clazz);

        //主键解析
        List<JdbcField> idFields = new ArrayList<>();
        for (JdbcField field : entity.fields) {
            if (field.isPrimary) {
                idFields.add(field);
            }
        }
        entity.idFields = idFields.toArray(new JdbcField[0]);
        if (entity.idFields.length == 1) {
            //单主键
            entity.id = new SingleIdEntity(entity.idFields[0], entity);
        } else if (entity.idFields.length == 0) {
            throw new RuntimeException(entity.entityName + " does not has primary key,JdbcEntity must have a primary key");
        } else {
            //复合主键
            entity.id = new ComplexIdEntity(entity.idFields, entity);
        }

        //索引解析
        Index index = Lang.getAnnotation(entity.getEntityClass(), Index.class);
        if (null != index) {
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
                    throw new RuntimeException("cannot found index column,index:" + column);
                }
                indexFields.add(temp);
            }
            entity.index = new DefaultIndexEntity(indexFields.toArray(new JdbcField[0]), entity);
            entity.index.init();

        }
        //初始化缓存管理器 TODO 暂时不处理缓存
        //entity.cacheManger = CacheManager.build(entity, context, cacheFactory);
        return entity;

    }


    /**
     * 重新延迟执行SQL标记
     */
    public final void resetDelaySqlFlag() {
        delaySQLEnable.remove();
    }

    public void setEnhance(boolean enhance) {
        this.enhance = enhance;
    }

    public String getInsertSQL() {
        return insertSQL;
    }

    public void setInsertSQL(String insertSQL) {
        this.insertSQL = insertSQL;
    }

    public String getUpdateSQL() {
        return updateSQL;
    }

    public void setUpdateSQL(String updateSQL) {
        this.updateSQL = updateSQL;
    }

    public String getSelectAllSQL() {
        return selectAllSQL;
    }

    public void setSelectAllSQL(String selectAllSQL) {
        this.selectAllSQL = selectAllSQL;
    }

    public String getSelectAllCountSQL() {
        return selectAllCountSQL;
    }

    public void setSelectAllCountSQL(String selectAllCountSQL) {
        this.selectAllCountSQL = selectAllCountSQL;
    }

    /**
     * 获取查询所有的SQL
     *
     * @param forUpdate
     * @return
     */
    public String getSelectSQL(boolean forUpdate) {
        if (!forUpdate) {
            return selectSQL;
        } else {
            return selectForUpdateSQL;
        }
    }

    public void setSelectSQL(String selectSQL) {
        this.selectSQL = selectSQL;
    }

    public String getSelectForUpdateSQL() {
        return selectForUpdateSQL;
    }

    public void setSelectForUpdateSQL(String selectForUpdateSQL) {
        this.selectForUpdateSQL = selectForUpdateSQL;
    }

    public String getDeleteSQL() {
        return deleteSQL;
    }

    public void setDeleteSQL(String deleteSQL) {
        this.deleteSQL = deleteSQL;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public ThreadLocal<Boolean> getDelaySQLEnable() {
        return delaySQLEnable;
    }

    public void setDelaySQLEnable(ThreadLocal<Boolean> delaySQLEnable) {
        this.delaySQLEnable = delaySQLEnable;
    }

    public CacheManager getCacheManger() {
        return cacheManger;
    }

    public void setCacheManger(CacheManager cacheManger) {
        this.cacheManger = cacheManger;
    }

    public JdbcField[] getIdFields() {
        return idFields;
    }

    public void setIdFields(JdbcField[] idFields) {
        this.idFields = idFields;
    }
}
