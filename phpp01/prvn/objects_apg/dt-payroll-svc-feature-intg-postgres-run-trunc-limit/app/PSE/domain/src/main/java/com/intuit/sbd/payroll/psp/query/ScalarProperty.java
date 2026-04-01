package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.query.propertyoperators.*;

import java.util.Collection;

/**
 * User: achaves
 * Date: Nov 30, 2008
 * Time: 5:55:42 PM
 */
public class ScalarProperty<Q, V> extends Property<Q, V> implements SortableProperty<Q, V> {
    public ScalarProperty(DomainEntityProperty<Q, ?> parentDataEntityExpression, String propertyName) {
        super(parentDataEntityExpression, propertyName);
    }

    public PropertyComparisonExpression<Q, V> equalTo(V value) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.Equal, this, value);
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

    public PropertyComparisonExpression<Q, V> equalTo(Property<Q, V> propertyValue) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.Equal, this, propertyValue);
    }

    public PropertyComparisonExpression<Q, V> notEqualTo(Property<Q, V> propertyValue) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.NotEqual, this, propertyValue);
    }

    public PropertyComparisonExpression<Q, V> greaterOrEqualThan(Property<Q, V> propertyValue) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.GreaterOrEqualThan, this, propertyValue);
    }

    public PropertyComparisonExpression<Q, V> greaterThan(Property<Q, V> propertyValue) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.GreaterThan, this, propertyValue);
    }

    public PropertyComparisonExpression<Q, V> lessOrEqualThan(Property<Q, V> propertyValue) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.LessOrEqualThan, this, propertyValue);
    }

    public PropertyComparisonExpression<Q, V> lessThan(Property<Q, V> propertyValue) {
        return new PropertyComparisonExpression<Q, V>(PropertyComparisonExpression.ComparisonType.LessThan, this, propertyValue);
    }

    public IsNull<Q, V> isNull() {
        return new IsNull<Q, V>(this);
    }

    public IsNotNull<Q, V> isNotNull() {
        return new IsNotNull<Q, V>(this);
    }

    public Between<Q, V> between(V first, V last) {
        return new Between<Q, V>(this, first, last);
    }

    public Like<Q, V> like(String likeString, boolean caseInsensitive) {
        return new Like<Q, V>(this, likeString, caseInsensitive);
    }

    public Like<Q, V> like(String likeString) {
        return new Like<Q, V>(this, likeString, true);
    }

    public RegexpLike<Q, V> regexpLike(String regexpLikeString) {
        return new RegexpLike<Q, V>(this, regexpLikeString);
    }

    public In<Q, V> in(V... valueList) {
        return new In<Q, V>(this, valueList);
    }

    public In<Q, V> in(Collection<V> valueList) {
        //noinspection unchecked
        return in((V[]) valueList.toArray());
    }

    public NotIn<Q, V> notIn(V... valueList) {
        return new NotIn<Q, V>(this, valueList);
    }

    public ScalarProperty<Q, V> Max() {
        aggregateType = AggregateType.Max;
        return this;
    }

    public ScalarProperty<Q, V> Min() {
        aggregateType = AggregateType.Min;
        return this;
    }

    public ScalarProperty<Q, V> Avg() {
        aggregateType = AggregateType.Avg;
        return this;
    }

    public ScalarProperty<Q, V> Count() {
        aggregateType = AggregateType.Count;
        return this;
    }

    public ScalarProperty<Q, V> Sum() {
        aggregateType = AggregateType.Sum;
        return this;
    }

    public ScalarProperty<Q, V> Distinct() {
        aggregateType = AggregateType.Distinct;
        return this;
    }

    public ScalarProperty<Q, V> CountDistinct() {
        aggregateType = AggregateType.CountDistinct;
        return this;
    }

    public SortableProperty<Q, V> Descending() {
        isDescending = true;
        return this;
    }

    AggregateType getAggregateType() {
        return aggregateType;
    }

    enum AggregateType {
        Max,
        Min,
        Avg,
        Count,
        Sum,
        Distinct,
        NotAggregated,
        CountDistinct
    }

    public boolean isDescending() {
        return isDescending;
    }

    private AggregateType aggregateType = AggregateType.NotAggregated;
    private boolean isDescending = false;
}
