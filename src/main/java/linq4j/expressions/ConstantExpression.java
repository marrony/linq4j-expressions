package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

/**
 * Created by marrony on 6/15/17.
 */
public class ConstantExpression extends Expression {
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
