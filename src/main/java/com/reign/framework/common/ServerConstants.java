package com.reign.framework.common;

/**
 * @ClassName: ServerConstants
 * @Description: 服务器常量
 * @Author: wuwx
 * @Date: 2021-04-14 18:15
 **/
public class ServerConstants {

    /**
     * crossDomain文件体
     */
    public static final byte[] CROSSDOMAIN = "<cross-domain-policy><allow-access-from domain=\"*\" to-ports=\"*\" /><cross-domain-policy>\u0000".getBytes();

    /**
     * 长连接命令
     */
    public static final String LONG_HTTP = "longhttp";


    public static final String CONTENT_TYPE_COMPRESSED = "application/x-gzip-compressed";

    public static final String CONTENT_TYPE = "application/json";

    public static final String JESSIONID ="REIGNID";

    /**
     * 参数中command命令
     */
    public static final String COMMAND = "command";

    /***
     * 策略文件请求头
     */
    public static final String POLICY_FILE_REQUEST = "<policy-file-request/>\0";
}
