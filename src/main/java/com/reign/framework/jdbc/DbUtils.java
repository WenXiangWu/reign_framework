package com.reign.framework.jdbc;

import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @ClassName: DbUtils
 * @Description: db工具类
 * @Author: wuwx
 * @Date: 2021-04-07 18:13
 **/
public class DbUtils {

    private static final Logger log = InternalLoggerFactory.getLogger(DbUtils.class);

    public static void closeQuietly(Statement pstmt, ResultSet rs) {

        try {
            closeQuietly(rs);
        } finally {
            closeQuietly(pstmt);
        }

    }

    private static void closeQuietly(Statement pstmt) {
        try {
            close(pstmt);
        } catch (SQLException e) {
            log.error("Could not close jdbc Statement", e);
        } catch (Throwable e) {
            log.error("Unexpected exception on closing jdbc Statement");
        }

    }

    /**
     * 关闭statement
     *
     * @param pstmt
     */
    private static void close(Statement pstmt) throws SQLException {
        if (pstmt != null) {
            pstmt.close();
        }
    }

    private static void closeQuietly(ResultSet rs) {
        try {
            close(rs);
        } catch (SQLException e) {
            log.error("Could not close jdbc Statement", e);
        } catch (Throwable e) {
            log.error("Unexpected exception on closing jdbc Statement");
        }
    }

    /**
     * 关闭rs
     *
     * @param rs
     */
    private static void close(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }
}
