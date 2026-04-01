package com.intuit.sbd.payroll.psp.query.clauses;

import com.intuit.sbd.payroll.psp.query.Expression;

/**
 * User: dweinberg
 * Date: 1/8/13
 * Time: 2:56 PM
 */
public abstract class ReadOnlyClause<Q> extends Expression<Q> {
    public abstract Expression<Q> ReadOnly(boolean pReadOnly);
}
