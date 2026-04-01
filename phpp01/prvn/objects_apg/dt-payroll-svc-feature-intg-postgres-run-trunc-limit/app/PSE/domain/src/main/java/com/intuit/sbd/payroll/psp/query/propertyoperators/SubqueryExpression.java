package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.DomainEntityProperty;
import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;
import com.intuit.sbd.payroll.psp.query.Property;

/**
 * User: dweinberg
 * Date: 1/2/13
 * Time: 4:26 PM
 */
public class SubqueryExpression<Q, V> extends PropertyOperator<Q, V> {

    public enum SubqueryType {
        Exists,
        NotExists
    }

    private V right;
    private SubqueryType subqueryType;
    private Class<V> subqueryClass;
    private String parentProperty;
    private DomainEntityProperty parentDomainEntityExpression;

    public SubqueryExpression(SubqueryType pSubqueryType, Class<V> pSubqueryClass, String pParentProperty, DomainEntityProperty pParentDomainEntityExpression, Property<Q, V> pLeft, V pRight) {
        super(pLeft);
        right = pRight;
        subqueryType = pSubqueryType;
        subqueryClass = pSubqueryClass;
        parentProperty = pParentProperty;
        parentDomainEntityExpression = pParentDomainEntityExpression;
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitSubqueryExpression(this);
    }

    public V getRight() {
        return right;
    }

    public SubqueryType getSubqueryType() {
        return subqueryType;
    }

    public Class<V> getSubqueryClass() {
        return subqueryClass;
    }

    public String getParentProperty() {
        return parentProperty;
    }

    public DomainEntityProperty getParentDomainEntityExpression() {
        return parentDomainEntityExpression;
    }
}
