package com.reign.framework.common;

import java.io.File;

/**
 * @ClassName: ListenerConstants
 * @Description: listener的常数
 * @Author: wuwx
 * @Date: 2021-04-15 12:24
 **/
public class ListenerConstants {
    /**
     * session中保存Player信息关键字
     */
    public static final String PLAYER = "PLAYER";
    /**
     * Session中保存User信息关键字
     */
    public static final String USER = "USER";
    /**
     * properties文件夹名
     */
    public static final String PROPERTIES = "properties";

    /**
     * 加载properties的路径
     */
    public static final String WEB_PATH = System.getProperty("user.dir") + File.separator + PROPERTIES + File.separator;

    /**
     * 静态库地址
     */
    public static String SDATA_URL;
    /**
     * 是否使用sequence
     */
    public static boolean useSequece =false;


}
