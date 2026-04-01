package com.intuit.sbd.payroll.psp.query.clauses;

import com.intuit.sbd.payroll.psp.query.Criterion;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 4, 2008
 * Time: 8:18:16 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class WhereClause<Q> extends GroupByClause<Q> {
    public abstract GroupByClause<Q> Where(Criterion<? super Q> pCriterion);
}