package com.reign.framework.core.servlet;

/**
 * @ClassName: ServletPipelineFactory
 * @Description:
 * @Author: wuwx
 * @Date: 2021-04-15 10:04
 **/
public interface ServletPipelineFactory {

    /**
     * 初始化Pipeline factory
     * @param servlet
     * @param servletContext
     * @param config
     * @param useSession
     * @param isCluster
     * @throws Exception
     */
    void init(Servlet servlet,ServletContext servletContext,NettyConfig config,boolean useSession,boolean isCluster) throws Exception;
}
