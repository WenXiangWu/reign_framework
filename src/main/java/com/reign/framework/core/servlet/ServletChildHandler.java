package com.reign.framework.core.servlet;

import java.util.concurrent.ExecutorService;

/**
 * @ClassName: ServletChildHandler
 * @Description: ServletPipelineFactroy的PipelineFactory
 * @Author: wuwx
 * @Date: 2021-04-13 10:11
 **/
public interface ServletChildHandler {

    /**
     * 初始化PipelineFactory
     * @param servlet  servlet对象
     * @param context  全局信息
     * @param nettyConfig netty配置
     * @param executorService 执行线程池
     * @param useSession 是否使用session
     * @param isCluster 是否使用集群
     * @throws Exception
     */
    void init(Servlet servlet, ServletContext context, NettyConfig nettyConfig, ExecutorService executorService,boolean useSession,boolean isCluster) throws Exception;
}
