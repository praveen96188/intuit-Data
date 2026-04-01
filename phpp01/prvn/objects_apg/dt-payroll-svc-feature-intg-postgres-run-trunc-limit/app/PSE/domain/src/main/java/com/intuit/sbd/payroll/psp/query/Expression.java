package com.intuit.sbd.payroll.psp.query;

import org.apache.commons.lang.NotImplementedException;

public abstract class Expression<Q> {
    protected abstract <T> T accept(ExpressionVisitor<?, T> visitor);
    public abstract String getParentProperty();
    public DomainEntityProperty getParentDomainObjectExpression(){
        //TODO : implement wherever required
        throw new NotImplementedException();
    }
}








