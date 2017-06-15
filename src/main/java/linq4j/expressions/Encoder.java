package linq4j.expressions;

import org.objectweb.asm.*;
import org.objectweb.asm.Type;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

enum ExpressionType {
    Assign,
    Constant, Parameter, Call, Block, ArrayIndex, NewArrayInit, MemberAccess
}

abstract class Expression {
    private final ExpressionType nodeType;

    protected Expression(ExpressionType nodeType) {
        this.nodeType = nodeType;
    }

    public ExpressionType getNodeType() {
        return nodeType;
    }

    public abstract Expression accept(ExpressionVisitor visitor);

    public static BinaryExpression makeBinary(ExpressionType nodeType, Expression left, Expression right) {
        return new BinaryExpression(nodeType, left, right);
    }

    public static BinaryExpression assign(Expression left, Expression right) {
        return makeBinary(ExpressionType.Assign, left, right);
    }

    public static MemberExpression makeMemberAccess(Expression expression, String owner, String member, Class<?> type) {
        return new MemberExpression(expression, owner, member, type);
    }

    public static ConstantExpression constant(Object value) {
        return new ConstantExpression(value);
    }

    public static ParameterExpression parameter(int index, String name, Class<?> type) {
        return new ParameterExpression(index, name, type);
    }

    public static ThisExpression _this() {
        return new ThisExpression();
    }

    public static MethodCallExpression call(Expression expression, MethodInfo methodInfo, Expression... parameters) {
        return new MethodCallExpression(expression, methodInfo, parameters);
    }

    public static MethodCallExpression call(MethodInfo methodInfo, Expression... parameters) {
        return new MethodCallExpression(null, methodInfo, parameters);
    }

    public static BlockExpression block(Expression... expressions) {
        return new BlockExpression(Void.TYPE, Collections.<ParameterExpression>emptyList(), expressions);
    }

    public static BlockExpression block(Class<?> type, Expression... expressions) {
        return new BlockExpression(type, Collections.<ParameterExpression>emptyList(), expressions);
    }

    public static NewArrayExpression newArray(Class<Object> type, Expression... expressions) {
        return new NewArrayExpression(type, expressions);
    }

    public static IndexExpression arrayAccess(Expression array, Expression index) {
        return new IndexExpression(array, index);
    }
}

class BinaryExpression extends Expression {
    private final Expression left;
    private final Expression right;

    protected BinaryExpression(ExpressionType nodeType, Expression left, Expression right) {
        super(nodeType);
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitBinary(this);
    }
}

class MemberExpression extends Expression {
    private final Expression expression;
    private final String owner;
    private final String member;
    private final Class<?> type;

    protected MemberExpression(Expression expression, String owner, String member, Class<?> type) {
        super(ExpressionType.MemberAccess);
        this.expression = expression;
        this.owner = owner;
        this.member = member;
        this.type = type;
    }

    public Expression getExpression() {
        return expression;
    }

    public String getOwner() {
        return owner;
    }

    public String getMember() {
        return member;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitMember(this);
    }
}

class ParameterExpression extends Expression {
    private final int index;
    private final String name;
    private final Class<?> type;

    protected ParameterExpression(int index, String name, Class<?> type) {
        super(ExpressionType.Parameter);
        this.index = index;
        this.name = name;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitParameter(this);
    }
}

class ThisExpression extends ParameterExpression {
    protected ThisExpression() {
        super(0, "this", null);
    }
}

class MethodInfo {
    private final String owner;
    private final String name;
    private final Class<?> returnType;
    private final Class<?>[] types;

    public MethodInfo(String owner, String name, Class<?> returnType, Class<?>... types) {
        this.owner = owner;
        this.name = name;
        this.returnType = returnType;
        this.types = types;
    }

    public String getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public Class<?>[] getTypes() {
        return types;
    }
}

class MethodCallExpression extends Expression {
    private final Expression expression;
    private final MethodInfo methodInfo;
    private final Expression[] parameters;

    protected MethodCallExpression(Expression expression, MethodInfo methodInfo, Expression... parameters) {
        super(ExpressionType.Call);
        this.expression = expression;
        this.methodInfo = methodInfo;
        this.parameters = parameters;
    }

    public Expression getExpression() {
        return expression;
    }

    public MethodInfo getMethodInfo() {
        return methodInfo;
    }

    public Expression[] getParameters() {
        return parameters;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitMethodCall(this);
    }
}

class ConstantExpression extends Expression {
    private final Object value;

    protected ConstantExpression(Object value) {
        super(ExpressionType.Constant);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitConstant(this);
    }
}

class BlockExpression extends Expression {

    private final Class<?> type;
    private final List<ParameterExpression> variables;
    private final List<Expression> expressions;
    private final Expression result;

    protected BlockExpression(Class<?> type, List<ParameterExpression> variables, Expression... expressions) {
        super(ExpressionType.Block);
        this.type = type;
        this.variables = variables;

        if (expressions.length == 1) {
            this.expressions = Collections.emptyList();
            this.result = expressions[0];
        } else {
            this.expressions = Arrays.asList(expressions).subList(0, expressions.length - 1);
            this.result = expressions[expressions.length - 1];
        }
    }

    public Class<?> getType() {
        return type;
    }

    public List<ParameterExpression> getVariables() {
        return variables;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public Expression getResult() {
        return result;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitBlock(this);
    }
}

class IndexExpression extends Expression {
    private final Expression array;
    private final Expression index;

    protected IndexExpression(Expression array, Expression index) {
        super(ExpressionType.ArrayIndex);
        this.array = array;
        this.index = index;
    }

    public Expression getArray() {
        return array;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitIndex(this);
    }
}

class NewArrayExpression extends Expression {
    private final Class<?> type;
    private final Expression[] expressions;

    protected NewArrayExpression(Class<?> type, Expression... expressions) {
        super(ExpressionType.NewArrayInit);
        this.type = type;
        this.expressions = expressions;
    }

    public Class<?> getType() {
        return type;
    }

    public Expression[] getExpressions() {
        return expressions;
    }

    @Override
    public Expression accept(ExpressionVisitor visitor) {
        return visitor.visitNewArrayInit(this);
    }
}

abstract class ExpressionVisitor {
    public Expression visit(Expression expression) {
        return expression.accept(this);
    }

    public Expression visitBinary(BinaryExpression binaryExpression) {
        Expression left = visit(binaryExpression.getLeft());
        Expression right = visit(binaryExpression.getRight());

        if (left != binaryExpression.getLeft() || right != binaryExpression.getRight())
            return Expression.makeBinary(binaryExpression.getNodeType(), left, right);

        return binaryExpression;
    }

    public Expression visitMember(MemberExpression memberExpression) {
        Expression expression = visit(memberExpression.getExpression());

        if (expression != memberExpression.getExpression())
            return Expression.makeMemberAccess(expression, memberExpression.getOwner(), memberExpression.getMember(), memberExpression.getType());

        return memberExpression;
    }

    public Expression visitParameter(ParameterExpression parameterExpression) {
        return parameterExpression;
    }

    public Expression visitConstant(ConstantExpression constantExpression) {
        return constantExpression;
    }

    public Expression visitMethodCall(MethodCallExpression methodCallExpression) {
        return methodCallExpression;
    }

    public Expression visitBlock(BlockExpression blockExpression) {
        return blockExpression;
    }

    public Expression visitIndex(IndexExpression indexExpression) {
        return indexExpression;
    }

    public Expression visitNewArrayInit(NewArrayExpression newArrayExpression) {
        return newArrayExpression;
    }
}

class ByteCodeGenerator extends ExpressionVisitor {
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

public class Encoder {
    public static class User {
        private final String name;
        private final String pwd;

        public User(String name, String pwd) {
            this.name = name;
            this.pwd = pwd;
        }

        public String getName() { return name; }
        public String getPwd() { return pwd; }

        @Override
        public String toString() {
            return String.format("User(%s, %s)", name, pwd);
        }
    }

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

//    private static void implementFunctionAndGetAValue() throws Exception {
//        Method getName = User.class.getDeclaredMethod("getName");
//
//        Function fn = new ByteBuddy()
//                .subclass(Function.class)
//                .method(ElementMatchers.named("apply"))
//                .intercept(MethodCall.invoke(getName).onArgument(0).withAssigner(Assigner.DEFAULT, Assigner.Typing.DYNAMIC))
//                .make()
//                .load(Encoder.class.getClassLoader())
//                .getLoaded()
//                .newInstance();
//
//        System.out.format("User name: %s\n", fn.apply(new User("name", "secret")));
//    }
//
//    private static void instantiateAObject() throws Exception {
//        Constructor<User> constructor = User.class.getConstructor(String.class, String.class);
//
////        Implementation interceptor = InvokeDynamic.bootstrap(constructor, "first", "second");
//        //Implementation interceptor = MethodCall.construct(constructor).withReference(FixedValue.value("first"),
//        //        FixedValue.value("second"));
//        Implementation interceptor = MethodCall.construct(constructor).with("first", "second");
//                //.with(ImmutableMap.of("a", new Value.StringV("b")));
//
//        Function fn = new ByteBuddy()
//                .subclass(Function.class)
//                .method(ElementMatchers.named("apply")).intercept(interceptor)
//                .make()
//                .load(Encoder.class.getClassLoader())
//                .getLoaded()
//                .newInstance();
//
//        System.out.format("User name: %s\n", fn.apply("string"));
//    }
//
//    private static void createConstructor() throws Exception {
//        DynamicType.Builder<?> builder = new ByteBuddy()
//                .subclass(Object.class)
//                .name("What");
//
//        Implementation interceptor = StubMethod.INSTANCE;
//
//        //field user
//        builder = builder.defineField("user", String.class, Visibility.PRIVATE, FieldManifestation.FINAL);
//        interceptor = FieldAccessor.ofField("user").setsArgumentAt(0).andThen(interceptor);
//
//        //field pwd
//        builder = builder.defineField("pwd", String.class, Visibility.PRIVATE, FieldManifestation.FINAL);
//        interceptor = FieldAccessor.ofField("pwd").setsArgumentAt(1).andThen(interceptor);
//
//        //call super constructor
//        interceptor = MethodCall.invoke(Object.class.getConstructor()).andThen(interceptor);
//
//        Class<?> clazz = builder
//                .defineConstructor(Visibility.PUBLIC).withParameters(String.class, String.class).intercept(interceptor)
//                .make()
//                .load(Encoder.class.getClassLoader())
//                .getLoaded();
//
//        Object obj = clazz.getConstructor(String.class, String.class).newInstance("marrony", "pwd");
//        System.out.format("Obj: %s\n", obj);
//    }
}

