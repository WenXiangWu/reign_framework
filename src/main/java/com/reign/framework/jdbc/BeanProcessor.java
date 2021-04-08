package com.reign.framework.jdbc;

import com.reign.framework.common.util.ReflectUtil;
import com.reign.framework.jdbc.orm.JdbcFactory;
import com.reign.framework.jdbc.orm.JdbcField;
import com.reign.framework.jdbc.orm.util.JdbcUtil;
import net.sf.cglib.beans.BeanMap;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * @ClassName: BeanProcessor
 * @Description: bean转换器
 * @Author: wuwx
 * @Date: 2021-04-07 18:13
 **/
public class BeanProcessor {
    /**
     * 用于标识属性在bean中未找到
     */
    protected static final int PROPERTY_NOT_FOUND = -1;

    /**
     * 基本类型的默认值
     */
    private static final Map<Class<?>, Object> primitiveDefaults = new HashMap<>();

    /**
     * 名称策略
     */
    private NameStrategy strategy = new DefaultNameStrategy();

    static {
        primitiveDefaults.put(Integer.TYPE, 0);
        primitiveDefaults.put(Short.TYPE, (short) 0);
        primitiveDefaults.put(Byte.TYPE, (byte) 0);
        primitiveDefaults.put(Float.TYPE, (float) 0);
        primitiveDefaults.put(Double.TYPE, (double) 0);
        primitiveDefaults.put(Long.TYPE, (long) 0);
        primitiveDefaults.put(Boolean.TYPE, Boolean.FALSE);
        primitiveDefaults.put(Character.TYPE, '\u0000');
    }

    public BeanProcessor() {
        super();
    }


    /**
     * 调用setter方法
     *
     * @param target 需要填充的对象
     * @param prop   填充的属性
     * @param value  填充的值
     * @throws SQLException
     */
    private void callSetter(Object target, PropertyDescriptor prop, Object value) throws SQLException {
        Method setter = prop.getWriteMethod();
        if (setter == null) return;
        Class<?>[] params = setter.getParameterTypes();

        try {
            if (value != null) {
                if (value instanceof java.util.Date) {
                    if (params[0].getName().equals("java.sql.Date")) {
                        value = new java.sql.Date(((java.sql.Date) value).getTime());
                    } else if (params[0].getName().equals("java.sql.Time")) {
                        value = new java.sql.Date(((java.sql.Time) value).getTime());
                    } else if (params[0].getName().equals("java.sql.Timestamp")) {
                        value = new java.sql.Date(((java.sql.Timestamp) value).getTime());
                    }

                }
            }

            if (this.isCompatibleType(value, params[0])) {
                setter.invoke(target, new Object[]{value});
            } else {
                throw new SQLException("Cannot set" + prop.getName() + ":incompatible types");
            }

        } catch (IllegalArgumentException e) {
            throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new SQLException("Cannot set " + prop.getName() + ": " + e.getMessage());
        }
    }

    /**
     * 创建bean
     *
     * @param rs               结果集
     * @param type             class类型
     * @param fields           属性集合
     * @param beanMap
     * @param columnToProperty 列到属性的映射
     * @param <T>
     * @return
     * @throws SQLException
     */
    private <T> T createBean(ResultSet rs, Class<T> type, JdbcField[] fields, BeanMap beanMap, int[] columnToProperty) throws SQLException {
        T bean = this.newInstance(type);
        BeanMap newBeanMap = beanMap.newInstance(bean);
        for (int i = 1; i < columnToProperty.length; i++) {
            if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
                continue;
            }
            JdbcField field = fields[columnToProperty[i]];
            Object value = this.processColumn(rs, i, field.jdbcType);
            if (value == null && field.field.getType().isPrimitive()) {
                value = primitiveDefaults.get(field.field.getType());
            }
            newBeanMap.put(field.propertyName, value);
        }
        return bean;
    }


    /**
     * 创建bean
     *
     * @param rs
     * @param type
     * @param fields
     * @param columnToProperty
     * @param <T>
     * @return
     * @throws SQLException
     */
    private <T> T createBean(ResultSet rs, Class<T> type, JdbcField[] fields, int[] columnToProperty) throws SQLException {
        T bean = this.newInstance(type);
        for (int i = 1; i < columnToProperty.length; i++) {
            if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
                continue;
            }
            JdbcField field = fields[columnToProperty[i]];
            Object value = this.processColumn(rs, i, field.jdbcType);
            if (value == null && field.field.getType().isPrimitive()) {
                value = primitiveDefaults.get(field.field.getType());
            }
            ReflectUtil.set(field.field, bean, value);
        }
        return bean;

    }

    /**
     * 创建bean
     *
     * @param rs
     * @param type
     * @param props
     * @param columnToProperty
     * @param <T>
     * @return
     * @throws SQLException
     */
    private <T> T createBean(ResultSet rs, Class<T> type, PropertyDescriptor[] props, int[] columnToProperty) throws SQLException {
        T bean = this.newInstance(type);
        for (int i = 1; i < columnToProperty.length; i++) {
            if (columnToProperty[i] == PROPERTY_NOT_FOUND) {
                continue;
            }
            PropertyDescriptor prop = props[columnToProperty[i]];
            Class<?> propType = prop.getPropertyType();
            Object value = this.processColumn(rs, i, propType);
            if (propType != null && value == null && propType.isPrimitive()) {
                value = primitiveDefaults.get(propType);
            }
            this.callSetter(bean, prop, value);
        }
        return bean;
    }


    /**
     * 将列映射和属性进行映射
     *
     * @param rsmd   列描述信息
     * @param fields fields
     * @return
     * @throws SQLException
     */
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, JdbcField[] fields) throws SQLException {
        int cols = rsmd.getColumnCount();
        int columnToProperty[] = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col < cols; col++) {
            String columnName = rsmd.getColumnName(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }

            //从属性列表中查找匹配的列
            for (int i = 0; i < fields.length; i++) {
                if (fields[i].columnName.equalsIgnoreCase(columnName)) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }
        return columnToProperty;
    }

    /**
     * 将列映射和属性进行映射
     *
     * @param rsmd  列描述信息
     * @param props 属性列表
     * @return
     * @throws SQLException
     */
    protected int[] mapColumnsToProperties(ResultSetMetaData rsmd, PropertyDescriptor[] props) throws SQLException {
        int cols = rsmd.getColumnCount();
        int columnToProperty[] = new int[cols + 1];
        Arrays.fill(columnToProperty, PROPERTY_NOT_FOUND);

        for (int col = 1; col < cols; col++) {
            String columnName = rsmd.getColumnName(col);
            if (null == columnName || 0 == columnName.length()) {
                columnName = rsmd.getColumnName(col);
            }

            //从属性列表中查找匹配的列
            for (int i = 0; i < props.length; i++) {
                if (strategy.columnNameToPropertyName(columnName).equalsIgnoreCase(props[i].getName())) {
                    columnToProperty[col] = i;
                    break;
                }
            }
        }
        return columnToProperty;
    }


    protected Object processColumn(ResultSet rs, int index, Class<?> propType) throws SQLException {
        if (propType.equals(String.class)) {
            return rs.getString(index);
        } else if (propType.equals(Integer.class) || propType.equals(int.class)) {
            return (rs.getInt(index));
        } else if (propType.equals(Boolean.class) || propType.equals(boolean.class)) {
            return (rs.getBoolean(index));
        } else if (propType.equals(Long.class) || propType.equals(long.class)) {
            return (rs.getLong(index));
        } else if (propType.equals(Double.class) || propType.equals(double.class)) {
            return (rs.getDouble(index));
        } else if (propType.equals(Float.class) || propType.equals(float.class)) {
            return (rs.getFloat(index));
        } else if (propType.equals(Short.class) || propType.equals(short.class)) {
            return (rs.getShort(index));
        } else if (propType.equals(Byte.class) || propType.equals(byte.class)) {
            return (rs.getByte(index));
        } else if (propType.equals(Timestamp.class)) {
            return rs.getTimestamp(index);
        } else {
            return rs.getObject(index);
        }
    }


    /**
     * @param rs
     * @param index
     * @param jdbcType
     * @return
     */
    private Object processColumn(ResultSet rs, int index, Type jdbcType) throws SQLException {
        switch (jdbcType) {
            case Object:
                return rs.getObject(index);
            case Int:
                return rs.getInt(index);
            case Long:
                return rs.getLong(index);
            case Double:
                return rs.getDouble(index);
            case Float:
                return rs.getFloat(index);
            case String:
                return rs.getString(index);
            case Byte:
                return rs.getByte(index);
            case SqlDate:
                return rs.getDate(index);
            case Timestamp:
                return rs.getTimestamp(index);
            case Time:
                return rs.getTime(index);
            case BigDecimal:
                return rs.getBigDecimal(index);
            case Blob:
                return rs.getBlob(index);
            case Clob:
                return rs.getClob(index);
            case NClob:
                return rs.getNClob(index);
            case Bytes:
                return rs.getBytes(index);
            case Bool:
                return rs.getBoolean(index);
            default:
                return rs.getObject(index);

        }

    }


    /**
     * 将结果集转换为指定的bean
     *
     * @param rs
     * @param type
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> T toBean(ResultSet rs, Class<T> type) throws SQLException {
        BeanMap beanMap = JdbcUtil.getBeanMap(type);
        JdbcField[] fields = JdbcUtil.getJdbcFields(type, strategy);
        if (null == fields) {
            //退化为原始方式
            PropertyDescriptor[] props = this.propertyDescriptors(type);
            ResultSetMetaData rsmd = rs.getMetaData();
            int[] columnToProperty = this.mapColumnsToProperties(rsmd, props);
            return this.createBean(rs, type, props, columnToProperty);
        } else if (null == beanMap) {
            ResultSetMetaData rsmd = rs.getMetaData();
            int[] columnToProperty = this.mapColumnsToProperties(rsmd, fields);
            return this.createBean(rs, type, fields, columnToProperty);
        } else {
            ResultSetMetaData rsmd = rs.getMetaData();
            int[] columnToProperty = this.mapColumnsToProperties(rsmd, fields);
            return this.createBean(rs, type, fields, beanMap, columnToProperty);
        }
    }


    /**
     * 将结果转换为BeanList
     *
     * @param rs
     * @param type
     * @param <T>
     * @return
     * @throws SQLException
     */
    public <T> List<T> toBeanList(ResultSet rs, Class<T> type) throws SQLException {
        List<T> results = new ArrayList<>();
        if (!rs.next()) {
            return results;
        }

        JdbcField[] fields = JdbcUtil.getJdbcFields(type, strategy);
        BeanMap beanMap = JdbcUtil.getBeanMap(type);
        if (null == fields) {
            //退还为原始方式
            PropertyDescriptor[] props = this.propertyDescriptors(type);
            ResultSetMetaData rsmd = rs.getMetaData();
            int[] columnToProperty = this.mapColumnsToProperties(rsmd, props);
            do {
                results.add(this.createBean(rs, type, props, columnToProperty));
            } while (rs.next());
        } else if (null == beanMap) {
            //使用field
            ResultSetMetaData rsmd = rs.getMetaData();
            int[] columnToProperty = this.mapColumnsToProperties(rsmd, fields);
            do {
                results.add(this.createBean(rs, type, fields, columnToProperty));
            } while (rs.next());
        } else {
            ResultSetMetaData rsmd = rs.getMetaData();
            int[] columnToProperty = this.mapColumnsToProperties(rsmd, fields);
            do {
                results.add(this.createBean(rs, type, fields, beanMap, columnToProperty));
            } while (rs.next());
        }
        return results;
    }


    /**
     * 获取propertyDescriptor集合类
     *
     * @param c
     * @return
     * @throws SQLException
     */
    private PropertyDescriptor[] propertyDescriptors(Class<?> c) throws SQLException {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(c);
        } catch (IntrospectionException e) {
            throw new SQLException("Bean introspection failed :" + e.getMessage());
        }
        return beanInfo.getPropertyDescriptors();
    }


    protected <T> T newInstance(Class<T> c) throws SQLException {
        try {
            return c.newInstance();
        } catch (InstantiationException e) {
            throw new SQLException("Cannot create " + c.getName() + ": " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new SQLException("Cannot create " + c.getName() + ": " + e.getMessage());
        }
    }

    /**
     * 是否是兼容的类型
     *
     * @param value 填充的值
     * @param type  类型
     * @return
     */
    private boolean isCompatibleType(Object value, Class<?> type) {
        if (value == null || type.isInstance(value)) {
            return true;
        } else if (type.equals(Integer.TYPE) && Integer.class.isInstance(value)) {
            return true;
        } else if (type.equals(Long.TYPE) && Long.class.isInstance(value)) {
            return true;
        } else if (type.equals(Double.TYPE) && Double.class.isInstance(value)) {
            return true;
        } else if (type.equals(Float.TYPE) && Float.class.isInstance(value)) {
            return true;
        } else if (type.equals(Short.TYPE) && Short.class.isInstance(value)) {
            return true;
        } else if (type.equals(Byte.TYPE) && Byte.class.isInstance(value)) {
            return true;
        } else if (type.equals(Character.TYPE) && Character.class.isInstance(value)) {
            return true;
        } else if (type.equals(Boolean.TYPE) && Boolean.class.isInstance(value)) {
            return true;
        } else {
            return false;
        }

    }
}
