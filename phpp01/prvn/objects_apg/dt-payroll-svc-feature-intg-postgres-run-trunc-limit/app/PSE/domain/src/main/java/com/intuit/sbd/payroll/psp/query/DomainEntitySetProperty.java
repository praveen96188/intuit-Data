package com.intuit.sbd.payroll.psp.query;

/**
 * User: achaves
 * Date: Nov 30, 2008
 * Time: 5:56:15 PM
 */
public class DomainEntitySetProperty<Q, V> extends Property<Q, V> {

    protected String inversePropertyName;

    public DomainEntitySetProperty(DomainEntityProperty<Q, ?> parentDataEntityExpression, String propertyName) {
        super(parentDataEntityExpression, propertyName);
    }

    public DomainEntitySetProperty(DomainEntityProperty<Q, ?> parentDataEntityExpression, String propertyName, String pInversePropertyName) {
        super(parentDataEntityExpression, propertyName);
        inversePropertyName = pInversePropertyName;
    }
}
