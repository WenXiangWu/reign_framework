package com.reign.framework.log;

import com.reign.framework.jdbc.orm.transaction.Transaction;
import com.reign.framework.jdbc.orm.transaction.TransactionListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName: TransLoggerManager
 * @Description: 事务日志；   会在事务成功提交之后，将log内容写入到文件中，否则不写入
 * @Author: wuwx
 * @Date: 2021-04-10 16:56
 **/
public class TransLoggerManager implements TransactionListener {

    private static final TransLoggerManager instance = new TransLoggerManager();


    /**
     * 日志集合
     */
    private List<TransLogger> logList;


    public static TransLoggerManager getInstance() {
        return instance;
    }

    /**
     * 添加TransLogger
     *
     * @param log
     */
    public synchronized void addTransLogger(TransLogger log) {
        if (null == log) {
            logList = new CopyOnWriteArrayList<>();
        }
        logList.add(log);
    }


    @Override
    public void begin(Transaction transaction) {
        if (null == logList) return;
        for (TransLogger logger:logList){
            logger.startTrans();
        }

    }

    @Override
    public void beforeCommit(Transaction transaction, boolean succ) {
        //ignore
    }

    @Override
    public void commit(Transaction transaction, boolean succ) {
        if (null == logList) return;
        for (TransLogger logger:logList){
            logger.commitTrans(succ);
        }

    }
}
