package linq4j.main;

import linq4j.visitors.ByteCodeGenerator;
import linq4j.expressions.Expression;
import linq4j.expressions.MethodInfo;
import org.objectweb.asm.*;
import org.objectweb.asm.Type;

public class Encoder {

    public static void main(String[] args) throws Exception {
        asmTest();
    }

    private static void asmTest() throws Exception {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, "User", null, "java/lang/Object", null);

        {
            FieldVisitor user = cw.visitField(Opcodes.ACC_PRIVATE, "user", Type.getDescriptor(String.class), null, null);
            user.visitEnd();
        }

        {
            FieldVisitor pwd = cw.visitField(Opcodes.ACC_PRIVATE, "pwd", Type.getDescriptor(String.class), null, null);
            pwd.visitEnd();
        }

        {
            String ctorDesc = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(String.class), Type.getType(String.class));
            MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", ctorDesc, null, null);

            ctor.visitCode();

            ByteCodeGenerator byteCodeGenerator = new ByteCodeGenerator(cw, ctor);
            Expression thisExpr = Expression._this();

            MethodInfo methodInfo = new MethodInfo(Type.getInternalName(Object.class), "<init>", Void.TYPE);
            Expression callSuper = Expression.call(thisExpr, methodInfo);
//            callSuper.accept(byteCodeGenerator);
//            ctor.visitVarInsn(Opcodes.ALOAD, 0);
//            ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);

            //user
            Expression assignUser = Expression.assign(
                    Expression.makeMemberAccess(thisExpr, "User", "user", String.class),
                    Expression.parameter(1, "user", String.class)
            );
//            assignUser.accept(byteCodeGenerator);
//            ctor.visitVarInsn(Opcodes.ALOAD, 0);
//            ctor.visitVarInsn(Opcodes.ALOAD, 1);
//            ctor.visitFieldInsn(Opcodes.PUTFIELD, "User", "user", Type.getDescriptor(String.class));

            //pwd
            Expression assignPwd = Expression.assign(
                    Expression.makeMemberAccess(thisExpr, "User", "pwd", String.class),
                    Expression.parameter(2, "pwd", String.class)
            );
//            assignPwd.accept(byteCodeGenerator);
//            ctor.visitVarInsn(Opcodes.ALOAD, 0);
//            ctor.visitVarInsn(Opcodes.ALOAD, 2);
//            ctor.visitFieldInsn(Opcodes.PUTFIELD, "User", "pwd", Type.getDescriptor(String.class));

            Expression block = Expression.block(callSuper, assignUser, assignPwd);
            block.accept(byteCodeGenerator);

            //ctor.visitInsn(Opcodes.RETURN);
            ctor.visitMaxs(0,0);
            ctor.visitEnd();
        }

        {
            String toStringDesc = Type.getMethodDescriptor(Type.getType(String.class));
            MethodVisitor toString = cw.visitMethod(Opcodes.ACC_PUBLIC, "toString", toStringDesc, null, null);
            toString.visitCode();

            Expression _this = Expression._this();
            ByteCodeGenerator byteCodeGenerator = new ByteCodeGenerator(cw, toString);

            //String.format("User(%s, %s)", user, pwd)
            Expression constant = Expression.constant("User(%s, %s)");
            //constant.accept(byteCodeGenerator);
//            toString.visitLdcInsn("User(%s, %s)");

            Expression newArray = Expression.parameter(1, "newArray", Object[].class);
            Expression temp = Expression.parameter(2, "temp", String.class);

            Expression assign = Expression.assign(
                    newArray,
                    Expression.newArray(
                            Object.class,
                            Expression.makeMemberAccess(_this, "User", "user", String.class),
                            Expression.makeMemberAccess(_this, "User", "pwd", String.class)
                    )
            );

            Expression expr0 = Expression.assign(
                    Expression.arrayAccess(newArray, Expression.constant(0)),
                    Expression.constant("index 0")
            );
            Expression expr1 = Expression.assign(
                    Expression.arrayAccess(newArray, Expression.constant(1)),
                    Expression.constant("index 1")
            );
            Expression expr2 = Expression.assign(
                    temp,
                    Expression.makeMemberAccess(_this, "User", "user", String.class)
            );
            Expression expr3 = Expression.assign(
                    Expression.arrayAccess(newArray, Expression.constant(1)),
                    temp
            );

            //expr.accept(byteCodeGenerator);

//            toString.visitInsn(Opcodes.ICONST_2);
//            toString.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
//
//            toString.visitInsn(Opcodes.DUP);
//            toString.visitInsn(Opcodes.ICONST_0); //index 0
//            toString.visitVarInsn(Opcodes.ALOAD, 0);
//            toString.visitFieldInsn(Opcodes.GETFIELD, "User", "user", Type.getDescriptor(String.class));
//            toString.visitInsn(Opcodes.AASTORE);
//
//            toString.visitInsn(Opcodes.DUP);
//            toString.visitInsn(Opcodes.ICONST_1); //index 1
//            toString.visitVarInsn(Opcodes.ALOAD, 0);
//            toString.visitFieldInsn(Opcodes.GETFIELD, "User", "pwd", Type.getDescriptor(String.class));
//            toString.visitInsn(Opcodes.AASTORE);

            MethodInfo methodInfo = new MethodInfo(Type.getInternalName(String.class), "format", String.class, String.class, Object[].class);
            Expression callToString = Expression.call(methodInfo, constant, newArray);

            //callToString.accept(byteCodeGenerator);

//            Method format = String.class.getDeclaredMethod("format", String.class, Object[].class);
//            String formatDesc = Type.getMethodDescriptor(format);
//            toString.visitMethodInsn(Opcodes.INVOKESTATIC, Type.getInternalName(String.class),"format", formatDesc,false);

            Expression.block(String.class, assign, expr0, expr1, expr2, expr3, callToString).accept(byteCodeGenerator);

            //toString.visitInsn(Opcodes.ARETURN);
            toString.visitMaxs(0, 0);
            toString.visitEnd();
        }

        cw.visitEnd();

        final byte[] clazzBytes = cw.toByteArray();

        ClassLoader classLoader = new ClassLoader() {
            @Override
            protected Class<?> findClass(String s) throws ClassNotFoundException {
                if ("User".equals(s))
                    return this.defineClass(s, clazzBytes, 0, clazzBytes.length);
                return super.findClass(s);
            }
        };

        Class<?> clazz = classLoader.loadClass("User");
        Object user = clazz.getConstructor(String.class, String.class).newInstance("marrony", "pwd");
        System.out.format("User: %s\n%s\n%s\n", user, Type.getDescriptor(Object[].class), Type.getInternalName(String.class));
    }
}

