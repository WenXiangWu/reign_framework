package com.reign.framework.jdbc.orm;

import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.Params;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName: DefaultIndexEntity
 * @Description: 默认key实体
 * @Author: wuwx
 * @Date: 2021-04-07 15:28
 **/
public class DefaultIndexEntity implements IndexEntity {

    /**
     * field
     */
    private JdbcField[] fields;


    /**
     * 实体
     */
    private JdbcEntity entity;


    /**
     * 查询SQL语句
     */
    private String selectSQL;

    /**
     * 是否已经初始化
     */
    private boolean init;

    public DefaultIndexEntity(JdbcField[] fields, JdbcEntity entity) {
        this.fields = fields;
        this.entity = entity;
    }

    public boolean isInit() {
        return init;
    }

    @Override
    public synchronized void init() {
        if (init) return;
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT * FROM ")
                .append(entity.getTableName())
                .append(" WHERE ");
        int index = 1;
        for (JdbcField field : fields) {
            if (index != 1) {
                builder.append(" AND ");
            }
            builder.append(field.columnName)
                    .append("=")
                    .append("?");
            index++;
        }
        selectSQL = builder.toString();
        init = true;
    }

    @Override
    public String selectSQL() {
        return selectSQL;
    }

    @Override
    public List<Param> builderParams(Object... args) {
        Params params = new Params();
        int index = 0;
        for (JdbcField field : fields) {
            params.addParam(args[index++], field.jdbcType);
        }
        return params;
    }

    @Override
    public String getKeyValueByParams(Object... args) {
        return Arrays.toString(args);
    }

    @Override
    public String getKeyValueByObject(Object obj) {
        try {
            Object[] array = new Object[fields.length];
            int index = 0;
            for (JdbcField field : fields) {
                field.field.setAccessible(true);
                array[index++] = field.field.get(obj);
            }
            return Arrays.toString(array);
        } catch (Throwable t) {
            throw new RuntimeException("get key error ", t);
        }
    }

    public static void main(String[] args) {
        DefaultIndexEntity indexEntity = new DefaultIndexEntity(null,null);
        System.out.println(indexEntity.getKeyValueByParams("123",123));
    }
}
