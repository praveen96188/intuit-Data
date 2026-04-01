package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.domainsecondary.Hcm401kCompanyQbdtPitem;
import com.intuit.sbd.payroll.psp.query.propertyoperators.SubqueryExpression;

public class Hcm401kCompanyQbdtPitemDomainEntitySetProperty<Q, V> extends DomainEntitySetProperty<Q, V> {
    public Hcm401kCompanyQbdtPitemDomainEntitySetProperty(DomainEntityProperty<Q, ?> parentDataEntityExpression, String propertyName, String parentProperty) {
        super(parentDataEntityExpression, propertyName, parentProperty);
    }

    public com.intuit.sbd.payroll.psp.query.Hcm401kCompanyQbdtPitemExpression<Q> Filter() {
        //noinspection unchecked
        return new Hcm401kCompanyQbdtPitemExpression<Q>(getParentDomainObjectExpression(), getPropertyName(), false);
    }

    @SuppressWarnings("unchecked")
    public Criterion<Q> Exists(Criterion<V> expr) {
        return new SubqueryExpression(SubqueryExpression.SubqueryType.Exists, Hcm401kCompanyQbdtPitem.class, inversePropertyName, getParentDomainObjectExpression(), this, expr);
    }

    @SuppressWarnings("unchecked")
    public Criterion<Q> NotExists(Criterion<V> expr) {
        return new SubqueryExpression(SubqueryExpression.SubqueryType.NotExists, Hcm401kCompanyQbdtPitem.class, inversePropertyName, getParentDomainObjectExpression(), this, expr);
    }
}