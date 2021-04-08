package com.reign.framework.jdbc.orm.asm;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * @ClassName: ASMClassMethodAdapter
 * @Description: ASM 方法修改适配器
 * @Author: wuwx
 * @Date: 2021-04-08 15:46
 **/
public class ASMClassMethodAdapter extends MethodAdapter {

    /**
     * 新的类名
     */
    private String className;

    public ASMClassMethodAdapter(MethodVisitor methodVisitor, String className) {
        super(methodVisitor);
        this.className = className;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, owner, name, desc);
    }
}
