package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.domain.$propertyType$;
import com.intuit.sbd.payroll.psp.query.propertyoperators.SubqueryExpression;

public class $propertyType$DomainEntitySetProperty<Q, V> extends DomainEntitySetProperty<Q, V> {
    public $propertyType$DomainEntitySetProperty(DomainEntityProperty<Q, ?> parentDataEntityExpression, String propertyName, String parentProperty) {
        super(parentDataEntityExpression, propertyName, parentProperty);
    }

    public $propertyType$Expression<Q> Filter() {
        //noinspection unchecked
        return new $propertyType$Expression<Q>(getParentDomainObjectExpression(), getPropertyName(), false);
    }

    @SuppressWarnings("unchecked")
    public Criterion<Q> Exists(Criterion<V> expr) {
        return new SubqueryExpression(SubqueryExpression.SubqueryType.Exists, $propertyType$.class, inversePropertyName, getParentDomainObjectExpression(), this, expr);
    }

    @SuppressWarnings("unchecked")
    public Criterion<Q> NotExists(Criterion<V> expr) {
        return new SubqueryExpression(SubqueryExpression.SubqueryType.NotExists, $propertyType$.class, inversePropertyName, getParentDomainObjectExpression(), this, expr);
    }
}
