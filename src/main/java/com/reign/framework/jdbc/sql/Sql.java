package com.reign.framework.jdbc.sql;

import javax.xml.bind.annotation.*;

/**
 * @ClassName: Sql
 * @Description: schema解析出的SQL语句
 * @Author: wuwx
 * @Date: 2021-04-07 17:54
 **/
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "sql", propOrder = {"value"})
public class Sql {

    @XmlValue
    protected String value;

    @XmlAttribute
    protected String id;


    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String value) {
        this.id = value;
    }
}
