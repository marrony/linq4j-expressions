package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

/**
 * Created by marrony on 6/15/17.
 */
public class MemberExpression extends Expression {
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
