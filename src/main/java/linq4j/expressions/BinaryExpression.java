package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

/**
 * Created by marrony on 6/15/17.
 */
public class BinaryExpression extends Expression {
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
