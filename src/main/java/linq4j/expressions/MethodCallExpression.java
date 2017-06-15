package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

/**
 * Created by marrony on 6/15/17.
 */
public class MethodCallExpression extends Expression {
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
