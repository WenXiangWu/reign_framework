package com.reign.framework.common;

import com.reign.framework.core.mvc.spring.SpringObjectFactory;
import com.reign.framework.core.servlet.ServletContext;
import com.reign.framework.jdbc.Type;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import org.springframework.context.ApplicationContext;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
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


    private static final Logger log = InternalLoggerFactory.getLogger(Lang.class);

    /**
     * 默认值的map
     */
    private static Map<Class<?>, Object> DEFAULT_VALUE_MAP = new HashMap<>();

    /**
     * SIMPLE_CLASS
     */
    private static Set<Class<?>> SIMPLE_CLASS = new HashSet<>();

    private static Map<Class<?>, Class<?>> DEFAULT_WRAPPERCLASS_MAP = new HashMap<>();


    /**
     * 对象工厂
     */
    private static ObjectFactory objectFactory = null;

    private static ObjectFactory comObjectFactory = new ObjectFactory();

    public static Map<Class<?>, MyField[]> FIELD_MAP = new HashMap<>(1024);


    static {
        DEFAULT_VALUE_MAP.put(boolean.class, false);
        DEFAULT_VALUE_MAP.put(byte.class, 0);
        DEFAULT_VALUE_MAP.put(char.class, 0);
        DEFAULT_VALUE_MAP.put(short.class, 0);
        DEFAULT_VALUE_MAP.put(int.class, 0);
        DEFAULT_VALUE_MAP.put(long.class, 0);
        DEFAULT_VALUE_MAP.put(float.class, 0);
        DEFAULT_VALUE_MAP.put(double.class, 0);


        DEFAULT_WRAPPERCLASS_MAP.put(boolean.class, Boolean.class);
        DEFAULT_WRAPPERCLASS_MAP.put(byte.class, Byte.class);
        DEFAULT_WRAPPERCLASS_MAP.put(char.class, Character.class);
        DEFAULT_WRAPPERCLASS_MAP.put(short.class, Short.class);
        DEFAULT_WRAPPERCLASS_MAP.put(int.class, Integer.class);
        DEFAULT_WRAPPERCLASS_MAP.put(long.class, Long.class);
        DEFAULT_WRAPPERCLASS_MAP.put(float.class, Float.class);
        DEFAULT_WRAPPERCLASS_MAP.put(double.class, Double.class);


        for (Class<?> clazz : DEFAULT_VALUE_MAP.keySet()) {
            SIMPLE_CLASS.add(clazz);
        }

        for (Class<?> clazz : DEFAULT_WRAPPERCLASS_MAP.keySet()) {
            SIMPLE_CLASS.add(clazz);
        }
    }

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
     * 获取方法上的Annotation，会遍历父类和接口定义
     *
     * @param method
     * @param clazz
     * @param anClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> T getAnnotation(Method method, Class<?> clazz, Class<T> anClass) {
        T annotation = method.getAnnotation(anClass);
        if (null == annotation) {
            Class<?> cc = clazz.getSuperclass();
            //处理父类
            while (null != cc && cc != Object.class) {
                Method[] methods = cc.getDeclaredMethods();
                for (Method _method : methods) {
                    if (isSameMethod(method, _method)) {
                        annotation = _method.getAnnotation(anClass);
                        if (null != annotation) {
                            break;
                        }
                    }
                }
                if (null != annotation) {
                    break;
                }
                cc = cc.getSuperclass();
            }
            //处理接口
            if (null == annotation) {
                for (Class<?> _clazz : clazz.getInterfaces()) {
                    Method[] methods = _clazz.getDeclaredMethods();
                    for (Method _method : methods) {
                        if (isSameMethod(method, _method)) {
                            annotation = _method.getAnnotation(anClass);
                            if (null != annotation) {
                                break;
                            }
                        }

                    }
                    if (null != annotation) {
                        break;
                    }
                }
            }
        }
        return annotation;
    }

    /**
     * 判断两个方法是否相同
     *
     * @param method1
     * @param method2
     * @return
     */
    private static boolean isSameMethod(Method method1, Method method2) {
        if (!method1.getName().equalsIgnoreCase(method2.getName())) {
            return false;
        }
        if (!arrayEq(method1.getParameterTypes(), method2.getParameterTypes())) {
            return false;
        }
        return true;
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

    /**
     * 创建对象
     *
     * @param servletContext
     * @param clazz
     * @return
     * @throws Exception
     */
    public static Object createObject(ServletContext servletContext, Class<?> clazz) throws Exception {
        ObjectFactory localObjectFactory = objectFactory;
        if (null == localObjectFactory) {
            SpringObjectFactory springObjectFactory = new SpringObjectFactory();
            ApplicationContext applicationContext = (ApplicationContext) servletContext.getAttribute(ServletContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
            if (null == applicationContext) {
                log.info("applicationContext cound not be found,Action classes will not be autowired");
                localObjectFactory = comObjectFactory;

            } else {
                springObjectFactory.setApplicationContext(applicationContext);
                localObjectFactory = springObjectFactory;
                objectFactory = springObjectFactory;
            }
        }
        return localObjectFactory.buildBean(clazz);
    }


    public static <T> T castTo(Object src, Class<T> clazz) {
        if (null == src) {
            return (T) Lang.getDefaultValue(clazz);
        }
        return castTo(src, src.getClass(), clazz);
    }


    /**
     * 将src从原始类型转换为目标类型
     *
     * @param src
     * @param fromType
     * @param toType
     * @param <T>
     * @return
     */
    public static <T, F> T castTo(Object src, Class<F> fromType, Class<T> toType) {
        if (fromType.getName().equals(toType.getName())) {
            return (T) src;
        } else if (toType.isAssignableFrom(fromType)) {
            return (T) src;
        }

        if (fromType == String.class) {
            return String2Object((String) src, toType);
        }
        if (fromType.isArray() && !toType.isArray()) {
            return castTo(Array.get(src, 0), toType);
        }

        if (fromType.isArray() && toType.isArray()) {
            int len = Array.getLength(src);
            Object result = Array.newInstance(toType.getComponentType(), len);
            for (int i = 0; i < len; i++) {
                Array.set(result, i, castTo(Array.get(src, i), toType.getComponentType()));
            }
            return (T) result;
        }
        return (T) Lang.getDefaultValue(toType);

    }

    private static <T> T String2Object(String str, Class<T> type) {
        try {
            if (isBoolean(type)) {
                return (T) Boolean.valueOf(str);
            } else if (isByte(type)) {
                return (T) Byte.valueOf(str);
            } else if (isChar(type)) {
                return (T) Character.valueOf(str.charAt(0));
            } else if (isInteger(type)) {
                return (T) Integer.valueOf(str);
            } else if (isFloat(type)) {
                return (T) Float.valueOf(str);
            } else if (isLong(type)) {
                return (T) Long.valueOf(str);
            } else if (isDouble(type)) {
                return (T) Double.valueOf(str);
            } else if (isShort(type)) {
                return (T) Short.valueOf(str);
            } else if (isStringLike(type)) {
                return (T) str;
            } else if (isString(type)) {
                return (T) str;
            } else {
                Constructor<T> constructor = (Constructor<T>) Lang.getWrapper(type).getConstructor(String.class);
                if (null != constructor) {
                    return constructor.newInstance(str);
                }
            }
        } catch (Throwable t) {
            //不处理
        }
        return (T) Lang.getDefaultValue(type);
    }

    public static boolean isString(Class<?> clazz) {
        return is(clazz, String.class);
    }

    public static Class<?> getWrapper(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return DEFAULT_WRAPPERCLASS_MAP.get(clazz);
        }
        return clazz;
    }


    public static Object getDefaultValue(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return DEFAULT_VALUE_MAP.get(clazz);
        }
        return null;
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
     * 判断两个数组是否绝对相等
     *
     * @param a1
     * @param a2
     * @return
     */
    private static boolean arrayEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }
        if (a2 == null) return a1.length == 0;

        if (a1.length != a2.length) return false;

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
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
        return CharSequence.class.isAssignableFrom(clazz);
    }

    public static boolean isByte(Class<?> clazz) {
        return Byte.class.isAssignableFrom(clazz);
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

    public static boolean isStaticMethod(Method method) {
        return Modifier.isStatic(method.getModifiers()) && !Modifier.isFinal(method.getModifiers());
    }

}
