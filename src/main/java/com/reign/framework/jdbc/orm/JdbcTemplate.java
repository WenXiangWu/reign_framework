package com.reign.framework.jdbc.orm;

import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.ResultSetHandler;
import com.reign.framework.jdbc.orm.extractor.BaseJdbcExtractor;
import com.reign.framework.jdbc.orm.session.JdbcSession;
import com.reign.framework.jdbc.orm.session.JdbcSessionUtil;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: JdbcTemplate
 * @Description: jdbc模板类
 * @Author: wuwx
 * @Date: 2021-04-02 14:51
 **/
public class JdbcTemplate implements BaseJdbcExtractor {


    private JdbcFactory jdbcFactory;

    public JdbcFactory getJdbcFactory() {
        return jdbcFactory;
    }

    public void setJdbcFactory(JdbcFactory jdbcFactory) {
        this.jdbcFactory = jdbcFactory;
    }

    @Override
    public <T, PK> T read(PK pk, JdbcEntity entity, ResultSetHandler<T> handler) {
        return doExecute(new JdbcCallBack<T>() {
            @Override
            public T doInJdbcSession(JdbcSession session) {
                return session.read(pk, entity, handler);
            }
        });
    }

    @Override
    public <T, PK> T readByIndex(Object[] indexs, JdbcEntity entity, ResultSetHandler<T> handler) {
        return doExecute(new JdbcCallBack<T>() {
            @Override
            public T doInJdbcSession(JdbcSession session) {
                return session.readByIndex(indexs, entity, handler);
            }
        });
    }

    @Override
    public <T, PK> void insert(T newInstance, JdbcEntity entity, String... keys) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.insert(newInstance, entity, keys);
                return null;
            }
        });
    }

    @Override
    public <T, PK> void insertDelay(T newInstance, JdbcEntity entity, String... keys) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.insertDelay(newInstance, entity, keys);
                return null;
            }
        });
    }

    @Override
    public <T> void update(T transientObject, JdbcEntity entity) {
        doExecute(new JdbcCallBack<Integer>() {
            @Override
            public Integer doInJdbcSession(JdbcSession session) {
                session.update(transientObject, entity);
                return null;
            }
        });
    }

    @Override
    public <PK> void delete(PK id, JdbcEntity entity) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.delete(id, entity);
                return null;
            }
        });
    }

    @Override
    public <T> List<T> query(final String selectKey,final String sql,final List<Param> params,final JdbcEntity entity,final ResultSetHandler<List<T>> handler) {
        return doExecute(new JdbcCallBack<List<T>>() {
            @Override
            public List<T> doInJdbcSession(JdbcSession session) {
                return session.query(selectKey, sql, params, entity, handler);
            }
        });
    }

    @Override
    public <PK> int update(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        return doExecute(new JdbcCallBack<Integer>() {
            @Override
            public Integer doInJdbcSession(JdbcSession session) {
                return session.update(sql, params, entity, pk, keys);
            }
        });
    }

    @Override
    public <PK> void updateDelay(String sql, List<Param> params, JdbcEntity entity, PK pk, String... keys) {
        doExecute(new JdbcCallBack<Integer>() {
            @Override
            public Integer doInJdbcSession(JdbcSession session) {
                session.updateDelay(sql, params, entity, pk, keys);
                return null;
            }
        });
    }


    @Override
    public <T> T query(String sql, List<Param> params, JdbcEntity entity, ResultSetHandler<T> handler) {
        return doExecute(new JdbcCallBack<T>() {
            @Override
            public T doInJdbcSession(JdbcSession session) {
                return session.query(sql, params, entity, handler);
            }
        });
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params, JdbcEntity entity) {
        return doExecute(new JdbcCallBack<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInJdbcSession(JdbcSession session) {
                return session.query(sql, params, entity);
            }
        });
    }

    @Override
    public <T> T query(String sql, List<Param> params, ResultSetHandler<T> handler) {
        return doExecute(new JdbcCallBack<T>() {
            @Override
            public T doInJdbcSession(JdbcSession session) {
                return session.query(sql, params, handler);
            }
        });
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params) {
        return doExecute(new JdbcCallBack<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInJdbcSession(JdbcSession session) {
                return session.query(sql, params);
            }
        });
    }

    @Override
    public int update(String sql, List<Param> params) {
        return doExecute(new JdbcCallBack<Integer>() {
            @Override
            public Integer doInJdbcSession(JdbcSession session) {
                return session.update(sql, params);
            }
        });
    }

    @Override
    public int insert(String sql, List<Param> params, boolean autoGenerator) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.insert(sql, params, autoGenerator);
                return null;
            }
        });
        return 0;
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.batch(sql, paramList);
                return null;
            }
        });
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList, int batchSize) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.batch(sql, paramList, batchSize);
                return null;
            }
        });
    }

    @Override
    public void batch(final String sql, final List<List<Param>> paramList, final JdbcEntity entity, final String... keys) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.batch(sql, paramList, entity, keys);
                return null;
            }
        });
    }

    @Override
    public void batch(List<String> sqlList) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.batch(sqlList);
                return null;
            }
        });
    }

    @Override
    public void batch(List<String> sqlList, int batchSize) {
        doExecute(new JdbcCallBack<Object>() {
            @Override
            public Object doInJdbcSession(JdbcSession session) {
                session.batch(sqlList, batchSize);
                return null;
            }
        });
    }


    protected <T> T doExecute(JdbcCallBack<T> action) {
        JdbcSession session = getSession();
        boolean existTrans = JdbcSessionUtil.isSessionTransactional(session, getJdbcFactory());

        try {
            T result = action.doInJdbcSession(session);
            return result;
        } catch (RuntimeException e) {
            throw e;
        } finally {
            if (!existTrans) {
                session.close();
            }
        }
    }

    private JdbcSession getSession() {
        return JdbcSessionUtil.getSession(jdbcFactory, true);
    }
}
