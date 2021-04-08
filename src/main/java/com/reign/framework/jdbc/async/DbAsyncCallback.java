package com.reign.framework.jdbc.async;

import com.reign.framework.jdbc.SqlFactory;
import com.reign.framework.jdbc.orm.JdbcEntity;
import com.reign.framework.jdbc.orm.annotation.AsyncMethod;
import com.reign.framework.jdbc.sql.Sql;
import com.reign.framework.log.Logger;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @ClassName: DbAsyncCallback
 * @Description: 数据库异步操作
 * @Author: wuwx
 * @Date: 2021-04-07 18:30
 **/
public class DbAsyncCallback implements AsyncCallback {

    /**
     * id生成器
     */
    private static AtomicInteger idGenerator = new AtomicInteger();

    private Method method;

    private Object obj;

    private Object[] args;

    private String sql;

    private int id;

    @Override
    public void callback() {
        try {
            method.invoke(obj, args);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * 构造函数
     *
     * @param async
     * @param entity
     * @param factory
     * @param method
     * @param obj
     * @param args
     */
    public DbAsyncCallback(AsyncMethod async, JdbcEntity entity, SqlFactory factory, Method method, Object obj, Object[] args) {
        this.method = method;
        this.obj = obj;
        this.args = args;
        this.id = idGenerator.incrementAndGet();
        this.sql = getSQL(async, entity, factory, args);

    }

    /**
     * 获取SQL
     *
     * @param async
     * @param entity
     * @param factory
     * @param args
     * @return
     */
    private String getSQL(AsyncMethod async, JdbcEntity entity, SqlFactory factory, Object[] args) {
        String sql = async.sql();
        if (StringUtils.isNotBlank(sql)) {
            //不为空，直接返回
            String temp = factory.get(sql);
            sql = (null == temp) ? sql : temp;
            sql = sql.trim();
            return SqlFormatter.format(sql, args);
        }
        sql = null;
        switch (async.type()) {
            case INSERT:
                //实体插入
                sql = entity.getInsertSQL();
                sql = sql.trim();
                sql = SqlFormatter.format(sql, entity.builderInsertParams(args[0]));
                break;
            case UPDATE:
                //实体更新
                sql = entity.getUpdateSQL();
                sql = sql.trim();
                sql = SqlFormatter.format(sql, entity.builderUpdateParams(args[0]));
                break;
            case DELETE:
                //PK删除
                sql = entity.getDeleteSQL();
                sql = sql.trim();
                sql = SqlFormatter.format(sql, args);
                break;
        }
        return sql;
    }

    @Override
    public void doLog(Logger log, int type) {
        if (null != sql) {
            switch (type) {
                case 1:
                    //插入队列
                    log.info("{}#{}#{}", id, type, sql);
                    break;
                case 2:
                    //成功执行
                    log.info("{}#{}", id, type);
                    break;
                case 3:
                    //失败执行
                    log.info("{}#{}", id, type);
                    break;
                default:
                    break;
            }
        }

    }
}
