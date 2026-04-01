package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.DomainEntityProperty;
import com.intuit.sbd.payroll.psp.query.Property;

/**
 * Property Operators - between, in, like
 */
abstract class PropertyOperator<Q, V> extends Criterion<Q> {
    public PropertyOperator(Property<Q, V> pProperty) {
        this.setProperty(pProperty);
    }

    public Property<Q, V> getProperty() {
        return property;
    }

    @Override
    public DomainEntityProperty getParentDomainObjectExpression() {
        return getProperty().getParentDomainObjectExpression();
    }

    public void setProperty(Property<Q, V> pProperty) {
        property = pProperty;
    }

    @Override
    public String getParentProperty() {
        return getProperty().getParentProperty();
    }

    private Property<Q, V> property;

}

