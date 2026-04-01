package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:27:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class IsNull<Q, V> extends PropertyOperator<Q, V> {
    public IsNull(Property<Q, V> pProperty) {
        super(pProperty);
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitIsNullExpression(this);
    }
}