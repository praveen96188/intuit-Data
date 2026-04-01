package com.intuit.sbd.payroll.psp.query.logicaloperators;

import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;

/**
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:33:46 PM
 */
public class And<Q> extends BinaryOperator<Q> {

    public And(Criterion<Q> pLeftExpression, Criterion<Q> pRightExpression) {
        super(pLeftExpression, pRightExpression);
    }

    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitAndExpression(this);
    }
}
