package com.reign.framework.core.mvc.result;

/**
 * @ClassName: ByteResult
 * @Description: 字节结果
 * @Author: wuwx
 * @Date: 2021-04-19 17:51
 **/
public class ByteResult implements Result<byte[]> {

    private byte[] result;

    public ByteResult(byte[] result) {
        this.result = result;
    }

    @Override
    public String getViewName() {
        return "byte";
    }

    @Override
    public byte[] getResult() {
        return result;
    }

    @Override
    public int getBytesLength() {
        return result.length;
    }
}
