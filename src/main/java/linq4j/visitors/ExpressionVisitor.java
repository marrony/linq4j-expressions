package linq4j.visitors;

import linq4j.expressions.*;

public abstract class ExpressionVisitor {
    public Expression visit(Expression expression) {
        return expression.accept(this);
    }

    public Expression visitBinary(BinaryExpression binaryExpression) {
        Expression left = visit(binaryExpression.getLeft());
        Expression right = visit(binaryExpression.getRight());

        if (left != binaryExpression.getLeft() || right != binaryExpression.getRight())
            return Expression.makeBinary(binaryExpression.getNodeType(), left, right);

        return binaryExpression;
    }

    public Expression visitMember(MemberExpression memberExpression) {
        Expression expression = visit(memberExpression.getExpression());

        if (expression != memberExpression.getExpression())
            return Expression.makeMemberAccess(expression, memberExpression.getOwner(), memberExpression.getMember(), memberExpression.getType());

        return memberExpression;
    }

    public Expression visitParameter(ParameterExpression parameterExpression) {
        return parameterExpression;
    }

    public Expression visitConstant(ConstantExpression constantExpression) {
        return constantExpression;
    }

    public Expression visitMethodCall(MethodCallExpression methodCallExpression) {
        return methodCallExpression;
    }

    public Expression visitBlock(BlockExpression blockExpression) {
        return blockExpression;
    }

    public Expression visitIndex(IndexExpression indexExpression) {
        return indexExpression;
    }

    public Expression visitNewArrayInit(NewArrayExpression newArrayExpression) {
        return newArrayExpression;
    }
}
