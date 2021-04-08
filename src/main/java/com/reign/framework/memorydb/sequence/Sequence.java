package com.reign.framework.memorydb.sequence;

import com.reign.framework.jdbc.orm.annotation.Cache;
import com.reign.framework.jdbc.orm.annotation.Id;
import com.reign.framework.jdbc.orm.annotation.JdbcEntity;
import com.reign.framework.memorydb.AbstractDomain;
import com.reign.framework.memorydb.annotation.AutoId;
import com.reign.framework.memorydb.annotation.BTreeIndex;
import com.reign.framework.memorydb.annotation.BTreeIndexs;

/**
 * @ClassName: Sequence
 * @Description: sequence表的pojo
 * @Author: wuwx
 * @Date: 2021-04-02 10:44
 **/

@JdbcEntity
@Cache(disable = true)
@AutoId
@BTreeIndexs({
        @BTreeIndex(name = "tablename", value = {"tableName"}, unique = true)
})
public class Sequence extends AbstractDomain {


    private static final long serialVersionUID = 1L;


    @Id
    private int id;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 对应table的sequence
     */
    private int sequence;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        super.mark();
        this.tableName = tableName;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    @Override
    public Object clone() {
        Sequence obj = new Sequence();
        obj.id = this.id;
        obj.tableName = this.tableName;
        obj.sequence = this.sequence;
        return obj;
    }
}
