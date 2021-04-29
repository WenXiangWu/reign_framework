package com.reign.framework.core.mvc.spring;

import com.reign.framework.core.mvc.ObjectFactory;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * @ClassName: SpringObjectFactory
 * @Description: Bean对象工厂，用于兼容spring
 * @Author: wuwx
 * @Date: 2021-04-19 17:04
 **/
public class SpringObjectFactory extends ObjectFactory implements ApplicationContextAware {


    private static final Logger log = InternalLoggerFactory.getLogger(SpringObjectFactory.class);

    protected ApplicationContext context;

    protected AutowireCapableBeanFactory autowireCapableBeanFactory;

    protected int autowireStrategy = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    protected boolean alwaysRespectAutowireStrategy = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = context;
        this.autowireCapableBeanFactory = findAutoWiringBeanFactory(context);
    }

    @Override
    public Object buildBean(Class<?> clazz) throws IllegalAccessException, InstantiationException {
        Object o = null;
        try {
            o = context.getBean(clazz.getName());
        } catch (NoSuchBeanDefinitionException e) {
            o = _buildBean(clazz);
        }
        return o;
    }

    private Object _buildBean(Class<?> clazz) throws InstantiationException, IllegalAccessException {
        Object bean;
        try {
            if (alwaysRespectAutowireStrategy) {
                bean = autowireCapableBeanFactory.createBean(clazz, autowireStrategy, false);
                return bean;
            } else {
                bean = autowireCapableBeanFactory.autowire(clazz, AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR, false);
                bean = autowireCapableBeanFactory.applyBeanPostProcessorsBeforeInitialization(bean, bean.getClass().getName());
                bean = autowireCapableBeanFactory.applyBeanPostProcessorsAfterInitialization(bean, bean.getClass().getName());
                return autoWireBean(bean, autowireStrategy);
            }
        } catch (UnsatisfiedDependencyException e) {
            log.error("error build bean", e);
            return autoWireBean(clazz.newInstance(), autowireStrategy);
        }

    }

    /**
     * 注入bean对象
     *
     * @param bean
     * @param autowireStrategy
     * @return
     */
    private Object autoWireBean(Object bean, int autowireStrategy) {
        if (autowireCapableBeanFactory != null) {
            autowireCapableBeanFactory.autowireBeanProperties(bean, autowireStrategy, false);
        }
        injectApplicationContext(bean);
        return bean;
    }

    private void injectApplicationContext(Object bean) {
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(context);
        }
    }

    private AutowireCapableBeanFactory findAutoWiringBeanFactory(ApplicationContext context) {
        if (context instanceof AutowireCapableBeanFactory) {
            return (AutowireCapableBeanFactory) context;
        } else if (context instanceof ConfigurableApplicationContext) {
            return ((ConfigurableApplicationContext) context).getBeanFactory();
        } else if (context.getParent() != null) {
            return findAutoWiringBeanFactory(context.getParent());
        }
        return null;
    }
}
