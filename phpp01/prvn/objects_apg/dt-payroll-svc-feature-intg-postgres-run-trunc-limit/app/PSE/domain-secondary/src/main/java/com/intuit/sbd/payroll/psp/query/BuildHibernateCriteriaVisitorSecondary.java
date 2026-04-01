package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.domainsecondary.ApplicationSecondary;
import com.intuit.sbd.payroll.psp.hibernate.criterion.CustomRestriction;
import com.intuit.sbd.payroll.psp.query.logicaloperators.And;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Not;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Or;
import com.intuit.sbd.payroll.psp.query.propertyoperators.*;
import com.intuit.sbd.payroll.psp.query.propertyoperators.SubqueryExpression;
import edu.emory.mathcs.backport.java.util.Arrays;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.*;
import org.hibernate.criterion.Criterion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BuildHibernateCriteriaVisitorSecondary<Q> extends ExpressionVisitor<Q, Criterion> {
    public BuildHibernateCriteriaVisitorSecondary(Expression pExpression) {
        super(pExpression);
    }

    private Criteria criteria;
    private DetachedCriteria detachedCriteria;
    private int detachedDepth;
    private String rootAlias = "this";

    private Set<String> eagerlyFilteredCollections = new HashSet<String>();

    public Criteria visit(Class persistentClass) {
        criteria = ApplicationSecondary.getHibernateSession().createCriteria(persistentClass);

        Criterion criterion = visit();
        if (criterion != null) {
            criteria.add(criterion);
        }

        //normal aliases (eager aliases are created in visitQuery)
        for (Map.Entry<String, String> entry : propertyPathToAlias.entrySet()) {
            if (!eagerlyFilteredCollections.contains(entry.getKey())) {
                criteria.createAlias(entry.getKey(), entry.getValue());
            }
        }

        return criteria;
    }

    public DetachedCriteria visitDetached(Class persistentClass, BuildHibernateCriteriaVisitorSecondary parentVisitor) {
        detachedDepth = parentVisitor.detachedDepth + 1;
        rootAlias = "d" + detachedDepth;
        detachedCriteria = DetachedCriteria.forClass(persistentClass, rootAlias);

        Criterion criterion = visit();
        if (criterion != null) {
            detachedCriteria.add(criterion);
        }

        for (Map.Entry<String, String> entry : propertyPathToAlias.entrySet()) {
            detachedCriteria.createAlias(entry.getKey(), entry.getValue());
        }

        return detachedCriteria;
    }

    @Override
    public Criterion visitAndExpression(And<?> andExpr) {
        Criterion leftExpressionCriterion = andExpr.getLeftExpression().accept(this);
        Criterion rightExpressionCriterion = andExpr.getRightExpression().accept(this);
        return Restrictions.and(leftExpressionCriterion, rightExpressionCriterion);
    }

    @Override
    public Criterion visitOrExpression(Or<?> orExpr) {
        Criterion leftExpressionCriterion = orExpr.getLeftExpression().accept(this);
        Criterion rightExpressionCriterion = orExpr.getRightExpression().accept(this);
        return Restrictions.or(leftExpressionCriterion, rightExpressionCriterion);
    }

    @Override
    public Criterion visitNotExpression(Not<?> notExpr) {
        return Restrictions.not(notExpr.getExpression().accept(this));
    }

    @Override
    public Criterion visitPropertyComparisonExpression(PropertyComparisonExpression<?, ?> propertyComparisonExpr) {
        String leftAlias = propertyComparisonExpr.getLeft().getAlias(propertyPathToAlias, rootAlias);
        if (propertyComparisonExpr.getPropertyRight() != null) {
            String rightAlias = propertyComparisonExpr.getPropertyRight().getAlias(propertyPathToAlias, rootAlias);
            switch (propertyComparisonExpr.getComparisonType()) {
                case GreaterOrEqualThan:
                    return Restrictions.geProperty(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), rightAlias + "." + propertyComparisonExpr.getPropertyRight().getPropertyQueryPath());
                case GreaterThan:
                    return Restrictions.gtProperty(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), rightAlias + "." + propertyComparisonExpr.getPropertyRight().getPropertyQueryPath());
                case LessOrEqualThan:
                    return Restrictions.leProperty(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), rightAlias + "." + propertyComparisonExpr.getPropertyRight().getPropertyQueryPath());
                case LessThan:
                    return Restrictions.ltProperty(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), rightAlias + "." + propertyComparisonExpr.getPropertyRight().getPropertyQueryPath());
                case Equal:
                    return Restrictions.eqProperty(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), rightAlias + "." + propertyComparisonExpr.getPropertyRight().getPropertyQueryPath());
                case NotEqual:
                    return Restrictions.neProperty(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), rightAlias + "." + propertyComparisonExpr.getPropertyRight().getPropertyQueryPath());
                default:
                    throw new RuntimeException("Unexpected comparison type");
            }
        } else {
            switch (propertyComparisonExpr.getComparisonType()) {
                case GreaterOrEqualThan:
                    return Restrictions.ge(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), propertyComparisonExpr.getRight());
                case GreaterThan:
                    return Restrictions.gt(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), propertyComparisonExpr.getRight());
                case LessOrEqualThan:
                    return Restrictions.le(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), propertyComparisonExpr.getRight());
                case LessThan:
                    return Restrictions.lt(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), propertyComparisonExpr.getRight());
                case Equal:
                    return Restrictions.eq(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), propertyComparisonExpr.getRight());
                case NotEqual:
                    return Restrictions.ne(leftAlias + "." + propertyComparisonExpr.getLeft().getPropertyQueryPath(), propertyComparisonExpr.getRight());
                default:
                    throw new RuntimeException("Unexpected comparison type");
            }
        }
    }

    @Override
    public Criterion visitBetweenExpression(Between<?, ?> betweenExpr) {
        return Restrictions.between(betweenExpr.getProperty().getAlias(propertyPathToAlias, rootAlias) + "." + betweenExpr.getProperty().getPropertyName(), betweenExpr.getFirst(), betweenExpr.getLast());
    }

    @Override
    public Criterion visitInExpression(In<?, ?> inExpr) {
        //oracle limitation of 1000 elements in an IN list.  Replace with an OR if greater.
        Object[] valueList = inExpr.getValueList();
        Criterion orCriteria = null;
        int first = 0;
        do {
            Object[] valueListPart = Arrays.copyOfRange(valueList, first, Math.min(first + 1000, valueList.length));
            Criterion partCriteria = Restrictions.in(inExpr.getProperty().getAlias(propertyPathToAlias, rootAlias) + "." + inExpr.getProperty().getPropertyName(), valueListPart);
            if (orCriteria == null) {
                orCriteria = partCriteria;
            } else {
                orCriteria = Restrictions.or(orCriteria, partCriteria);
            }

            first += 1000;
        } while (first < valueList.length);

        if (valueList.length > 1000) {
            Application.getLogger(getClass()).warn("IN clause replaced with OR/IN: " + orCriteria.toString());
        }

        return orCriteria;
    }

    @Override
    public Criterion visitNotInExpression(NotIn<?, ?> notInExpr) {
        return Restrictions.not(Restrictions.in(notInExpr.getProperty().getAlias(propertyPathToAlias, rootAlias) + "." + notInExpr.getProperty().getPropertyName(), notInExpr.getValueList()));
    }

    @Override
    public Criterion visitLikeExpression(Like<?, ?> likeExpr) {
        if (likeExpr.isCaseInsensitive()) {
            return Restrictions.ilike(likeExpr.getProperty().getAlias(propertyPathToAlias, rootAlias) + "." + likeExpr.getProperty().getPropertyName(), likeExpr.getLikeString());
        } else {
            return Restrictions.like(likeExpr.getProperty().getAlias(propertyPathToAlias, rootAlias) + "." + likeExpr.getProperty().getPropertyName(), likeExpr.getLikeString());
        }
    }

    @Override
    public Criterion visitRegexpLikeExpression(RegexpLike<?, ?> regexpLikeExpr) {
        return CustomRestriction.sqlRestriction("regexp_like({" + regexpLikeExpr.getProperty().getPropertyName() + "}, '" + regexpLikeExpr.getRegexpLikeString() + "')", regexpLikeExpr.getProperty().getPropertyName());
    }

    @Override
    public Criterion visitIsNullExpression(IsNull<?, ?> isNullExpr) {
        return Restrictions.isNull(isNullExpr.getProperty().getAlias(propertyPathToAlias, rootAlias) + "." + isNullExpr.getProperty().getPropertyName());
    }

    @Override
    public Criterion visitIsNotNullExpression(IsNotNull<?, ?> isNotNullExpr) {
        return Restrictions.isNotNull(isNotNullExpr.getProperty().getAlias(propertyPathToAlias, rootAlias) + "." + isNotNullExpr.getProperty().getPropertyName());
    }

    @Override
    public Criterion visitEmptyExpression(EmptyCriterion<?> query) {
        return Restrictions.sqlRestriction("1=1");
    }

    @Override
    public Criterion visitSubqueryExpression(SubqueryExpression<?, ?> subqueryExpression) {
        BuildHibernateCriteriaVisitorSecondary visitor = new BuildHibernateCriteriaVisitorSecondary((Expression) subqueryExpression.getRight());
        DetachedCriteria detachedCriteria = visitor.visitDetached(subqueryExpression.getSubqueryClass(), this);

        String subqueryPath = detachedCriteria.getAlias() + "." + subqueryExpression.getParentProperty() + ".id";
        String parentPath;
        if (subqueryExpression.getParentDomainEntityExpression() != null) {
            parentPath = subqueryExpression.getParentDomainEntityExpression().getAlias(propertyPathToAlias, rootAlias) + "." + subqueryExpression.getParentDomainEntityExpression().getPropertyName();
        } else {
            parentPath = getCriteriaAlias();
        }
        parentPath += ".id";
        detachedCriteria.add(Restrictions.eqProperty(subqueryPath, parentPath));
        detachedCriteria.setProjection(Projections.id());

        switch (subqueryExpression.getSubqueryType()) {
            case Exists:
                return Subqueries.exists(detachedCriteria);
            case NotExists:
                return Subqueries.notExists(detachedCriteria);
            default:
                throw new RuntimeException("Unexpected subquery type");
        }
    }

    private String getCriteriaAlias() {
        return criteria != null ? criteria.getAlias() : detachedCriteria.getAlias();
    }

    @Override
    public Criterion visitPropertyExpression(Property<?, ?> pProperty) {
        throw new RuntimeException("PropertyExpression does not support evaluate visitor");
    }

    @Override
    public Criterion visitQueryExpression(Query<?> query) {
        // OrderBy
        for (SortableProperty orderByProperty : query.getOrderByProperties()) {
            Property<? super Q, ?> property = (Property<? super Q, ?>) orderByProperty;
            String qualifiedPropertyName = getQualifiedPropertyName(property);
            criteria.addOrder(orderByProperty.isDescending() ?
                    Order.desc(qualifiedPropertyName) :
                    Order.asc(qualifiedPropertyName));
        }


        ProjectionList projectionList = null;
        if (query.getGroupByProperties().size() > 0 ||
                query.getSelectProperties().size() > 0) {
            projectionList = Projections.projectionList();
        }

        // GroupBy
        for (ScalarProperty groupByProperty : query.getGroupByProperties()) {
            String qualifiedPropertyName = getQualifiedPropertyName(groupByProperty);
            projectionList.add(Projections.groupProperty(qualifiedPropertyName));
        }

        // Select
        for (Property selectProperty : query.getSelectProperties()) {
            String qualifiedPropertyName = getQualifiedPropertyName(selectProperty);

            ScalarProperty.AggregateType aggType = ScalarProperty.AggregateType.NotAggregated;
            if (selectProperty instanceof ScalarProperty) {
                aggType =((ScalarProperty) selectProperty).getAggregateType();
            }

            switch (aggType) {
                case Avg:
                    projectionList.add(Projections.avg(qualifiedPropertyName));
                    break;
                case Count:
                    projectionList.add(Projections.count(qualifiedPropertyName));
                    break;
                case Max:
                    projectionList.add(Projections.max(qualifiedPropertyName));
                    break;
                case Min:
                    projectionList.add(Projections.min(qualifiedPropertyName));
                    break;
                case Sum:
                    projectionList.add(Projections.sum(qualifiedPropertyName));
                    break;
                case Distinct:
                    projectionList.add(Projections.distinct(Projections.property(qualifiedPropertyName)));
                    break;
                case NotAggregated:
                    projectionList.add(Projections.property(qualifiedPropertyName));
                    break;
                case CountDistinct:
                    projectionList.add(Projections.countDistinct(qualifiedPropertyName));
                    break;
            }

        }

        if (projectionList != null) {
            criteria.setProjection(projectionList);
        }

        // EagerLoad
        for (Property eagerLoadPath : query.getEagerLoadPaths()) {
            criteria.setFetchMode(eagerLoadPath.getPropertyPath(), FetchMode.JOIN);
        }

        for (com.intuit.sbd.payroll.psp.query.Criterion<?> eagerLoadCriterion : query.getEagerLoadCriteria()) {
            String parentProperty = eagerLoadCriterion.getParentProperty();
            eagerlyFilteredCollections.add(parentProperty);
            Criterion withClause = eagerLoadCriterion.accept(this);  //magic side effects on propertyPathToAlias
            criteria.createAlias(parentProperty, propertyPathToAlias.get(parentProperty), CriteriaSpecification.LEFT_JOIN, withClause);
        }

        // LimitResults
        if (query.getMaxResults() != -1) {
            criteria.setMaxResults(query.getMaxResults());
        }
        if (query.getFirstResult() != -1) {
            criteria.setFirstResult(query.getFirstResult());
        }

        // ReadOnly
        if (query.getReadOnly() != null) {
            criteria.setReadOnly(query.getReadOnly());
        }

        if (query.getFilterExpression() != null) {
            return query.getFilterExpression().accept(this);
        }
        else {
            return null;
        }
    }

    private <Q> String getQualifiedPropertyName(Property<Q, ?> property) {
        return property.getAlias(propertyPathToAlias, rootAlias) + "." + property.getPropertyName();
    }

    private HashMap<String, String> propertyPathToAlias = new HashMap<String, String>();

    public Set<String> getEagerlyFilteredCollections() {
        return eagerlyFilteredCollections;
    }

    public HashMap<String, String> getPropertyPathToAlias() {
        return propertyPathToAlias;
    }
}
