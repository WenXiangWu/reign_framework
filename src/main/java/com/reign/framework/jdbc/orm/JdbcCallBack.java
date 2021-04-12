package com.reign.framework.jdbc.orm;

import com.reign.framework.jdbc.orm.session.JdbcSession;

/**
 * @ClassName: JdbcCallBack
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-02 14:51
 **/
public interface JdbcCallBack<T> {

    /**
     * 回调函数
     * @param session
     * @return
     */
    T doInJdbcSession(JdbcSession session);
}
