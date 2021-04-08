package com.reign.framework.jdbc.orm.util;

import com.reign.framework.common.Lang;
import com.reign.framework.jdbc.NameStrategy;
import com.reign.framework.jdbc.orm.JdbcField;
import com.reign.framework.jdbc.orm.annotation.Id;
import com.reign.framework.jdbc.orm.annotation.IgnoreField;
import com.reign.framework.jdbc.orm.annotation.InsertIgnoreField;
import net.sf.cglib.beans.BeanMap;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: JdbcUtil
 * @Description: jdbc相关工具类
 * @Author: wuwx
 * @Date: 2021-04-07 15:39
 **/
public class JdbcUtil {


    private static Map<Class<?>, JdbcField[]> cacheMap = new HashMap<>();

    private static Map<Class<?>,BeanMap> beanMapMapCache = new HashMap<>();


    /**
     * 获取类型的BeanMap
     * @param clazz
     * @return
     */
    public static BeanMap getBeanMap (Class<?> clazz){
        return beanMapMapCache.get(clazz);
    }


    /**
     * 初始化beanMap类型
     * @param clazz
     * @return
     */
    public static BeanMap createBeanMap(Class<?> clazz){
        try {
            BeanMap beanMap = BeanMap.create(clazz.newInstance());
            beanMapMapCache.put(clazz, beanMap);
            return beanMap;
        }catch (InstantiationException e){
            e.printStackTrace();
        }catch (IllegalAccessException e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取jdbc field
     * @param clazz
     * @param nameStrategy
     * @return
     */
    public static JdbcField[] getJdbcFields(Class<?> clazz, NameStrategy nameStrategy){
        return cacheMap.get(clazz);
    }

    /**
     * 创建jdbc fields
     * @param clazz
     * @param nameStrategy
     * @return
     */
    public static JdbcField[] createJdbcFields(Class<?> clazz, NameStrategy nameStrategy){
        //即时生成
        Lang.MyField[] fields = Lang.getFields(clazz);
        if (null == fields) return null;
        JdbcField[] jdbcFields = null;
        if (fields.length == 0){
            jdbcFields = new JdbcField[0];
            cacheMap.put(clazz,jdbcFields);
            return jdbcFields;
        }

        jdbcFields = new JdbcField[fields.length];
        for (int i=0;i<fields.length;i++){
            jdbcFields[i].isPrimary = Lang.hasAnnotation(jdbcFields[i].field, Id.class);
            jdbcFields[i].insertIgnore = Lang.hasAnnotation(jdbcFields[i].field, InsertIgnoreField.class);
            jdbcFields[i].ignore = Lang.hasAnnotation(jdbcFields[i].field, IgnoreField.class);
            jdbcFields[i].jdbcType = Lang.getJdbcType(jdbcFields[i].field.getType());
        }
        cacheMap.put(clazz,jdbcFields);
        return jdbcFields;
    }

}
