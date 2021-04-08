package com.reign.framework.jdbc.orm.asm;

import com.reign.framework.common.util.Tuple;
import com.reign.framework.jdbc.DefaultNameStrategy;
import com.reign.framework.jdbc.NameStrategy;
import com.reign.framework.jdbc.Param;
import com.reign.framework.jdbc.orm.IDynamicUpdate;
import com.reign.framework.jdbc.orm.JdbcModel;
import com.reign.framework.jdbc.orm.asm.util.ASMUtil;
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @ClassName: ASMClassAdapter
 * @Description: ASM类适配器，用于修改类
 * @Author: wuwx
 * @Date: 2021-04-08 15:16
 **/
public class ASMClassAdapter extends ClassAdapter {

    /**
     * 修改类的父类的名字
     */
    private String enhanceSuperName;

    /**
     * 修改类的名字
     */
    private String enhanceName;

    /**
     * 父类
     */
    private Class<?> clazz;

    /**
     * 命名策略
     */
    private NameStrategy strategy = new DefaultNameStrategy();

    /**
     * 所有get方法的签名信息
     */
    public Set<String[]> fieldSet = new LinkedHashSet<>();

    /**
     * 构造函数
     *
     * @param classVisitor
     * @param clazz
     */
    public ASMClassAdapter(ClassVisitor classVisitor, Class<?> clazz) {
        super(classVisitor);
        this.clazz = clazz;
    }


    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        //改变类名
        this.enhanceName = name + "$EnhanceByASM";
        //存储父类的名字
        this.enhanceSuperName = name;

        //修改类使其继承DynamicUpdate
        super.visit(version, access, enhanceName, signature, enhanceSuperName, new String[]{ASMUtil.getClassName(IDynamicUpdate.class)});
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("<init>")) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            MethodVisitor wrapMv = mv;
            //构造函数
            wrapMv = new ASMClassConstructorMethodAdapter(mv, enhanceSuperName);
            return wrapMv;
        } else if (name.startsWith("get")) {
            //添加方法的描述
            fieldSet.add(new String[]{enhanceName, name, desc, getFieldName(name), getFieldType(desc)});
            return null;
        } else if (name.startsWith("set")) {
            return null;
        } else if (name.startsWith("clone")) {
            return null;
        } else {
            return null;
        }
    }

    /**
     * 获取field的类型
     *
     * @param desc
     * @return
     */
    private String getFieldType(String desc) {
        if (desc.indexOf("L") != -1) {
            return desc.substring(desc.indexOf("L") + 1, desc.length() - 1);
        }
        return desc;
    }

    /**
     * 获取field的名称
     *
     * @param name
     * @return
     */
    private String getFieldName(String name) {
        if (name.startsWith("get")) {
            return name.substring(3);
        } else if (name.startsWith("is")) {
            return name.substring(2);
        }
        return name;
    }


    @Override
    public FieldVisitor visitField(int access, String owner, String desc, String signature, Object obj) {
        if (access == Opcodes.ACC_STATIC) {
            return super.visitField(access, owner, desc, signature, obj);
        }
        return null;
    }

    @Override
    public void visitEnd() {
        addMethodDynamicUpdateSQL();
        super.visitEnd();
    }


    /**
     * 添加DynamicUpdateSQL方法
     */
    private void addMethodDynamicUpdateSQL() {
        { //添加一个方法
            MethodVisitor mv = cv.visitMethod(Opcodes.ACC_PUBLIC,
                    "dynamicUpdateSQL",
                    "(Ljava/lang/String;" + ASMUtil.getDesc(JdbcModel.class, true) + ")" + ASMUtil.getDesc(Tuple.class, true),
                    "(Ljava/lang/String;" + ASMUtil.getDesc(JdbcModel.class, true) + ")" + ASMUtil.getSignature(Tuple.class, new Class<?>[][]{{String.class}, {List.class, Param.class}}),
                    null);

            mv.visitCode();

            //new一个StringBuilder
            mv.visitTypeInsn(Opcodes.NEW, ASMUtil.getClassName(StringBuilder.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtil.getClassName(StringBuilder.class), "<init>", "()V");
            mv.visitVarInsn(Opcodes.ASTORE, 3);

            // boolean start = false;
            mv.visitInsn(Opcodes.ICONST_0);
            mv.visitVarInsn(Opcodes.ISTORE, 4);

            //int index = 1;
            mv.visitInsn(Opcodes.ICONST_1);
            mv.visitVarInsn(Opcodes.ISTORE, 5);

            //new List
            mv.visitTypeInsn(Opcodes.NEW, ASMUtil.getClassName(ArrayList.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtil.getClassName(ArrayList.class), "<init>", "()V");
            mv.visitVarInsn(Opcodes.ASTORE, 6);

            // new PlayerPower
            mv.visitInsn(Opcodes.ACONST_NULL);
            mv.visitVarInsn(Opcodes.ASTORE, 7);

            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.INSTANCEOF, ASMUtil.getClassName(clazz));

            Label l0 = new Label();
            mv.visitJumpInsn(Opcodes.IFEQ, l0);

            Label l1 = new Label();
            mv.visitLabel(l1);
            mv.visitVarInsn(Opcodes.ALOAD, 2);
            mv.visitTypeInsn(Opcodes.CHECKCAST, ASMUtil.getClassName(clazz));
            mv.visitVarInsn(Opcodes.ASTORE, 7);
            mv.visitLabel(l0);
            mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{ASMUtil.getClassName(clazz)}, 0, null);


            for (String[] name : fieldSet) {

                Label l2 = new Label();
                if (name[2].equals("()J")) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);

                    mv.visitVarInsn(Opcodes.ALOAD, 7);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, enhanceSuperName, name[1], name[2]);

                    mv.visitInsn(Opcodes.LCMP);
                    mv.visitJumpInsn(Opcodes.IFEQ, l2);
                } else if (name[2].equals("()F")) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);

                    mv.visitVarInsn(Opcodes.ALOAD, 7);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, enhanceSuperName, name[1], name[2]);

                    mv.visitInsn(Opcodes.FCMPG);
                    mv.visitJumpInsn(Opcodes.IFEQ, l2);
                } else if (name[2].equals("()D")) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);

                    mv.visitVarInsn(Opcodes.ALOAD, 7);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, enhanceSuperName, name[1], name[2]);

                    mv.visitInsn(Opcodes.DCMPG);
                    mv.visitJumpInsn(Opcodes.IFEQ, l2);
                } else if (name[2].equals("()I") || name[2].equals("()S")) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);

                    mv.visitVarInsn(Opcodes.ALOAD, 7);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, enhanceSuperName, name[1], name[2]);

                    mv.visitJumpInsn(Opcodes.IF_ICMPEQ, l2);
                } else {
                    //object
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);

                    Label l8 = new Label();
                    mv.visitJumpInsn(Opcodes.IFNONNULL, l8);
                    mv.visitVarInsn(Opcodes.ALOAD, 7);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, enhanceSuperName, name[1], name[2]);

                    Label l9 = new Label();
                    mv.visitJumpInsn(Opcodes.IFNONNULL, l9);

                    mv.visitLabel(l8);
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);

                    mv.visitJumpInsn(Opcodes.IFNONNULL, l2);


                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);
                    mv.visitVarInsn(Opcodes.ALOAD, 7);
                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, enhanceSuperName, name[1], name[2]);

                    mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z");
                    mv.visitJumpInsn(Opcodes.IFNE, l2);

                    mv.visitLabel(l9);

                }

                mv.visitVarInsn(Opcodes.ILOAD, 4);
                Label l4 = new Label();
                mv.visitJumpInsn(Opcodes.IFNE, l4);


                Label l5 = new Label();
                mv.visitLabel(l5);
                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitLdcInsn("UPDATE ");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitVarInsn(Opcodes.ALOAD, 1);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitLdcInsn(" SET ");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(Opcodes.POP);
                mv.visitInsn(Opcodes.ICONST_1);
                mv.visitVarInsn(Opcodes.ISTORE, 4);
                mv.visitLabel(l4);


                mv.visitVarInsn(Opcodes.ILOAD, 5);
                mv.visitInsn(Opcodes.ICONST_1);
                Label l6 = new Label();
                mv.visitJumpInsn(Opcodes.IF_ICMPEQ, l6);


                Label l7 = new Label();
                mv.visitLabel(l7);
                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitLdcInsn(", ");
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(Opcodes.POP);
                mv.visitLabel(l6);

                mv.visitVarInsn(Opcodes.ALOAD, 3);
                mv.visitLdcInsn(strategy.propertyNameToColumnName(name[3] + " = ? "));
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(Opcodes.POP);

                mv.visitTypeInsn(Opcodes.NEW, ASMUtil.getClassName(Param.class));
                mv.visitInsn(Opcodes.DUP);
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, name[0], name[1], name[2]);
                if (name[2].equals("()I") || name[2].equals("()S")) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                } else if (name[2].equals("()F")) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");

                } else if (name[2].equals("()J")) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                } else if (name[2].equals("()D")) {
                    mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                }


                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtil.getClassName(Param.class), "<init>", "(Ljava/lang/Object;)V");
                mv.visitVarInsn(Opcodes.ASTORE, 8);

                mv.visitVarInsn(Opcodes.ALOAD, 6);
                mv.visitVarInsn(Opcodes.ALOAD, 8);
                mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, ASMUtil.getClassName(List.class), "add", "(Ljava/lang/Object;)Z");
                mv.visitInsn(Opcodes.POP);

                //i++
                mv.visitIincInsn(5, 1);
                mv.visitLabel(l2);

            }

            //new List
            mv.visitTypeInsn(Opcodes.NEW, ASMUtil.getClassName(Tuple.class));
            mv.visitInsn(Opcodes.DUP);
            mv.visitVarInsn(Opcodes.ALOAD, 3);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
            mv.visitVarInsn(Opcodes.ALOAD, 6);
            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ASMUtil.getClassName(Tuple.class), "<init>", "(Ljava/lang/Object;Ljava/lang/Object;)V");
            mv.visitVarInsn(Opcodes.ASTORE, 8);

            mv.visitVarInsn(Opcodes.ALOAD, 8);
            mv.visitInsn(Opcodes.ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }
}
