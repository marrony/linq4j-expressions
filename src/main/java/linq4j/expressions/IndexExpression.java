package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

/**
 * Created by marrony on 6/15/17.
 */
public class IndexExpression extends Expression {
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
