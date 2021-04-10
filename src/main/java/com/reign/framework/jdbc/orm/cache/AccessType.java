package com.reign.framework.jdbc.orm.cache;


/**
 * @ClassName: AccessType
 * @Description: cache类型
 * @Author: wuwx
 * @Date: 2021-04-09 17:45
 **/
public enum AccessType {
    READ_WRITE("read-write");

    private String value;

    AccessType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public AccessType getAccessType(String value) {
        if (value.equalsIgnoreCase("read-write")) {
            return READ_WRITE;
        }
        return READ_WRITE;
    }

}
