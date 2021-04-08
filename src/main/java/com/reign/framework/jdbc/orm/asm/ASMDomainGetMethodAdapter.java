package com.reign.framework.jdbc.orm.asm;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName: ASMDomainGetMethodAdapter
 * @Description: domainGet
 * @Author: wuwx
 * @Date: 2021-04-08 15:46
 **/
public class ASMDomainGetMethodAdapter extends MethodAdapter {


    public static final Set<String[]> set = new HashSet<>();

    private String className;


    public ASMDomainGetMethodAdapter(MethodVisitor methodVisitor, String className) {
        super(methodVisitor);
        this.className = className;
    }


    @Override
    public void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var);
    }


    @Override
    public void visitCode() {
        super.visitCode();
    }


    @Override
    public void visitFieldInsn(int i, String s, String s1, String s2) {
        super.visitFieldInsn(i, s, s1, s2);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        super.visitMethodInsn(opcode, owner, name, desc);
        set.add(new String[]{owner, name, desc});
    }
}
