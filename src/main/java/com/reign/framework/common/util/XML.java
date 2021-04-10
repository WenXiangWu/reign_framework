package com.reign.framework.common.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: XML
 * @Description: xml解析
 * @Author: wuwx
 * @Date: 2021-04-10 17:32
 **/
public class XML {

    private Document doc;

    private Element root;

    public XML(String path) {
        try {
            this.init(new FileInputStream(path));
        } catch (Exception e) {
            throw new RuntimeException("init xml error", e);
        }
    }


    public XML(InputStream is) {
        try  {
            this.init(is);
        } catch (Exception e) {
            throw new RuntimeException("init xml error", e);
        }
    }


    public XMLNode get(String tagName) {
        NodeList nodeList = this.root.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == 1) {
                String nodeName = node.getNodeName();
                if (nodeName.equalsIgnoreCase(tagName)) {
                    return new XMLNode((Element) node);
                }
            }
        }
        return null;
    }

    public List<XMLNode> getList(String tagName) {
        NodeList nodeList = this.root.getChildNodes();
        List<XMLNode> resultList = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == 1) {
                String nodeName = node.getNodeName();
                if (nodeName.equalsIgnoreCase(tagName)) {
                    resultList.add(new XMLNode((Element) node));
                }
            }
        }
        return resultList;
    }




    private void init(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        this.doc = builder.parse(is);
        this.root = this.doc.getDocumentElement();
    }


    public class XMLNode {
        private Element element;

        public XMLNode(Element element) {
            this.element = element;
        }

        public String getValue() {
            return this.element.getTextContent();
        }

        public String getAttribute(String attr) {
            return this.element.getAttribute(attr);
        }

        public XMLNode get(String tagName) {
            NodeList nodeList = this.element.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == 1) {
                    String nodeName = node.getNodeName();
                    if (nodeName.equalsIgnoreCase(tagName)) {
                        return XML.this.new XMLNode((Element) node);
                    }
                }
            }
            return null;
        }

        public List<XMLNode> getList(String tagName) {
            NodeList nodeList = this.element.getChildNodes();
            List<XMLNode> resultList = new ArrayList<>();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == 1) {
                    String nodeName = node.getNodeName();
                    if (nodeName.equalsIgnoreCase(tagName)) {
                        resultList.add(XML.this.new XMLNode((Element) node));
                    }
                }
            }
            return resultList;
        }

    }

}
