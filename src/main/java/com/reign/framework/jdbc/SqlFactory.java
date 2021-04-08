package com.reign.framework.jdbc;

import com.reign.framework.jdbc.sql.Sql;
import com.reign.framework.jdbc.sql.Sqls;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @ClassName: SqlFactory
 * @Description: sql工厂
 * @Author: wuwx
 * @Date: 2021-04-02 10:22
 **/
public class SqlFactory implements InitializingBean {

    protected static Log log = LogFactory.getLog(SqlFactory.class);

    //存储ID -SQL
    protected Map<String, String> dic = new HashMap<>();

    //配置文件资源，sql.xml
    protected Resource[] resources;

    //schema 资源
    protected Resource schemaResource;

    public Resource[] getResources() {
        return resources;
    }

    public void setResources(Resource[] resources) {
        this.resources = resources;
    }

    public Resource getSchemaResource() {
        return schemaResource;
    }

    public void setSchemaResource(Resource schemaResource) {
        this.schemaResource = schemaResource;
    }

    /**
     * 返回指定SQL语句
     *
     * @param key
     * @return
     */
    public String get(String key) {
        String rtn = dic.get(key);
        return (null == rtn) ? key : rtn.trim();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<Resource> resList = new ArrayList<>();
        //获取所有资源
        for (Resource res : resources) {
            try {
                if (res.getFilename().endsWith(".jar")) {
                    JarInputStream jar = new JarInputStream(res.getInputStream());
                    JarEntry entry = jar.getNextJarEntry();
                    while (null != entry) {
                        if (entry.getName().endsWith("sql.xml")) {
                            resList.add(new InputStreamResource(getResource(jar)));
                        }
                        entry = jar.getNextJarEntry();
                    }
                    jar.close();
                } else {
                    getResources(resList, res.getFile());
                }
            } catch (IOException e) {

            }

        }

        if (resList.size() == 0) {
            throw new FileNotFoundException("not found sql file");
        }
        //开始解析SQL.xml文件
        JAXBContext jaxbContext = JAXBContext.newInstance("com.reign.framework.jdbc.sql");
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        Schema schema = factory.newSchema(new StreamSource(schemaResource.getInputStream()));
        unmarshaller.setSchema(schema);

        for (Resource res : resList) {
            JAXBElement<Sqls> element = (JAXBElement<Sqls>) unmarshaller.unmarshal(res.getInputStream());
            Sqls sqls = element.getValue();
            for (Sql sql : sqls.getSql()) {
                if (dic.containsKey(sql.getId())) {
                    throw new RuntimeException("定义了重复的SQL:" + sql.getId());
                } else {
                    dic.put(sql.getId(), sql.getValue());
                    log.info("SQL语句加载完毕" + sql.getId());
                    System.out.println("SQL语句加载完毕" + sql.getId());
                }
            }
        }

    }

    /**
     * 获取所有资源
     *
     * @param resources
     * @param file
     */
    private void getResources(List<Resource> resources, File file) {
        if (file.isFile()) {
            //文件
            resources.add(new FileSystemResource(file));
        } else {
            //目录
            File[] files = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (file.isDirectory()) {
                        return true;
                    } else if (file.getName().equals("sql.xml")) {
                        return true;
                    }
                    return false;
                }
            });
            for (File f : files) {
                getResources(resources, f);
            }
        }
    }

    /**
     * 从jar中读取资源
     *
     * @param jar
     * @return
     */
    private InputStream getResource(JarInputStream jar) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = -1;
        while (jar.available() > 0) {
            len = jar.read(buff);
            if (len > 0) {
                bos.write(buff, 0, len);
            }
        }
        return new ByteArrayInputStream(bos.toByteArray());

    }
}
