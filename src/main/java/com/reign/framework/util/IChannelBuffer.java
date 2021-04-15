package com.reign.framework.util;

import java.nio.Buffer;

/**
 * @ClassName: IChannelBuffer
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 13:23
 **/
public interface IChannelBuffer  {

    void writeString(String id);

    byte[] array();
}
