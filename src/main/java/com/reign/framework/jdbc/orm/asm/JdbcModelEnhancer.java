package com.reign.framework.jdbc.orm.asm;

import com.reign.framework.common.util.Tuple;
import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.orm.IDynamicUpdate;
import com.reign.framework.jdbc.orm.JdbcModel;
import com.reign.framework.log.InternalLoggerFactory;
import com.reign.framework.log.Logger;
import java.io.InputStream;
import java.util.List;

import com.reign.framework.memorydb.sequence.Sequence;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

/**
 * @ClassName: JdbcModelEnhancer
 * @Description: JdbcModel增强器
 * @Author: wuwx
 * @Date: 2021-04-08 15:47
 **/
public class JdbcModelEnhancer {

//    private static final Logger log = InternalLoggerFactory.getLogger(JdbcModelEnhancer.class);

    private static ASMClassLoader loader = null;

    public static Class<?> enhance(Class<?> clazz) {
        if (JdbcModel.class.isAssignableFrom(clazz)) {
            //初始化classLoader
            ClassLoader threadLoader = Thread.currentThread().getContextClassLoader();
            if (null == loader) {
                loader = new ASMClassLoader(threadLoader);
            }

            //缓存中查找
            Class<?> temp;
            try {
                temp = loader.loadClass(clazz.getName() + "$EnhanceByASM");
                if (null != temp) {
                    return temp;
                }
            }catch (ClassNotFoundException e){

            }

            //生成增强类
            try{
                String path = clazz.getName().replace(".","/")+".class";
                InputStream is = threadLoader.getResourceAsStream(path);
                ClassReader cr = new ClassReader(is);
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                ASMClassAdapter adapter = new ASMClassAdapter(cw,clazz);
                cr.accept(adapter,ClassReader.SKIP_DEBUG);
                byte[] date = cw.toByteArray();

                Class<?> enhanceClazz = loader.loadClassFromBytes(clazz.getName()+"$EnhanceByASM",date);
              //  log.info("enhance class {} success",clazz.getName());
                return enhanceClazz;

            }catch (Throwable t){
             //   log.error("enhance class {} failed",t,clazz.getName());
            }

        }
        return clazz;
    }

    public static void main(String[] args) throws IllegalAccessException, InstantiationException {
        Class<?> clazz = enhance(Sequence.class);
        Sequence s = (Sequence) clazz.newInstance();
        s.setId(50);
        s.setSequence(12);
        s.setTableName("player");

        Sequence s1= new Sequence();
        s1.setId(111);
        s1.setSequence(123);
        s1.setTableName("player");

        IDynamicUpdate update = (IDynamicUpdate)s;
        Tuple<String, List<Param>> rtn = update.dynamicUpdateSQL("sequence",s1);
        System.out.println(rtn);

    }

}
