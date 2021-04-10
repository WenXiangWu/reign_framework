package com.reign.framework.jdbc.orm;

import com.reign.framework.common.Lang;
import com.reign.framework.common.util.Scans;
import com.reign.framework.common.util.XML;
import com.reign.framework.jdbc.DefaultNameStrategy;
import com.reign.framework.jdbc.orm.cache.CacheConfig;
import com.reign.framework.jdbc.orm.cache.CacheFactory;
import com.reign.framework.jdbc.orm.extractor.BaseJdbcExtractor;
import com.reign.framework.jdbc.orm.session.DefaultJdbcSession;
import com.reign.framework.jdbc.orm.session.JdbcSession;
import com.reign.framework.jdbc.orm.session.JdbcSessionUtil;
import com.reign.framework.jdbc.orm.transaction.JdbcTransaction;
import com.reign.framework.jdbc.orm.transaction.TransactionListener;
import com.reign.framework.log.TransLoggerManager;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.*;

/**
 * @ClassName: JdbcFactory
 * @Description: jdbcContext运行环境
 * @Author: wuwx
 * @Date: 2021-04-02 14:51
 **/
public class JdbcFactory implements InitializingBean {


    /**
     * 扫描的包
     */
    private String scanPackage;

    /**
     * entityMap
     */
    private Map<Class<?>, JdbcEntity> entityMap = new HashMap<>();

    /**
     * 缓存配置map
     */
    private Map<String, CacheConfig> cacheConfigMap = new HashMap<>();


    private CacheFactory cacheFactory;


    private BaseJdbcExtractor baseJdbcExtractor;

    private DataSource dataSource;

    /**
     * 事务监听器
     */
    private List<TransactionListener> listeners;

    /**
     * 缓存配置文件
     */
    private String cacheConfigFile;

    public void setScanPackage(String scanPackage) {
        this.scanPackage = scanPackage;
    }

    public void setCacheFactory(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    public CacheFactory getCacheFactory() {
        return cacheFactory;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public BaseJdbcExtractor getBaseJdbcExtractor() {
        return baseJdbcExtractor;
    }

    public void setBaseJdbcExtractor(BaseJdbcExtractor baseJdbcExtractor) {
        this.baseJdbcExtractor = baseJdbcExtractor;
        if (this.baseJdbcExtractor instanceof TransactionListener) {
            addListener((TransactionListener) this.baseJdbcExtractor);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    public String getScanPackage() {
        return scanPackage;
    }

    /**
     * 初始化
     *
     * @throws Exception
     */
    public void init() throws Exception {

        //加载cache配置文件
        parseCacheConfig();

        //扫描jdbc组件
        Set<Class<?>> set = Scans.getClasses(getScanPackage());

        //TODO 扫描插件中的jdbc组件+

        //解析jdbc组件
        for (Class<?> clazz : set) {
            com.reign.framework.jdbc.orm.annotation.JdbcEntity domain = Lang.getAnnotation(clazz, com.reign.framework.jdbc.orm.annotation.JdbcEntity.class);
            if (null != domain) {
                //表明是一个数据库实体映射类
                JdbcEntity entity = JdbcEntity.resolve(clazz, new DefaultNameStrategy(), this, cacheFactory);
                entityMap.put(clazz, entity);
            }
        }
        //添加logListener
        addListener(TransLoggerManager.getInstance());

    }

    /**
     * 获取当前jdbcSession
     *
     * @return
     */
    public JdbcSession openSession() {
        return new DefaultJdbcSession(dataSource, this);
    }


    /**
     * 获取jdbc实体
     *
     * @param clazz
     * @return
     */
    public JdbcEntity getJdbcEntity(Class<?> clazz) {
        return entityMap.get(clazz);
    }

    public JdbcSession getCurrentSession() {
        return JdbcSessionUtil.getSession(this, false);
    }


    /**
     * 解析缓存配置
     */
    private void parseCacheConfig() {
        //读取缓存配置
        if (null != cacheConfigFile) {
            XML xml = new XML(cacheConfigFile);
            //获取默认配置
            XML.XMLNode node = xml.get("defaultCache");
            if (null != node) {
                int timeToLiveSeconds = Integer.valueOf(node.getAttribute("timeToLiveSeconds"));
                CacheConfig config = new CacheConfig("defaultCache", timeToLiveSeconds);
                cacheConfigMap.put("defaultCache", config);
            }

            //获取查询缓存配置
            node = xml.get("queryCache");
            if (null != node) {
                int timeToLiveSeconds = Integer.valueOf(node.getAttribute("timeToLiveSeconds"));
                CacheConfig config = new CacheConfig("queryCache", timeToLiveSeconds);
                cacheConfigMap.put("queryCache", config);
            }

            //其他配置
            List<XML.XMLNode> nodeList = xml.getList("cache");
            for (XML.XMLNode temp : nodeList) {
                int timeToLiveSeconds = Integer.valueOf(node.getAttribute("timeToLiveSeconds"));
                String name = temp.getAttribute("name");
                CacheConfig config = new CacheConfig(name, timeToLiveSeconds);
                cacheConfigMap.put(name, config);
            }
        }
        if (cacheConfigMap.size() == 0 || cacheConfigMap.get("defaultCache") == null) {
            CacheConfig config = new CacheConfig("defaultCache", 10 * 60);
            cacheConfigMap.put("defaultCache", config);
        }

    }

    /**
     * 增加监听器
     *
     * @param listener
     */
    private void addListener(TransactionListener listener) {
        if (null == listeners) {
            listeners = new ArrayList<>();
        }
        listeners.add(listener);

    }

    /**
     * 通知事务开始
     *
     * @param jdbcTransaction
     */
    public void notifyTransactionBegin(JdbcTransaction jdbcTransaction) {
        if (null == listeners) return;
        for (TransactionListener listener : listeners) {
            listener.begin(jdbcTransaction);
        }
    }


    /**
     * 通知事务已经提交完毕
     *
     * @param jdbcTransaction
     * @param succ
     */
    public void notifyTransactionCommit(JdbcTransaction jdbcTransaction, boolean succ) {
        if (null == listeners) return;
        for (TransactionListener listener : listeners) {
            listener.commit(jdbcTransaction, succ);
        }

    }

    /**
     * 通知事务开始提交
     *
     * @param jdbcTransaction
     * @param succ
     */
    public void notifyTransactionBeforeCommit(JdbcTransaction jdbcTransaction, boolean succ) {
        if (null == listeners) return;
        for (TransactionListener listener : listeners) {
            listener.beforeCommit(jdbcTransaction, succ);
        }
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 获取缓存配置文件
     *
     * @param name
     * @return
     */
    public CacheConfig getCacheConfig(String name) {
        CacheConfig config = cacheConfigMap.get(name);
        if (null == config) {
            config = cacheConfigMap.get("defaultCache");
        }
        return config;
    }
}
