package com.intuit.sbd.payroll.psp.query;

import org.apache.commons.lang.NotImplementedException;

/**
 * User: dweinberg
 * Date: 7/8/11
 * Time: 1:37 PM
 */
public class EmptyCriterion<Q> extends Criterion<Q> {

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitEmptyExpression(this);
    }

    @Override
    public String getParentProperty() {
        return null;
    }

    @Override
    public DomainEntityProperty getParentDomainObjectExpression(){
        return null;
    }


}
