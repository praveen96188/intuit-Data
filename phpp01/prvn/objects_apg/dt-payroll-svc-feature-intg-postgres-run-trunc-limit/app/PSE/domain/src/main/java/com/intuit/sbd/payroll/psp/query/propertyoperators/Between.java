package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;
import com.intuit.sbd.payroll.psp.query.Property;

/**
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:27:41 PM
 */
public class Between<Q, V> extends PropertyOperator<Q, V> {
    public Between(Property<Q, V> pProperty, V pFirst, V pLast) {
        super(pProperty);
        first = pFirst;
        last = pLast;
    }

    @Override
    public <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitBetweenExpression(this);
    }

    public V getFirst() {
        return first;
    }

    public void setFirst(V first) {
        this.first = first;
    }

    public V getLast() {
        return last;
    }

    public void setLast(V last) {
        this.last = last;
    }

    private V first;
    private V last;
}
