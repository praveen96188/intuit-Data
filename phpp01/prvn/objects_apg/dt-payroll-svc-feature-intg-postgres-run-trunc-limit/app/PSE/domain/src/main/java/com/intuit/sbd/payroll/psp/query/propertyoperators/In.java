package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;
import com.intuit.sbd.payroll.psp.query.Property;

/**
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 4:27:47 PM
 */
public class In<Q, V> extends PropertyOperator<Q, V> {
    public In(Property<Q, V> pProperty, V... pValueList) {
        super(pProperty);
        valueList = pValueList;
    }

    @Override
    public <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitInExpression(this);
    }

    public V[] getValueList() {
        return valueList;
    }

    public void setValueList(V[] valueList) {
        this.valueList = valueList;
    }

    private V[] valueList;
}
