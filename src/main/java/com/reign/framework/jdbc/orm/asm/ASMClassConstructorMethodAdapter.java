package com.reign.framework.jdbc.orm.asm;

import jdk.internal.org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @ClassName: ASMClassConstructorMethodAdapter
 * @Description: ASM构造函数适配器
 * @Author: wuwx
 * @Date: 2021-04-08 15:46
 **/
public class ASMClassConstructorMethodAdapter extends MethodAdapter {

    //父类名字
    private String className;

    public ASMClassConstructorMethodAdapter(MethodVisitor methodVisitor, String className) {
        super(methodVisitor);
        this.className = className;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
            //在调用父类的构造函数
            owner = className;
        }
        super.visitMethodInsn(opcode, owner, name, desc);

    }
}
