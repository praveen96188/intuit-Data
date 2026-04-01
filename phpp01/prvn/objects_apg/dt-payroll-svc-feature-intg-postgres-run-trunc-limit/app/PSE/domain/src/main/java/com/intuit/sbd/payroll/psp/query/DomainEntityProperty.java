package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.query.propertyoperators.*;

/**
 * User: achaves
 * Date: Dec 9, 2008
 * Time: 3:32:29 PM
 */
public class DomainEntityProperty<Q, V> extends Property<Q, V> implements SortableProperty<Q, V> {
    public DomainEntityProperty(DomainEntityProperty parentDataEntityExpression, String propertyName, boolean isImplementedAsCollection) {
        super(parentDataEntityExpression, propertyName);
        this.setImplementedAsCollection(isImplementedAsCollection);
    }
    public DomainEntityProperty(DomainEntityProperty parentDataEntityExpression, String propertyName) {
        this(parentDataEntityExpression, propertyName, false);
    }

    public PropertyComparisonExpression<Q, V> equalTo(V value) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.Equal, this, value);
    }
    public PropertyComparisonExpression<Q, V> equalTo(Property<Q, V> property) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.Equal, this, property);
    }

    public PropertyComparisonExpression<Q, V> notEqualTo(V value) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.NotEqual, this, value);
    }

    public PropertyComparisonExpression<Q, V> greaterOrEqualThan(V value) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.GreaterOrEqualThan, this, value);
    }

    public PropertyComparisonExpression<Q, V> greaterThan(V value) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.GreaterThan, this, value);
    }

    public PropertyComparisonExpression<Q, V> lessOrEqualThan(V value) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.LessOrEqualThan, this, value);
    }

    public PropertyComparisonExpression<Q, V> lessThan(V value) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.LessThan, this, value);
    }

    protected <V> ScalarProperty<Q, V> getScalarExpressionInstance(String propertyName) {
        return new ScalarProperty<Q, V>(this, propertyName);
    }

    protected DomainEntityProperty<Q, V> getDomainObjectExpressionInstance(String propertyName) {
        return new DomainEntityProperty<Q, V>(this, propertyName);
    }

    public IsNull<Q, V> isNull() {
        return new IsNull<Q,V>(this);
    }

    public IsNotNull<Q, V> isNotNull() {
        return new IsNotNull<Q,V>(this);
    }

    public Between<Q, V> between(V first, V last) {
        return new Between<Q, V>(this, first, last);
    }

    public In<Q, V> in(V... valueList) {
        return new In<Q, V>(this, valueList);
    }

    public SortableProperty<Q, V> Descending() {
        isDescending = true;
        return this;
    }

    public boolean isDescending() {
        return isDescending;
    }

    private boolean isDescending = false;
    private boolean isImplementedAsCollection = false;

    public boolean isImplementedAsCollection() {
        return isImplementedAsCollection;
    }

    public void setImplementedAsCollection(boolean pImplementedAsCollection) {
        isImplementedAsCollection = pImplementedAsCollection;
    }

    @Override
    public String getPropertyName() {
        if (isImplementedAsCollection()) {
            return super.getPropertyName() + "Set";
        } else {
            return super.getPropertyName();
        }
    }
}