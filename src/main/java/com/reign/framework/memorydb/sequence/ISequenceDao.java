package com.reign.framework.memorydb.sequence;

import com.reign.framework.jdbc.orm.IBaseDao;

/**
 * @ClassName: ISequenceDao
 * @Description: 用于实现统一主键
 * @Author: wuwx
 * @Date: 2021-04-02 10:44
 **/
public interface ISequenceDao extends IBaseDao<Sequence,Integer> {


    /**
     * 获取下一个可用id
     * @param tableName
     * @return
     */
    int nextId(String tableName);


    /**
     * 获取sequence
     * @param tableName
     * @return
     */
    Sequence getSequence(String tableName);

    /**
     * 获取maxId
     * @param tableName
     * @return
     */
    int maxId(String tableName);
}
