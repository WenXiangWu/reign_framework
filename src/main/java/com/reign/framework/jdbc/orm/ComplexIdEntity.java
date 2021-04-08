package com.reign.framework.jdbc.orm;

import java.util.Arrays;

/**
 * @ClassName: ComplexIdEntity
 * @Description: 复合主键
 * @Author: wuwx
 * @Date: 2021-04-02 18:35
 **/
public class ComplexIdEntity implements IdEntity {


    /**
     * 列
     */
    private JdbcField[] fields;

    /**
     * 是否自动生成
     */
    private boolean autoGenerator;


    /**
     * 实体
     */
    private JdbcEntity entity;


    public ComplexIdEntity(JdbcField[] fields, JdbcEntity entity) {
        this.fields = fields;
        this.autoGenerator = false;
        this.entity = entity;
    }

    @Override
    public boolean isAutoGenerator() {
        return autoGenerator;
    }

    @Override
    public void setIdValue(Object obj, Object... args) {
        if (!autoGenerator) {
            return;
        }
        int index = 0;
        try {
            for (JdbcField field : fields) {
                field.field.setAccessible(true);
                field.field.set(obj, args[index]);
            }
        } catch (Throwable t) {
            throw new RuntimeException("set key error", t);
        }

    }

    @Override
    public Object[] getIdValue(Object obj) {
        try {
            Object[] array = new Object[fields.length];
            int index = 0;
            for (JdbcField field : fields) {
                field.field.setAccessible(true);
                array[index++] = field.field.get(obj);
            }
            return array;
        } catch (Throwable t) {
            throw new RuntimeException("get key error", t);
        }
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
            throw new RuntimeException("get key error", t);
        }
    }

    @Override
    public String getKeyValuesByParams(Object... args) {
        return Arrays.toString(args);
    }

    @Override
    public String[] getIdColumnName() {
        String[] names = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            names[i] = fields[i].columnName;
        }
        return names;
    }
}
