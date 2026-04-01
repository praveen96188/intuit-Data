package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;
import com.intuit.sbd.payroll.psp.query.Property;

/**
 * User: ihannur
 * Date: 12/17/13
 * Time: 8:04 PM
 */
public class RegexpLike<Q, V> extends PropertyOperator<Q, V> {

    private String regexpLikeString;

    public RegexpLike(Property<Q, V> pProperty, String pRegexpLikeString) {
        super(pProperty);
        regexpLikeString = pRegexpLikeString;
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitRegexpLikeExpression(this);
    }

    public String getRegexpLikeString() {
        return regexpLikeString;
    }

    public void setRegexpLikeString(String pRegexpLikeString) {
        regexpLikeString = pRegexpLikeString;
    }
}
