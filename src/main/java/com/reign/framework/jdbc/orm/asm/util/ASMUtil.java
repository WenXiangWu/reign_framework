package com.reign.framework.jdbc.orm.asm.util;

import java.lang.reflect.Method;

/**
 * @ClassName: ASMUtil
 * @Description: 工具
 * @Author: wuwx
 * @Date: 2021-04-08 15:22
 **/
public class ASMUtil {


    /**
     * ？ 的描述符
     */
    public static final String ANY_TYPE = "*";


    /**
     * 获取类名
     *
     * @param clazz
     * @return
     */
    public static String getClassName(Class<?> clazz) {
        String className = clazz.getName();
        return className.replace(".", "/");
    }


    /**
     * 获取描述，只有是泛型的类才有描述
     *
     * @param clazz
     * @param args
     * @return
     */
    public static String getSignature(Class<?> clazz, Class<?>... args) {
        if (null == args || args.length == 0) return null;
        StringBuilder builder = new StringBuilder();
        builder.append(getDesc(clazz, false)).append("<");
        for (Class<?> arg : args) {
            builder.append(getDesc(arg, true));
        }
        return builder.toString();
    }

    /**
     * 获取描述，只有是泛型的类才有描述
     *
     * @param clazz
     * @param args
     * @return
     */
    public static String getSignature(Class<?> clazz, Class<?>[][] args) {
        if (null == args || args.length == 0) return null;
        StringBuilder builder = new StringBuilder();
        builder.append(getDesc(clazz, false)).append("<");
        for (Class<?>[] arg : args) {
            if (arg.length == 1) {
                builder.append(getDesc(arg[0], true));
            } else {
                Class<?>[] array = new Class<?>[arg.length - 1];
                System.arraycopy(arg, 1, array, 0, array.length);
                builder.append(getSignature(arg[0], array));
            }

        }
        builder.append(">;");
        return builder.toString();
    }


    /**
     * 获取方法的描述符
     *
     * @param method
     * @return
     */
    public static String getDesc(Method method) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        java.lang.Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            builder.append(getDesc(types[i], false));
        }
        builder.append(")");
        builder.append(getDesc(method.getReturnType(), false));
        return builder.toString();

    }

    /**
     * 获取基本类型描述符
     *
     * @param type
     * @return
     */
    private static Object getPrimitiveLetter(Class<?> type) {
        if (Integer.TYPE.equals(type)) {
            return "I";
        } else if (Void.TYPE.equals(type)) {
            return "V";
        } else if (Boolean.TYPE.equals(type)) {
            return "Z";
        } else if (Character.TYPE.equals(type)) {
            return "C";
        } else if (Byte.TYPE.equals(type)) {
            return "B";
        } else if (Short.TYPE.equals(type)) {
            return "S";
        } else if (Float.TYPE.equals(type)) {
            return "F";
        } else if (Long.TYPE.equals(type)) {
            return "J";
        } else if (Double.TYPE.equals(type)) {
            return "D";
        }

        throw new IllegalStateException("Type :" + type.getCanonicalName() + " is not a primitive type");
    }


    /**
     * 获取描述符
     *
     * @param clazz
     * @param comma
     * @return
     */
    public static String getDesc(Class<?> clazz, boolean comma) {

        StringBuilder builder = new StringBuilder();
        if (clazz.isArray()) {
            builder.append("[").append(getDesc(clazz.getComponentType(), false));
        } else if (!clazz.isPrimitive()) {
            builder.append("L");
            String clazzName = clazz.getCanonicalName();
            if (clazz.isMemberClass()) {
                int dotIndex = clazzName.lastIndexOf(".");
                clazzName = clazzName.substring(0, dotIndex) + "$" + clazzName.substring(dotIndex + 1);
            }
            clazzName = clazzName.replaceAll("\\.", "/");
            builder.append(clazzName);
        } else {
            builder.append(getPrimitiveLetter(clazz));
        }
        if (comma) {
            builder.append(";");
        }
        return builder.toString();
    }
}
