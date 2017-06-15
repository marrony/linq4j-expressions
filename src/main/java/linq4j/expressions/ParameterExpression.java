package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

/**
 * Created by marrony on 6/15/17.
 */
public class ParameterExpression extends Expression {
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
