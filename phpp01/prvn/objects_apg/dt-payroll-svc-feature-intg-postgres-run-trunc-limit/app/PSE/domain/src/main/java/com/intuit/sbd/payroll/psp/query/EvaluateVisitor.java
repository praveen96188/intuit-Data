package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DataObject;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.logicaloperators.And;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Not;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Or;
import com.intuit.sbd.payroll.psp.query.propertyoperators.*;
import com.intuit.sbd.payroll.psp.util.DomainReflectionHelper;
import org.hibernate.metadata.ClassMetadata;

import java.util.regex.Pattern;

public class EvaluateVisitor<Q> extends ExpressionVisitor<Q, Boolean> {
    public EvaluateVisitor(Criterion pCriterion) {
        super(pCriterion);
    }

    public EvaluateVisitor(Query pQuery) {
        super(pQuery.getFilterExpression());
    }

    public Boolean visit(DataObject pPersistentObject) {
        classMetadata = Application.getHibernateClassMetadata(Application.getActualObject(pPersistentObject).getClass());
        persistentObject = pPersistentObject;
        return super.visit();
    }

    private Comparable EvaluateProperty(Property pProperty) {
        return pProperty.getPropertyValue(classMetadata, persistentObject);
    }


    @Override
    public Boolean visitAndExpression(And<?> andExpr) {
        return andExpr.getLeftExpression().accept(this) && andExpr.getRightExpression().accept(this);
    }

    @Override
    public Boolean visitOrExpression(Or<?> orExpr) {
        return orExpr.getLeftExpression().accept(this) || orExpr.getRightExpression().accept(this);    }

    @Override
    public Boolean visitNotExpression(Not<?> notExpr) {
        return !notExpr.getExpression().accept(this);
    }

    @Override
    public Boolean visitPropertyComparisonExpression(PropertyComparisonExpression<?, ?> propertyComparisonExpr) {
        Comparable LValue = EvaluateProperty(propertyComparisonExpr.getLeft());
        Object RValue = propertyComparisonExpr.getRight();

        switch (propertyComparisonExpr.getComparisonType()) {
            case GreaterOrEqualThan:
                if (LValue == null || RValue == null) {
                    return LValue == RValue;
                }
                else {
                    return LValue.compareTo(RValue) >= 0;
                }

            case GreaterThan:
                if (LValue == null || RValue == null) {
                    return false;
                }
                else {
                    return LValue.compareTo(RValue) > 0;
                }

            case LessOrEqualThan:
                if (LValue == null || RValue == null) {
                    return LValue == RValue;
                }
                else {
                    return LValue.compareTo(RValue) <= 0;
                }

            case LessThan:
                if (LValue == null || RValue == null) {
                    return false;
                }
                else {
                    return LValue.compareTo(RValue) < 0;
                }

            case Equal:
                if (LValue == null || RValue == null) {
                    return LValue == RValue;
                }
                else {
                    return LValue.compareTo(RValue) == 0;
                }

            case NotEqual:
                if (LValue == null || RValue == null) {
                    return LValue != RValue;
                }
                else {
                    return LValue.compareTo(RValue) != 0;
                }

            default:
                throw new RuntimeException("Unexpected comparison type");
        }
    }

    @Override
    public Boolean visitBetweenExpression(Between<?, ?> betweenExpr) {
        Comparable LValue = EvaluateProperty(betweenExpr.getProperty());
        if (LValue == null) return false;

        return LValue.compareTo(betweenExpr.getFirst()) >= 0 && LValue.compareTo(betweenExpr.getLast()) <= 0;
    }

    @Override
    public Boolean visitInExpression(In<?, ?> inExpr) {
        Comparable LValue = EvaluateProperty(inExpr.getProperty());
        if (LValue == null) return false;

        for (Object RValue : inExpr.getValueList()) {
            if (LValue.compareTo(RValue) == 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Boolean visitNotInExpression(NotIn<?,?> notInExpr) {
        Comparable LValue = EvaluateProperty(notInExpr.getProperty());
        if (LValue == null) return false;

        for (Object RValue : notInExpr.getValueList()) {
            if (LValue.compareTo(RValue) == 0) {
                return false;
            }
        }
        return true;        
    }

    @Override
    public Boolean visitLikeExpression(Like<?, ?> likeExpr) {
        String LValue = (String) EvaluateProperty(likeExpr.getProperty());
        if (LValue == null) return false;

        String LikeExpr = "^" + likeExpr.getLikeString().replace(".", "\\.").replace("%", ".*?").replace("_", ".") + "$";
        return Pattern.matches(LikeExpr, LValue);  //TODO: case insensitive?
    }

    @Override
    public Boolean visitRegexpLikeExpression(RegexpLike<?, ?> regexpLikeExpr) {
        String LValue = (String) EvaluateProperty(regexpLikeExpr.getProperty());
        return LValue != null && Pattern.matches(regexpLikeExpr.getRegexpLikeString(), LValue);
    }

    @Override
    public Boolean visitIsNullExpression(IsNull<?, ?> isNullExpr) {
        Object value = EvaluateProperty(isNullExpr.getProperty());
        return value == null;
    }

    @Override
    public Boolean visitIsNotNullExpression(IsNotNull<?, ?> isNotNullExpr) {
        Object value = EvaluateProperty(isNotNullExpr.getProperty());
        return value != null;
    }

    @Override
    public Boolean visitEmptyExpression(EmptyCriterion<?> query) {
        return true;
    }

    @Override
    public Boolean visitSubqueryExpression(SubqueryExpression<?, ?> subqueryExpression) {
        DomainEntitySet domainEntitySet = DomainReflectionHelper.getCollection(persistentObject, subqueryExpression.getProperty().getPropertyName());
        return domainEntitySet.find((Criterion) subqueryExpression.getRight()).isNotEmpty();
    }

    @Override
    public Boolean visitPropertyExpression(Property<?, ?> pProperty) {
        throw new RuntimeException("PropertyExpression does not support evaluate visitor");
    }

    @Override
    public Boolean visitQueryExpression(Query<?> query) {
        return query.getFilterExpression().accept(this);
    }

    private DataObject persistentObject;
    private ClassMetadata classMetadata;
}
