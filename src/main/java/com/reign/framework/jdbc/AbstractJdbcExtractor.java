package com.reign.framework.jdbc;

import com.reign.framework.jdbc.handlers.MapListHandler;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.LogLevel;
import com.reign.framework.log.LogMonitor;
import com.reign.framework.log.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: AbstractJdbcExtractor
 * @Description: 抽象执行器
 * @Author: wuwx
 * @Date: 2021-04-07 18:13
 **/
public abstract class AbstractJdbcExtractor implements JdbcExtractor, InitializingBean {

    private static final Logger log = InternalLoggerFactory.getLogger("com.reign.framework.jdbc");

    @Autowired
    private SqlFactory sqlFactory;

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    /**
     * 设置每次取的行数，就是每次结果集最多返回的行数
     */
    private int fetchSize;

    /**
     * 设置查询时最大返回的结果的数量，超对该数量的结果会被抛弃
     * 设置为0表示不限制
     */
    private int maxRows;

    /**
     * 设置batch执行时，最多一次执行的batch数量
     */
    private int batchSize = 50;

    /**
     * SQL解析器
     */
    private SqlBuilder sqlBuilder = new SqlBuilder();

    /**
     * sql错误解释器
     */
    private SQLExceptionTranslator translator = new SQLStateSQLExceptionTranslator();

    /**
     * mapListHandler
     */
    private static final MapListHandler MAP_LIST_HANDLER = new MapListHandler();

    /**
     * 查询最近插入的主键
     */
    private static final String SQL_SELECT_LAST_INSERT_ID = "SELECT LAST_INSERT_ID()";

    public AbstractJdbcExtractor(DataSource dataSource) throws Exception {
        this.dataSource = dataSource;
        afterPropertiesSet();
    }

    public AbstractJdbcExtractor() {
        //配置日志监控
        LogMonitor.getInstance().config(LogMonitor.MKEY_JDBCSQL, -1, LogLevel.INFO, 60 * 1000 * 10);
    }


    private synchronized void initSQLExceptionTranslator() {
        if (translator == null) {
            if (dataSource != null) {
                this.translator = new SQLErrorCodeSQLExceptionTranslator(dataSource);
            } else {
                this.translator = new SQLStateSQLExceptionTranslator();
            }

        }
    }

    public void afterPropertiesSet() throws Exception {
        initSQLExceptionTranslator();
    }

    /**
     * 从数据库连接池中获取连接
     *
     * @return
     * @throws SQLException
     */
    protected Connection getConnection() throws SQLException {
        return DataSourceUtils.getConnection(dataSource);
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public int getMaxRows() {
        return maxRows;
    }

    protected void releaseConnection(Connection connection) {
        DataSourceUtils.releaseConnection(connection, dataSource);
    }

    @Override
    public void batch(List<String> sqlList) {
        batch(sqlList, batchSize);
    }

    @Override
    public void batch(List<String> sqlList, int batchSize) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        //batch计数器
        int size = 0;

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            applyStatementSettings(stmt);
            for (String sql : sqlList) {
                //日志监控
                LogMonitor.getInstance().addMetric(LogMonitor.MKEY_JDBCSQL, LogMonitor.MKEY_JDBCSQL, sql, 1);
                stmt.addBatch(sql);
                size++;
                if (size == batchSize) {
                    doExecuteBatch(stmt, batchSize);
                    size = 0;
                }
            }
            if (size > 0) {
                doExecuteBatch(stmt, size);
            }

        } catch (SQLException e) {
            DataAccessException dat = translator.translate("execute batch sql error,msg::" + e.toString(), "", e);
            throw dat;
        } finally {
            DbUtils.closeQuietly(stmt, rs);
            releaseConnection(conn);
        }
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList) {
        batch(sql, paramList, batchSize);
    }

    @Override
    public void batch(String sql, List<List<Param>> paramList, int batchSize) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        //batch计数器
        int size = 0;
        sql = sqlFactory.get(sql);
        //打印日志
        if (log.isDebugEnabled()) {
            log.debug("SQL:" + sql);
        }
        //日志监控
        LogMonitor.getInstance().addMetric(LogMonitor.MKEY_JDBCSQL, LogMonitor.MKEY_JDBCSQL, sql, 1);

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            applyStatementSettings(pstmt);

            for (List<Param> params : paramList) {
                //填充参数
                sqlBuilder.buildParameters(pstmt, params);
                pstmt.addBatch();
                size++;
                if (size == batchSize) {
                    doExecuteBatch(pstmt, batchSize);
                    size = 0;
                }
            }
            if (size > 0) {
                doExecuteBatch(pstmt, size);
            }
        } catch (SQLException e) {
            DataAccessException dat = translator.translate("execute sql:" + sql + "error,msg:" + e.toString(), sql, e);
            throw dat;
        } finally {
            DbUtils.closeQuietly(pstmt, rs);
            releaseConnection(conn);
        }
    }

    @Override
    public int insert(String sql, List<Param> params, boolean autoGenerator) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        int result = 0;
        sql = sqlFactory.get(sql);
        //打印日志
        if (log.isDebugEnabled()) {
            log.debug("SQL:" + sql);
        }
        //日志监控
        LogMonitor.getInstance().addMetric(LogMonitor.MKEY_JDBCSQL, LogMonitor.MKEY_JDBCSQL, sql, 1);

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            applyStatementSettings(pstmt);
            //填充参数
            sqlBuilder.buildParameters(pstmt, params);
            result = pstmt.executeUpdate();
            if (result != 0 && autoGenerator) {
                pstmt.close();
                pstmt = conn.prepareStatement(SQL_SELECT_LAST_INSERT_ID);
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return result;
                }
            }

            return result;
        } catch (SQLException e) {
            DataAccessException dat = translator.translate("execute sql:" + sql + "error,msg:" + e.toString(), sql, e);
            throw dat;
        } finally {
            DbUtils.closeQuietly(pstmt, rs);
            releaseConnection(conn);
        }

    }

    @Override
    public List<Map<String, Object>> query(String sql, List<Param> params) {
        return query(sql, params, MAP_LIST_HANDLER);
    }

    @Override
    public <T> T query(String sql, List<Param> params, ResultSetHandler<T> handler) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        sql = sqlFactory.get(sql);

        //打印日志
        if (log.isDebugEnabled()) {
            log.debug("SQL:" + sql);
        }
        //日志监控
        LogMonitor.getInstance().addMetric(LogMonitor.MKEY_JDBCSQL, LogMonitor.MKEY_JDBCSQL, sql, 1);

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            applyStatementSettings(pstmt);
            //填充参数
            sqlBuilder.buildParameters(pstmt, params);
            rs = pstmt.executeQuery();
            return handler.handler(rs);
        } catch (SQLException e) {
            DataAccessException dat = translator.translate("execute sql:" + sql + "error,msg:" + e.toString(), sql, e);
            throw dat;
        } finally {
            DbUtils.closeQuietly(pstmt, rs);
            releaseConnection(conn);
        }
    }

    @Override
    public int update(String sql, List<Param> params) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Connection conn = null;
        int result = 0;
        sql = sqlFactory.get(sql);
        //打印日志
        if (log.isDebugEnabled()) {
            log.debug("SQL:" + sql);
        }
        //日志监控
        LogMonitor.getInstance().addMetric(LogMonitor.MKEY_JDBCSQL, LogMonitor.MKEY_JDBCSQL, sql, 1);

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(sql);
            applyStatementSettings(pstmt);
            //填充参数
            sqlBuilder.buildParameters(pstmt, params);
            result = pstmt.executeUpdate();
            return result;
        } catch (SQLException e) {
            DataAccessException dat = translator.translate("execute sql:" + sql + "error,msg:" + e.toString(), sql, e);
            throw dat;
        } finally {
            DbUtils.closeQuietly(pstmt, rs);
            releaseConnection(conn);
        }
    }


    /**
     * 设置jdbc参数
     *
     * @param stme
     * @throws SQLException
     */
    protected void applyStatementSettings(Statement stme) throws SQLException {
        if (this.fetchSize > 0) {
            stme.setFetchSize(this.fetchSize);
        }
        if (this.maxRows > 0) {
            stme.setMaxFieldSize(this.maxRows);
        }
    }

    /**
     * 执行batch操作
     * @param stme
     * @param batchSize
     * @throws SQLException
     */
    private void doExecuteBatch(Statement stme, int batchSize) throws SQLException {
        int[] result = stme.executeBatch();
        //检查batch执行结果
        if (batchSize!=result.length){
            throw new RuntimeException("batch error,excepted batch result len:"+batchSize+",bat jdbc return len:"+result.length);
        }

        /**
         * 检查batch结果
         * -2：表示执行成功，但是影响行数未知
         * -3:表示执行失败，但是驱动在执行错误之后继续执行了后续batch命令
         * 0 or >0 返回影响的行数
         */
        for (int i:result){
            if (i==-3){
                throw new RuntimeException("batch update failed:"+i);
            }
        }

    }
}
