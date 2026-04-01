package com.intuit.sbd.payroll.psp.query.clauses;

import com.intuit.sbd.payroll.psp.query.SortableProperty;

/**
 * Created by IntelliJ IDEA.
 * User: achaves
 * Date: Dec 4, 2008
 * Time: 8:30:31 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class OrderByClause<Q> extends EagerLoadClause<Q> {
    public abstract EagerLoadClause<Q> OrderBy(SortableProperty<? super Q, ?>... pOrderByProperties);
}
