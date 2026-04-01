package com.intuit.sbd.payroll.psp.query.clauses;

import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Property;


public abstract class EagerLoadClause<Q> extends LimitResultsClause<Q> {
    public abstract LimitResultsClause<Q> EagerLoad(Property<? super Q, ?>... pEagerLoadPaths);
    public abstract EagerLoadClause<Q> EagerLoad(Criterion<Q> pEagerLoadExpression);

}
