package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.query.logicaloperators.And;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Not;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Or;
import com.intuit.sbd.payroll.psp.query.propertyoperators.*;

public abstract class ExpressionVisitor<Q, T> {
    public ExpressionVisitor(Expression<Q> pExpression) {
        expression = pExpression;
    }

    public T visit() {
        return expression.accept(this);
    }

    public abstract T visitAndExpression(And<?> andExpr);
    public abstract T visitOrExpression(Or<?> orExpr);
    public abstract T visitNotExpression(Not<?> notExpr);
    public abstract T visitPropertyComparisonExpression(PropertyComparisonExpression<?, ?> propertyComparisonExpr);
    public abstract T visitBetweenExpression(Between<?, ?> betweenExpr);
    public abstract T visitInExpression(In<?, ?> inExpr);
    public abstract T visitNotInExpression(NotIn<?, ?> notInExpr);
    public abstract T visitLikeExpression(Like<?, ?> likeExpr);
    public abstract T visitRegexpLikeExpression(RegexpLike<?, ?> regexpLikeExpr);
    public abstract T visitIsNullExpression(IsNull<?, ?> isNullExpr);
    public abstract T visitIsNotNullExpression(IsNotNull<?, ?> isNotNullExpr);
    public abstract T visitPropertyExpression(Property<?, ?> pProperty);
    public abstract T visitQueryExpression(Query<?> query);
    public abstract T visitEmptyExpression(EmptyCriterion<?> query);
    public abstract T visitSubqueryExpression(SubqueryExpression<?, ?> subqueryExpression);

    private Expression<Q> expression;
}