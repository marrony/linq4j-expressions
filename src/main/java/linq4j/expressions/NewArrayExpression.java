package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

/**
 * Created by marrony on 6/15/17.
 */
public class NewArrayExpression extends Expression {
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
