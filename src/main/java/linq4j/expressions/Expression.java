package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

import java.util.Collections;

public abstract class Expression {
    private final ExpressionType nodeType;

    protected Expression(ExpressionType nodeType) {
        this.nodeType = nodeType;
    }

    public ExpressionType getNodeType() {
        return nodeType;
    }

    public abstract Expression accept(ExpressionVisitor visitor);

    //public static BinaryExpression add();
    //addAssign
    //and
    //arrayAccess
    //arrayIndex
    //arrayLength
    //

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
