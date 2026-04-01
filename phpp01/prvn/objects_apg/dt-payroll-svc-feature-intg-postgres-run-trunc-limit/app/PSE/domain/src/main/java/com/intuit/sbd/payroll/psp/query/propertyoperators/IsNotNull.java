package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;
import com.intuit.sbd.payroll.psp.query.Property;

/**
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:27:51 PM
 */
public class IsNotNull<Q, V> extends PropertyOperator<Q, V> {
    public IsNotNull(Property<Q, V> pProperty) {
        super(pProperty);
    }

    @Override
    public <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitIsNotNullExpression(this);
    }
}