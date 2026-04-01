package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;

/**
 * User: rnorian
 * Date: Feb 17, 2010
 * Time: 3:24:36 PM
 */
public class NotIn<Q, V> extends In<Q, V> {
    public NotIn(Property<Q, V> pProperty, V... pValueList) {
        super(pProperty, pValueList);
    }

    @Override
    public <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitNotInExpression(this);
    }
}
