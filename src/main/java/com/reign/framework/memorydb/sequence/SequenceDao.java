package com.reign.framework.memorydb.sequence;

import com.reign.framework.jdbc.Params;
import com.reign.framework.jdbc.Type;
import com.reign.framework.jdbc.orm.BaseDao;

import java.util.List;

/**
 * @ClassName: SequenceDao
 * @Description: TODO 实现统一主键
 * @Author: wuwx
 * @Date: 2021-04-02 10:44
 **/
//@Component("sequenceDao")
public class SequenceDao extends BaseDao<Sequence, Integer> implements ISequenceDao {


    /**
     * 查询sequence
     */
    private static final String QUERY_SEQUENCE = "select * from sequence where table_name = ?";

    /**
     * 更新sequence
     */
    private static final String UPDATE_SEQUENCE = "update sequence set sequence = ? where table_name = ? and sequence = ?";


    public int nextId(String tableName) {
        Params params = new Params();
        params.addParam(tableName, Type.String);

        int rtn = -1;
        while (true) {
            try {
                //查询sequence
                List<Sequence> resultList = getResultByHQLAndParam(QUERY_SEQUENCE, params);
                Sequence sequence = null;
                if (resultList.size() > 0) {
                    sequence = resultList.get(0);
                }
                if (null == sequence) {
                    //不存在则创建
                    sequence = new Sequence();
                    sequence.setTableName(tableName);
                    sequence.setSequence(1);
                    rtn = 1;
                    create(sequence);
                    break;
                } else {
                    //存在则更新
                    rtn = sequence.getSequence() + 1;
                    Params updateParams = new Params();
                    updateParams.addParam(sequence.getSequence() + 1, Type.Int);
                    updateParams.addParam(tableName, Type.String);
                    updateParams.addParam(sequence.getSequence(), Type.Int);

                    if (update(UPDATE_SEQUENCE, updateParams, false) == 1) {
                        break;
                    }
                }
            } catch (Throwable throwable) {
                //忽略错误
            }
        }
        return rtn;
    }


    public Sequence getSequence(String tableName) {
        Params params = new Params();
        params.addParam(tableName, Type.String);
        //查询sequence
        List<Sequence> resultList = getResultByHQLAndParam(QUERY_SEQUENCE, params);
        Sequence sequence = null;
        if (resultList.size() > 0) {
            sequence = resultList.get(0);
        }
        return sequence;
    }


    public int maxId(String tableName) {
        Params params = new Params();
        params.addParam(tableName, Type.String);
        List<Sequence> resultList = getResultByHQLAndParam(QUERY_SEQUENCE, params);
        Sequence sequence = null;
        if (resultList.size()>0){
            sequence = resultList.get(0);
            return sequence.getSequence();
        }
        return 0;
    }
}
