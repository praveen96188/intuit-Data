package com.intuit.sbd.payroll.psp.query.propertyoperators;

import com.intuit.sbd.payroll.psp.query.Property;
import com.intuit.sbd.payroll.psp.query.ExpressionVisitor;

/**
 * Property comparisons: ==, !=, <, <=, >, >=
 */
public class PropertyComparisonExpression<Q, V> extends PropertyOperator<Q, V> {
    public enum ComparisonType {
        Equal,
        NotEqual,
        GreaterThan,
        GreaterOrEqualThan,
        LessThan,
        LessOrEqualThan
    };

    public PropertyComparisonExpression(ComparisonType pComparisonType, Property<Q, V> pLeft, V pRight) {
        super(pLeft);
        comparisonType = pComparisonType;
        right = pRight;
    }

    public PropertyComparisonExpression(ComparisonType pComparisonType, Property<Q, V> pLeft, Property<Q, V> pPropertyRight) {
        super(pLeft);
        comparisonType = pComparisonType;
        propertyRight = pPropertyRight;
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitPropertyComparisonExpression(this);
    }

    public ComparisonType getComparisonType() {
        return comparisonType;
    }

    public void setComparisonType(ComparisonType comparisonType) {
        this.comparisonType = comparisonType;
    }

    public Property<Q, V> getLeft() {
        return getProperty();
    }

    public V getRight() {
        return right;
    }

    public void setRight(V right) {
        this.right = right;
    }

    public Property<Q, V> getPropertyRight() {
        return propertyRight;
    }

    public void setPropertyRight(Property<Q, V> pPropertyRight) {
        propertyRight = pPropertyRight;
    }

    private V right;
    private Property<Q, V> propertyRight;
    private ComparisonType comparisonType;
}
