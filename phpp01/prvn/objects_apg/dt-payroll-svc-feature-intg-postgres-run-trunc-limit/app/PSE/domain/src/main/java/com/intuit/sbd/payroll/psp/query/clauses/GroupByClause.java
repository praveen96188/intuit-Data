package com.intuit.sbd.payroll.psp.query.clauses;

import com.intuit.sbd.payroll.psp.query.clauses.OrderByClause;
import com.intuit.sbd.payroll.psp.query.ScalarProperty;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 4, 2008
 * Time: 8:17:26 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class GroupByClause<Q> extends OrderByClause<Q> {
    public abstract OrderByClause<Q> GroupBy(ScalarProperty<? super Q, ?>... pGroupByProperties);
}