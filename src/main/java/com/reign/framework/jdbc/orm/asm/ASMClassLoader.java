package com.reign.framework.jdbc.orm.asm;

/**
 * @ClassName: ASMClassLoader
 * @Description: ASM类加载器
 * @Author: wuwx
 * @Date: 2021-04-08 15:46
 **/
public class ASMClassLoader extends ClassLoader {

    public ASMClassLoader(ClassLoader loader) {
        super(loader);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = null;
        //先查找已经载入的class
        clazz = findLoadedClass(name);
        if (null != clazz) {
            return clazz;
        }

        //父加载器查找
        try {
            clazz = super.loadClass(name);
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        }

        if (null != clazz) {
            return clazz;
        }
        throw new ClassNotFoundException();
    }

    /**
     * 自定义class
     *
     * @param name
     * @param date
     * @return
     */
    public Class<?> loadClassFromBytes(String name, byte[] date) {
        //自定义
        return defineClass(name, date, 0, date.length);
    }
}
