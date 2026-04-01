package com.intuit.sbd.payroll.psp.query.logicaloperators;

import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.DomainEntityProperty;
import org.apache.commons.lang.NotImplementedException;

import java.util.Objects;

/**
 * User: dweinberg
 * Date: 1/9/13
 * Time: 10:56 AM
 */
abstract class BinaryOperator<Q> extends Criterion<Q> {
    protected Criterion<Q> leftExpression;
    protected Criterion<Q> rightExpression;

    public Criterion<Q> getLeftExpression() {
        return leftExpression;
    }

    void setLeftExpression(Criterion<Q> pLeftExpression) {
        leftExpression = pLeftExpression;
    }

    public Criterion<Q> getRightExpression() {
        return rightExpression;
    }

    void setRightExpression(Criterion<Q> pRightExpression) {
        rightExpression = pRightExpression;
    }

    public BinaryOperator(Criterion<Q> pLeftExpression, Criterion<Q> pRightExpression) {
        setLeftExpression(pLeftExpression);
        setRightExpression(pRightExpression);
    }

    @Override
    public String getParentProperty() {
        String leftProperty = leftExpression.getParentProperty();
        String rightProperty = rightExpression.getParentProperty();
        if (leftProperty == null) {
            return rightProperty;
        }
        if (rightProperty == null) {
            return leftProperty;
        }
        if (!leftProperty.equals(rightProperty)) {
            throw new RuntimeException("Both expressions must have same parent property");
        }
        return leftProperty;
    }

    @Override
    public DomainEntityProperty getParentDomainObjectExpression(){
        DomainEntityProperty leftProperty = leftExpression.getParentDomainObjectExpression();
        DomainEntityProperty rightProperty = rightExpression.getParentDomainObjectExpression();

        if(Objects.isNull(leftProperty)){
            return rightProperty;
        }

        if(Objects.isNull(rightProperty)){
            return leftProperty;
        }
        if (!leftProperty.getPropertyName().equals(rightProperty.getPropertyName())) {
            throw new RuntimeException("Both expressions must have same parent property");
        }

        return leftProperty;
    }
}
