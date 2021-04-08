package com.reign.framework.jdbc.sql;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: Sqls
 * @Description: schema解析结果
 * @Author: wuwx
 * @Date: 2021-04-07 17:53
 **/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sqls", propOrder = {"sql"})
public class Sqls {

    protected List<Sql> sql;


    public List<Sql> getSql() {
        if (sql == null) {
            sql = new ArrayList<>();
        }
        return this.sql;
    }


}
