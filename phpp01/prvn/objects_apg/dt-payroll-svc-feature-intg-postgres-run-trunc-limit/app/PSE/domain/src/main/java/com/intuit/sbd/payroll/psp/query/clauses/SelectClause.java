package com.intuit.sbd.payroll.psp.query.clauses;

import com.intuit.sbd.payroll.psp.query.Property;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 4, 2008
 * Time: 8:18:16 PM
 */
public abstract class SelectClause<Q> extends WhereClause<Q> {
    public abstract WhereClause<Q> Select(Property<? super Q, ?>... pSelectProperties);

    public abstract WhereClause<Q> QueryHint(String queryHint);
}
