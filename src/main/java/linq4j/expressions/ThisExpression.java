package linq4j.expressions;

/**
 * Created by marrony on 6/15/17.
 */
class ThisExpression extends ParameterExpression {
    protected ThisExpression() {
        super(0, "this", null);
    }
}
