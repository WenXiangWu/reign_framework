package com.reign.framework.core.servlet;

import com.reign.framework.core.exception.SevletConfigException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: XmlConfig
 * @Description: xml解析器
 * @Author: wuwx
 * @Date: 2021-04-15 10:05
 **/
public class XmlConfig {

    private String servletName;

    private Class<? extends Servlet> servletClass;

    private Map<String, Object> servletParamsMap = new HashMap<>();

    private Map<String, Object> nettyParamsMap = new HashMap<>();


    private Map<String, Object> nettyTcpParamsMap = new HashMap<>();

    private List<Class<?>> listenerList = new ArrayList<>();

    public XmlConfig(String path) {
        parse(path);
    }

    public ServletConfig getServletConfig() {
        return new ServletConfig() {
            private long sessionTimeoutMillis = -1L;

            private long sessionEmpytTimeoutMillis = -1L;

            private long sessionInvalidateMillis = -1L;

            private long sessionNextDayInvalidateMillis = -1L;

            private int sessionTickTime = -1;

            @Override
            public String getServletName() {
                return servletName;
            }

            @Override
            public Class<? extends Servlet> getServletClass() {
                return servletClass;
            }

            @Override
            public List<Class<?>> getListeners() {
                return listenerList;
            }

            @Override
            public Object getInitParam(String paramName) {
                return servletParamsMap.get(paramName);
            }

            @Override
            public Map<String, Object> getInitParams() {
                return servletParamsMap;
            }

            @Override
            public long getSessionTimeoutMillis() {
                if (sessionTimeoutMillis != -1) {
                    return sessionTimeoutMillis;
                }
                //如果不配置，session默认3分钟超时
                Integer minutes = (Integer) getInitParam("sessionTimeOut");
                sessionTimeoutMillis = (null == minutes ? 3 * 60 * 1000 : minutes * 60 * 1000);
                return sessionTimeoutMillis;
            }

            @Override
            public long getSessionInvalidateMillis() {
                if (sessionInvalidateMillis != -1) {
                    return sessionInvalidateMillis;
                }
                //如果不配置,默认24小时
                Integer minutes = (Integer) getInitParam("sessionInvalidate");
                sessionInvalidateMillis = (null == minutes ? 24 * 3600000 * 1000 : minutes * 60 * 1000);
                return sessionInvalidateMillis;
            }

            @Override
            public long getSessionNextDayInvalidateMillis() {
                if (sessionNextDayInvalidateMillis != -1) {
                    return sessionNextDayInvalidateMillis;
                }
                //如果不配置,默认30分钟
                Integer minutes = (Integer) getInitParam("sessionNextDay");
                sessionNextDayInvalidateMillis = (null == minutes ? 30 * 60 * 1000 : minutes * 60 * 1000);
                return sessionNextDayInvalidateMillis;
            }

            @Override
            public int getSessionTickTime() {
                if (sessionTickTime != -1) {
                    return sessionTickTime;
                }
                //如果不配置，默认25s
                Integer seconds = (Integer) getInitParam("sessionTickTime");
                sessionTickTime = (null == seconds ? 25 : seconds);
                return sessionTickTime;
            }

            @Override
            public long getSessionEmptyTimeOutMillis() {
                if (sessionEmpytTimeoutMillis != -1) {
                    return sessionEmpytTimeoutMillis;
                }
                //如果不配置,默认10秒
                Integer secs = (Integer) getInitParam("sessionEmptyTimeOut");
                sessionEmpytTimeoutMillis = (null == secs ? 10000 : secs * 1000);
                return sessionEmpytTimeoutMillis;
            }
        };
    }

    public NettyConfig getNettyConfig() {
        return new NettyConfig() {
            @Override
            public Object getInitParam(String paramName) {
                return nettyParamsMap.get(paramName);
            }

            @Override
            public Map<String, Object> getInitParams() {
                return nettyParamsMap;
            }

            @Override
            public Map<String, Object> getTcpParams() {
                return nettyTcpParamsMap;
            }
        };
    }

    private void parse(String path) {
        if (StringUtils.isBlank(path)) {
            throw new SevletConfigException("cannot parse servlet config,path must not be null");
        }
        DocumentBuilder db;

        try {
            //初始化工厂
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(false);
            dbf.setNamespaceAware(false);

            db = dbf.newDocumentBuilder();
            db.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    throw exception;
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    throw exception;
                }
            });

            //解析文件
            Document doc = db.parse(XmlConfig.class.getClassLoader().getResourceAsStream(path));
            //解析netty配置
            Element nettyElement = (Element) doc.getElementsByTagName("netty").item(0);
            if (null != nettyElement) {
                parsePropertyNode(nettyElement, "init-param", "props", "property", this.nettyParamsMap);
                parsePropertyNode(nettyElement, "tcp-param", "props", "property", this.nettyTcpParamsMap);
            }

            Element servletElement = (Element) doc.getElementsByTagName("servlet").item(0);
            if (null == servletElement) {
                throw new SevletConfigException("cannot parse servlet config,cannot found [servlet] element");
            }
            //解析参数配置
            parsePropertyNode(servletElement, "init-param", "props", "property", this.servletParamsMap);

            //获取servlet-name
            Element servletNameElement = (Element) servletElement.getElementsByTagName("servlet-name").item(0);
            if (null == servletNameElement) {
                throw new SevletConfigException("cannot parse servlet config,cannot found [servlet-name] element");
            }
            this.servletName = servletNameElement.getTextContent();

            //获取servlet-class
            Element servletClassElement = (Element) servletElement.getElementsByTagName("servlet-class").item(0);
            if (servletClassElement == null) {
                throw new SevletConfigException("cannot parse servlet config,cannot found [servlet-class] element");
            }

            this.servletClass = (Class<? extends Servlet>) Class.forName(servletClassElement.getTextContent());

            //获取listener-class
            NodeList nodeList = doc.getElementsByTagName("listener-class");
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node instanceof Element) {
                    Element element = (Element) node;
                    listenerList.add(Class.forName(element.getTextContent()));
                }
            }

        } catch (Exception e) {
            throw new SevletConfigException("cannot parse servlet config,have a exception");
        }

    }

    private void parsePropertyNode(Element rootElement, String rootName, String secondName, String nodeName, Map<String, Object> map) {
        //解析配置参数
        Element initParamElement = (Element) rootElement.getElementsByTagName(rootName).item(0);
        if (null != initParamElement) {
            NodeList children = initParamElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node childNode = children.item(i);
                if (childNode instanceof Element) {
                    Element element = (Element) childNode;
                    String elementName = element.getNodeName();
                    if (secondName.equalsIgnoreCase(elementName)) {
                        NodeList propsList = element.getChildNodes();
                        for (int j = 0; i < propsList.getLength(); j++) {
                            Node propsNode = propsList.item(j);
                            if (propsNode instanceof Element) {
                                Element propsElement = (Element) propsNode;
                                String propsElementName = propsElement.getNodeName();
                                if (nodeName.equalsIgnoreCase(propsElementName)) {
                                    String name = propsElement.getAttribute("name");
                                    String type = propsElement.getAttribute("type");
                                    Object value = parseValue(type, propsElement.getTextContent());
                                    map.put(name, value);
                                }
                            }
                        }
                    }

                }

            }

        }
    }


    private Object parseValue(String type, String value) {
        if ("int".equalsIgnoreCase(type)) {
            return Integer.valueOf(value);
        } else if ("string".equalsIgnoreCase(type)) {
            return value;
        } else if ("boolean".equalsIgnoreCase(type)) {
            return Boolean.valueOf(value);
        }
        return value;

    }
}
