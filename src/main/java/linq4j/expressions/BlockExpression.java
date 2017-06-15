package linq4j.expressions;

import linq4j.visitors.ExpressionVisitor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by marrony on 6/15/17.
 */
public class BlockExpression extends Expression {

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
