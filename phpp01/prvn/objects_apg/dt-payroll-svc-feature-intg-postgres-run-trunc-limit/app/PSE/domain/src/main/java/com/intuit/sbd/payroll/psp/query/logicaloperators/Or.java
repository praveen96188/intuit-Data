package com.intuit.sbd.payroll.psp.query.logicaloperators;

import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;

/**
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:34:17 PM
 */
public class Or<Q> extends BinaryOperator<Q> {

    public Or(Criterion<Q> pLeftExpression, Criterion<Q> pRightExpression) {
        super(pLeftExpression, pRightExpression);
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitOrExpression(this);
    }
}
