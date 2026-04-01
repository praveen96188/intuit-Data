package com.intuit.sbd.payroll.psp.query.logicaloperators;

import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.DomainEntityProperty;
import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;

/**
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:34:03 PM
 */
public class Not<Q> extends Criterion<Q> {
    private Criterion<Q> expression;

    public Criterion<Q> getExpression() {
        return expression;
    }

    public void SetExpression(Criterion<Q> pExpression) {
        expression = pExpression;
    }

    public Not(Criterion<Q> expression) {
        this.expression = expression;
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitNotExpression(this);
    }

    @Override
    public String getParentProperty() {
        return getExpression().getParentProperty();
    }

    @Override
    public DomainEntityProperty getParentDomainObjectExpression() {
        return getExpression().getParentDomainObjectExpression();
    }
}
