package com.intuit.sbd.payroll.psp.query.clauses;

/**
 * User: achaves
 * Date: Dec 4, 2008
 * Time: 8:19:49 PM
 */
public abstract class LimitResultsClause<Q> extends ReadOnlyClause<Q> {
    public abstract ReadOnlyClause<Q> LimitResults(int FirstResults, int pMaxResults);
}
