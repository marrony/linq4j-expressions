package linq4j.visitors;

import linq4j.expressions.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Created by marrony on 6/15/17.
 */
public class ByteCodeGenerator extends ExpressionVisitor {
    private final ClassWriter cw;
    private final MethodVisitor method;

    public ByteCodeGenerator(ClassWriter cw, MethodVisitor method) {
        this.cw = cw;
        this.method = method;
    }

    @Override
    public Expression visitBinary(BinaryExpression binaryExpression) {
        System.out.printf("visitBinary: %s\n", binaryExpression.getNodeType());

        if (binaryExpression.getNodeType() == ExpressionType.Assign) {
            if (binaryExpression.getLeft().getNodeType() == ExpressionType.MemberAccess) {
                //obj.field = value
                MemberExpression member = (MemberExpression) binaryExpression.getLeft();

                visit(member.getExpression());        //load obj
                visit(binaryExpression.getRight());   //load value

                method.visitFieldInsn(Opcodes.PUTFIELD, member.getOwner(), member.getMember(), Type.getDescriptor(member.getType()));
            } else if (binaryExpression.getLeft().getNodeType() == ExpressionType.ArrayIndex) {
                //array[index] = value
                visit(binaryExpression.getLeft());    //load array+index
                visit(binaryExpression.getRight());   //load value

                IndexExpression index = (IndexExpression) binaryExpression.getLeft();

                method.visitInsn(Opcodes.AASTORE);
//            } else if (binaryExpression.getRight().getNodeType() == ExpressionType.MemberAccess) {
//                //variable = obj.field
//                MemberExpression member = (MemberExpression) binaryExpression.getRight();
//
//                visit(binaryExpression.getLeft());
//                //visit(binaryExpression.getRight());
//
//                method.visitFieldInsn(Opcodes.GETFIELD, member.getOwner(), member.getMember(), Type.getDescriptor(member.getType()));
//
//                //throw new IllegalStateException("variable = obj.field");
            } else if (binaryExpression.getLeft().getNodeType() == ExpressionType.Parameter) {
                //variable = value
                ParameterExpression parameterExpression = (ParameterExpression) binaryExpression.getLeft();

//                if (binaryExpression.getRight().getNodeType() == ExpressionType.MemberAccess) {
//                    MemberExpression member = (MemberExpression) binaryExpression.getRight();
//
//                    visit(member.getExpression());
//                    method.visitFieldInsn(Opcodes.GETFIELD, member.getOwner(), member.getMember(), Type.getDescriptor(member.getType()));
//                } else {
                    visit(binaryExpression.getRight());
//                }

                method.visitVarInsn(Opcodes.ASTORE, parameterExpression.getIndex());
            } else {
                throw new IllegalStateException("What?");
            }

            return binaryExpression;
        }

        return super.visitBinary(binaryExpression);
    }

    @Override
    public Expression visitMember(MemberExpression memberExpression) {
        System.out.printf("visitMember: %s\n", memberExpression.getMember());

        visit(memberExpression.getExpression());
        method.visitFieldInsn(Opcodes.GETFIELD, memberExpression.getOwner(), memberExpression.getMember(), Type.getDescriptor(memberExpression.getType()));

        return memberExpression;
    }

    @Override
    public Expression visitParameter(ParameterExpression parameterExpression) {
        System.out.printf("visitParameter: %d %s\n", parameterExpression.getIndex(), parameterExpression.getName());
        method.visitVarInsn(Opcodes.ALOAD, parameterExpression.getIndex());
        return parameterExpression;
    }

    @Override
    public Expression visitConstant(ConstantExpression constantExpression) {
        System.out.printf("visitConstant: %s\n", constantExpression.getValue());
        method.visitLdcInsn(constantExpression.getValue());
        return constantExpression;
    }

    @Override
    public Expression visitMethodCall(MethodCallExpression methodCallExpression) {
        System.out.printf("visitMethodCall: %s\n", methodCallExpression.getMethodInfo().getOwner());

//            ctor.visitVarInsn(Opcodes.ALOAD, 0);
//            ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);

        if (methodCallExpression.getExpression() != null)
            visit(methodCallExpression.getExpression());

        for (Expression parameter : methodCallExpression.getParameters())
            visit(parameter);

        MethodInfo methodInfo = methodCallExpression.getMethodInfo();
        Type[] types = new Type[methodInfo.getTypes().length];
        for (int i = 0; i < types.length; i++)
            types[i] = Type.getType(methodInfo.getTypes()[i]);

        String methodDescriptor = Type.getMethodDescriptor(Type.getType(methodInfo.getReturnType()), types);
        int invoke = methodCallExpression.getExpression() != null ? Opcodes.INVOKESPECIAL : Opcodes.INVOKESTATIC;
        method.visitMethodInsn(invoke, methodInfo.getOwner(), methodInfo.getName(), methodDescriptor, false);

        return methodCallExpression;
    }

    @Override
    public Expression visitBlock(BlockExpression blockExpression) {
        System.out.printf("visitBlock\n");

        for (Expression expression : blockExpression.getExpressions())
            visit(expression);

        visit(blockExpression.getResult());

        if (blockExpression.getType() != Void.TYPE)
            method.visitInsn(Opcodes.ARETURN);
        else
            method.visitInsn(Opcodes.RETURN);

        return blockExpression;
    }

    @Override
    public Expression visitIndex(IndexExpression indexExpression) {
        System.out.printf("visitIndex\n");

        visit(indexExpression.getArray());
        visit(indexExpression.getIndex());

        return indexExpression;
    }

    private void loadInt(int i) {
        switch (i) {
            case 0: method.visitInsn(Opcodes.ICONST_0); break;
            case 1: method.visitInsn(Opcodes.ICONST_1); break;
            case 2: method.visitInsn(Opcodes.ICONST_2); break;
            case 3: method.visitInsn(Opcodes.ICONST_3); break;
            case 4: method.visitInsn(Opcodes.ICONST_4); break;
            case 5: method.visitInsn(Opcodes.ICONST_5); break;
            default: method.visitLdcInsn(i);
        }
    }

    @Override
    public Expression visitNewArrayInit(NewArrayExpression newArrayExpression) {
        System.out.printf("visitNewArrayInit\n");

        Expression[] expressions = newArrayExpression.getExpressions();

        loadInt(expressions.length);
        method.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(newArrayExpression.getType()));

        for (int i = 0; i < expressions.length; i++) {
            method.visitInsn(Opcodes.DUP);
            loadInt(i);
            visit(expressions[i]);
            method.visitInsn(Opcodes.AASTORE);
        }

//        toString.visitInsn(Opcodes.ICONST_2);
//        toString.visitTypeInsn(Opcodes.ANEWARRAY, Type.getInternalName(Object.class));
//
//        toString.visitInsn(Opcodes.DUP);
//        toString.visitInsn(Opcodes.ICONST_0); //index 0
//        toString.visitVarInsn(Opcodes.ALOAD, 0);
//        toString.visitFieldInsn(Opcodes.GETFIELD, "User", "user", Type.getDescriptor(String.class));
//        toString.visitInsn(Opcodes.AASTORE);
//
//        toString.visitInsn(Opcodes.DUP);
//        toString.visitInsn(Opcodes.ICONST_1); //index 1
//        toString.visitVarInsn(Opcodes.ALOAD, 0);
//        toString.visitFieldInsn(Opcodes.GETFIELD, "User", "pwd", Type.getDescriptor(String.class));
//        toString.visitInsn(Opcodes.AASTORE);

        return newArrayExpression;
    }
}
