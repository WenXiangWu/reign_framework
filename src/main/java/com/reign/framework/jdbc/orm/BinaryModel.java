package com.reign.framework.jdbc.orm;

/**
 * @ClassName: BinaryModel
 * @Description: 使用支持使用二进制序列化和反序列化
 * @Author: wuwx
 * @Date: 2021-04-08 18:08
 **/
public interface BinaryModel {


    /**
     * 序列化为二进制
     * @return
     */
    byte[] toByte();
}
