package com.reign.framework.jdbc.orm.extractor;

import com.reign.framework.common.util.ReflectUtil;
import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.JdbcField;
import com.reign.framework.jdbc.orm.util.JdbcUtil;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import net.sf.cglib.beans.BeanMap;
import org.springframework.beans.BeanUtils;

/**
 * @ClassName: CacheEntry
 * @Description: 缓存实体
 * @Author: wuwx
 * @Date: 2021-04-08 18:13
 **/
public class CacheEntry {

    private static final Logger log = InternalLoggerFactory.getLogger(CacheEntry.class);

    /**
     * 缓存对象本身
     */
    private Object value;

    /**
     * 缓存对象clazz
     */
    private Class<?> clazz;

    /**
     * 缓存对象类型
     */
    public CacheItemType type;

    /**
     * 缓存对象实体
     */
    public JdbcEntity entity;


    public CacheEntry(Object value, Class<?> clazz, CacheItemType type, JdbcEntity entity) {
        super();
        this.value = value;
        this.clazz = clazz;
        this.type = type;
        this.entity = entity;
    }

    /**
     * 获取对象
     *
     * @return
     */
    public Object getValue() {
        if (null == value) return null;
        switch (type) {
            case Object:
                return createBean();
            case Primitive:
                return value;
            default:
                return value;

        }
    }

    /**
     * 拷贝一个bean
     *
     * @return
     */
    private Object createBean() {
        try {
            //拷贝对象
            BeanMap beanMap = JdbcUtil.getBeanMap(clazz);
            JdbcField[] fields = JdbcUtil.getJdbcFields(clazz, entity.getNameStrategy());
            if (null == fields) {
                //原始拷贝
                Object dest = clazz.newInstance();
                BeanUtils.copyProperties(value, dest);
                return dest;
            } else if (null == beanMap) {
                //Field set
                Object dest = clazz.newInstance();
                for (JdbcField field : fields) {
                    if (field.field == null) {
                        continue;
                    }
                    ReflectUtil.set(field.field, dest, ReflectUtil.get(field.field, value));
                }
                return dest;

            } else {
                //copy bean
                Object dest = clazz.newInstance();
                BeanMap newBeanMap = beanMap.newInstance(dest);
                newBeanMap.putAll(beanMap.newInstance(value));
                return dest;
            }

        } catch (Throwable t) {
            log.error("copy cacheEntry error,[entryName:{}]", t, entity.getEntityClass().getSimpleName());
        }
        //不拷贝
        return value;
    }
}
