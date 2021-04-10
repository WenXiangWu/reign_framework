package com.reign.framework.common;

import com.reign.framework.jdbc.Type;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import java.util.*;

/**
 * @ClassName: Lang
 * @Description: 工具类
 * @Author: wuwx
 * @Date: 2021-04-02 17:34
 **/
public class Lang {


    public static Map<Class<?>, MyField[]> FIELD_MAP = new HashMap<>(1024);

    /**
     * 对象工厂
     */
    private static ObjectFactory objectFactory = new ObjectFactory();

    /**
     * 获取类上的指定注解
     *
     * @param clazz
     * @param anClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T getAnnotation(Class<?> clazz, Class<T> anClass) {
        Class<?> cc = clazz;
        T annotation = null;
        while (null != cc && cc != Object.class) {
            annotation = cc.getAnnotation(anClass);
            if (null != annotation) {
                return annotation;
            }
            cc = cc.getSuperclass();
        }
        return null;
    }

    /**
     * 获取jdbc的类型
     *
     * @param type
     * @return
     */
    public static Type getJdbcType(String type) {
        type = type.toLowerCase();
        if (type.startsWith("int") || type.startsWith("tinyint") || type.startsWith("mediumint") || type.startsWith("bit") || type.startsWith("smallint")) {
            return Type.Int;
        } else if (type.startsWith("bigint")) {
            return Type.Long;
        } else if (type.startsWith("text") || type.startsWith("varchar") || type.startsWith("mediumtext")) {
            return Type.String;
        } else if (type.startsWith("datetime") || type.startsWith("timestamp") || type.startsWith("date")) {
            return Type.Date;
        } else if (type.startsWith("float")) {
            return Type.Float;
        } else if (type.startsWith("double")) {
            return Type.Double;
        } else if (type.startsWith("blob")) {
            return Type.Bytes;
        }
        return null;
    }

    /**
     * 获取一个clazz的所有成员列表
     *
     * @param clazz
     * @return
     */
    public static MyField[] getFields(Class<?> clazz) {
        MyField[] myFields = FIELD_MAP.get(clazz);
        if (null == myFields) {
            synchronized (FIELD_MAP) {
                myFields = FIELD_MAP.get(clazz);
                if (null == myFields) {
                    try {
                        //解析bean
                        BeanInfo bi = Introspector.getBeanInfo(clazz);
                        PropertyDescriptor[] pds = bi.getPropertyDescriptors();
                        Map<String, PropertyDescriptor> map = new LinkedHashMap<>();
                        for (PropertyDescriptor pd : pds) {
                            if (pd.getPropertyType() == Class.class) {
                                continue;
                            }
                            map.put(pd.getDisplayName(), pd);
                        }

                        //解析field
                        Field[] fields = clazz.getDeclaredFields();
                        List<MyField> list = new ArrayList<>();
                        for (Field field : fields) {
                            PropertyDescriptor pd = map.get(field.getName());
                            if (null == pd) continue;
                            MyField myField = new MyField();
                            myField.fieldName = pd.getDisplayName();
                            myField.type = getType(pd.getPropertyType());
                            myField.getter = pd.getReadMethod();
                            myField.writter = pd.getWriteMethod();
                            list.add(myField);
                        }
                        myFields = list.toArray(new MyField[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    FIELD_MAP.put(clazz, myFields);

                }

            }
        }
        return myFields;
    }

    /**
     * 获取类的类型
     *
     * @param clazzType
     * @return
     */
    private static ClassType getType(Class<?> clazzType) {
        ClassType type = ClassType.PRIMITIVE_TYPE;
        if (clazzType.isAssignableFrom(Date.class)) {
            type = ClassType.DATE_TYPE;
        } else if (clazzType.isAssignableFrom(Map.class)) {
            type = ClassType.MAP_TYPE;
        } else if (clazzType.isAssignableFrom(List.class)) {
            type = ClassType.LIST_TYPE;
        } else if (clazzType.isArray()) {
            type = ClassType.ARRAY_TYPE;
        }
        return type;

    }

    public static Object createObject(Class<?> enhanceClazz) {
        try {
            return objectFactory.buildBean(enhanceClazz);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public enum ClassType {
        PRIMITIVE_TYPE, STATIC_TYPE, FINAL_TYPE, DATE_TYPE, MAP_TYPE, LIST_TYPE, ARRAY_TYPE
    }

    public static class MyField {
        public Field field;
        public String fieldName;
        public ClassType type;
        public Method getter;
        public Method writter;

    }

    /**
     * 是否为int类型
     *
     * @param clazz
     * @return
     */
    public static boolean isInteger(Class<?> clazz) {
        return is(clazz, int.class) || is(clazz, Integer.class);
    }

    public static boolean isLong(Class<?> clazz) {
        return is(clazz, long.class) || is(clazz, Long.class);
    }

    public static boolean isFloat(Class<?> clazz) {
        return is(clazz, float.class) || is(clazz, Float.class);
    }

    public static boolean isDouble(Class<?> clazz) {
        return is(clazz, double.class) || is(clazz, Double.class);
    }

    public static boolean isBoolean(Class<?> clazz) {
        return is(clazz, boolean.class) || is(clazz, Boolean.class);
    }

    private static boolean is(Class<?> clazz1, Class<?> clazz2) {
        return clazz1 == clazz2;
    }

    public static boolean isStringLike(Class<?> clazz) {
        return is(clazz, int.class) || is(clazz, Integer.class);
    }

    public static boolean isByte(Class<?> clazz) {
        return CharSequence.class.isAssignableFrom(clazz);
    }

    public static boolean isShort(Class<?> clazz) {
        return is(clazz, short.class) || is(clazz, Short.class);
    }

    public static boolean isChar(Class<?> clazz) {
        return is(clazz, char.class) || is(clazz, Character.class);
    }


    public static Type getJdbcType(Class<?> clazzType) {
        Type type = Type.Object;
        if (isInteger(clazzType)) {
            type = Type.Int;
        } else if (isLong(clazzType)) {
            type = Type.Long;
        } else if (isDouble(clazzType)) {
            type = Type.Double;
        } else if (isFloat(clazzType)) {
            type = Type.Float;
        } else if (isStringLike(clazzType)) {
            type = Type.String;
        } else if (isByte(clazzType)) {
            type = Type.Byte;
        } else if (isShort(clazzType)) {
            type = Type.Int;
        } else if (isChar(clazzType)) {
            type = Type.Int;
        } else if (is(clazzType, java.util.Date.class)) {
            type = Type.Date;
        } else if (is(clazzType, java.sql.Timestamp.class)) {
            type = Type.Timestamp;
        } else if (clazzType.isAssignableFrom(BigDecimal.class)) {
            type = Type.BigDecimal;
        } else if (clazzType.isAssignableFrom(Blob.class)) {
            type = Type.Blob;
        } else if (clazzType.isAssignableFrom(Clob.class)) {
            type = Type.Clob;
        } else if (clazzType.isAssignableFrom(NClob.class)) {
            type = Type.NClob;
        } else if (is(byte[].class, clazzType) || is(Byte[].class, clazzType)) {
            type = Type.Bytes;
        } else if (isBoolean(clazzType)) {
            type = Type.Bool;
        }
        return type;
    }


    /**
     * 判断field上的是否包含指定注解
     *
     * @param field
     * @param anClass
     * @return
     */
    public static boolean hasAnnotation(Field field, Class<? extends Annotation> anClass) {
        Annotation annotation = field.getAnnotation(anClass);
        return null != annotation;
    }

    /**
     * 老版本apache common包中的
     *
     * @param str
     * @return
     */
    public static String capitalize(String str) {
        int strLen;
        return str != null && (strLen = str.length()) != 0 ? (new StringBuffer(strLen).append(Character.toTitleCase(str.charAt(0))).append(str.substring(1))).toString() : str;

    }

}
