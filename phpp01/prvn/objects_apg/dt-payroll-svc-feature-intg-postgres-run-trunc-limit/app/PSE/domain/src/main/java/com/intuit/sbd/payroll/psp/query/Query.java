package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.query.clauses.*;
import org.apache.commons.lang.NotImplementedException;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

/**
 * User: achaves
 * Date: Dec 4, 2008
 * Time: 11:46:39 PM
 */
public class Query<Q> extends SelectClause<Q> {
    // Select
     public WhereClause<Q> Select(Property<? super Q, ?>... pSelectProperties) {
         Collections.addAll(selectProperties, pSelectProperties);
         return this;
     }

    private final List<String> queryHints = new ArrayList<>();

    @Override
    public WhereClause<Q> QueryHint(String queryHint) {
        queryHints.add(queryHint);
        return this;
    }

    public List<String> getQueryHints() {
        return queryHints;
    }

    private Set<Property<? super Q, ?>> selectProperties = new LinkedHashSet<Property<? super Q, ?>>();

     Set<Property<? super Q, ?>> getSelectProperties() {
         return selectProperties;
     }

    // Where
    public GroupByClause<Q> Where(Criterion<? super Q> pCriterion) {
        criterion = pCriterion;
        return this;
    }

    private Criterion<? super Q> criterion;

    Criterion<? super Q> getFilterExpression() {
        return criterion;
    }

    // GroupBy
    public OrderByClause<Q> GroupBy(ScalarProperty<? super Q, ?>... pGroupByProperties) {
        Collections.addAll(orderByProperties, pGroupByProperties);

        return this;
    }

    private Set<ScalarProperty<? super Q, ?>> groupByProperties = new LinkedHashSet<ScalarProperty<? super Q, ?>>();

    Set<ScalarProperty<? super Q, ?>> getGroupByProperties() {
        return groupByProperties;
    }

    // OrderBy
    public EagerLoadClause<Q> OrderBy(SortableProperty<? super Q, ?>... pOrderByProperties) {
        Collections.addAll(orderByProperties, pOrderByProperties);
        return this;
    }

    private Set<SortableProperty<? super Q, ?>> orderByProperties = new LinkedHashSet<SortableProperty<? super Q, ?>>();

    Set<SortableProperty<? super Q, ?>> getOrderByProperties() {
        return orderByProperties;
    }

     // EagerLoad
    public LimitResultsClause<Q> EagerLoad(Property<? super Q, ?>... pEagerLoadPaths) {
        Collections.addAll(eagerLoadPaths, pEagerLoadPaths);

        return this;
    }

    @Override
    public EagerLoadClause<Q> EagerLoad(Criterion<Q> pEagerLoadCriteria) {
        eagerLoadCriteria.add(pEagerLoadCriteria);
        return this;
    }

    private Set<Property<? super Q, ?>> eagerLoadPaths = new LinkedHashSet<Property<? super Q, ?>>();

    Set<Property<? super Q, ?>> getEagerLoadPaths() {
        return eagerLoadPaths;
    }

    private Set<Criterion<Q>> eagerLoadCriteria = new LinkedHashSet<Criterion<Q>>();

    public Set<Criterion<Q>> getEagerLoadCriteria() {
        return eagerLoadCriteria;
    }

    // LimitResults
    public ReadOnlyClause<Q> LimitResults(int pFirstResult, int pMaxResults) {
        if (pFirstResult != -1 || pMaxResults != -1) {
            String eagerLimitError = "Limit Results cannot be used when eagerly loading a collection";
            for (Criterion<Q> qCriterion : getEagerLoadCriteria())
                if (qCriterion.getParentProperty().endsWith("Set")){
                    throw new RuntimeException(eagerLimitError);
                }
            for (Property<? super Q, ?> property : getEagerLoadPaths()) {
                if (property instanceof DomainEntitySetProperty) {
                    throw new RuntimeException(eagerLimitError);
                }
            }
        }

        maxResults = pMaxResults;
        firstResult = pFirstResult;
        return this;
    }

    private int maxResults = -1;
    private int firstResult = -1;

    int getMaxResults() {
        return maxResults;
    }

    int getFirstResult() {
        return firstResult;
    }

    private Boolean readOnly = null;

    public Boolean getReadOnly() {
        return readOnly;
    }

    @Override
    public Expression<Q> ReadOnly(boolean pReadOnly) {
        readOnly = pReadOnly;
        return this;
    }

    @Override
    protected <T> T accept(ExpressionVisitor<?, T> visitor) {
        return visitor.visitQueryExpression(this);
    }

    @Override
    public String getParentProperty() {
        return null;
    }

    @Override
    public DomainEntityProperty getParentDomainObjectExpression(){
        return null;
    }
}
