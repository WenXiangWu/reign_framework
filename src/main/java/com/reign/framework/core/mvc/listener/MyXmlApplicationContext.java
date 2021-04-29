package com.reign.framework.core.mvc.listener;

import com.reign.framework.core.servlet.ServletContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.ResourceEntityResolver;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.AbstractRefreshableConfigApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.annotation.Resource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @ClassName: MyXmlApplicationContext
 * @Description: 自定义ApplicationContext，用于自动生成一份组件配置文件，用于spring加载
 * @Author: wuwx
 * @Date: 2021-04-19 18:16
 **/
public class MyXmlApplicationContext extends AbstractRefreshableConfigApplicationContext {

    //组件扫描路径
    private static final String COMPONENT_SCAN_PACKAGE = "componentScanPackage";

    private ServletContext servletContext;

    public MyXmlApplicationContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException {
        XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.setResourceLoader(this);
        beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

        loadBeanDefinitions(beanDefinitionReader);
        initBeanDefinitionReader(beanDefinitionReader);

    }

    private void initBeanDefinitionReader(XmlBeanDefinitionReader beanDefinitionReader) {
        beanDefinitionReader.loadBeanDefinitions(new ByteArrayResource(generateComponentXML()));
    }

    @Override
    protected String[] getConfigLocations() {
        return new String[]{"classpath*:applicationContext.xml"};
    }

    private byte[] generateComponentXML() {
        try {
            String pkg = (String) servletContext.getInitParam(COMPONENT_SCAN_PACKAGE);
            //查到空的component.xml,用于append内容
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getClass().getClassLoader().getResourceAsStream("component.xml"));
            //获取此路径下的所有类
            Map<String, String> resourcesMap = scanClasses(pkg);
            //动态append
            for (Map.Entry<String, String> entry : resourcesMap.entrySet()) {
                Element e = doc.createElement("bean");
                e.setAttribute("id", entry.getKey());
                e.setAttribute("class", entry.getValue());
                doc.getDocumentElement().appendChild(e);
            }

            //转换成字节输出
            DOMSource ds = new DOMSource(doc);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            StringWriter writer = new StringWriter();
            Result result = new StreamResult(writer);
            transformer.transform(ds, result);
            return writer.toString().getBytes();
        } catch (Exception e) {
            logger.error("generateComponentXML error", e);
        }
        return null;
    }


    /**
     * 扫描包，会扫描指定包路径下的所有类
     *
     * @param pkg
     * @return
     */
    private Map<String, String> scanClasses(String pkg) {
        Map<String, String> resourcesMap = new LinkedHashMap<>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(pkg);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    put(resourcesMap, url.getFile());
                }
            }
        } catch (IOException e) {
            logger.debug("scanClasses error ", e);
        } catch (ClassNotFoundException e) {
            logger.debug("scanClasses error ", e);
            e.printStackTrace();
        }
        return resourcesMap;
    }

    private void put(Map<String, String> resourcesMap, String className) throws ClassNotFoundException {
        try {
            if (StringUtils.isNotBlank(className) && className.indexOf("$") == -1) {
                //内部类会跳过
                String simpleName = StringUtils.substringAfterLast(className, ".");
                Class<?> clazz = getClassLoader().loadClass(className);

                //默认bean名称
                simpleName = WordUtils.uncapitalize(simpleName);

                //判断是否是Component定义
                Component component = clazz.getAnnotation(Component.class);
                if (null != component) {
                    simpleName = StringUtils.isBlank(component.value()) ? simpleName : component.value();
                    resourcesMap.put(simpleName, className);
                    return;
                }

                //判断是否是Resource定义； 这里是java.annotation.Resource
                Resource resource = clazz.getAnnotation(Resource.class);
                if (resource != null) {
                    simpleName = StringUtils.isBlank(resource.name()) ? simpleName : resource.name();
                    resourcesMap.put(simpleName, className);
                    return;
                }
                //判断是否是Service定义
                Service service = clazz.getAnnotation(Service.class);
                if (service != null) {
                    simpleName = StringUtils.isBlank(service.value()) ? simpleName : service.value();
                    resourcesMap.put(simpleName, className);
                    return;
                }

                //判断是否是Repository
                Repository repository = clazz.getAnnotation(Repository.class);
                if (repository != null) {
                    simpleName = StringUtils.isBlank(repository.value()) ? simpleName : repository.value();
                    resourcesMap.put(simpleName, className);
                    return;
                }
            }
        } catch (Throwable t) {
            logger.debug("load " + className + " error ", t);
        }

    }

    private void loadBeanDefinitions(XmlBeanDefinitionReader reader) {
        String[] configLocations = getConfigLocations();
        if (configLocations != null) {
            for (String configLocation : configLocations) {
                reader.loadBeanDefinitions(configLocation);
            }
        }
    }
}
