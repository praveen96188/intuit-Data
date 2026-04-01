package com.intuit.sbd.payroll.psp.query;

import com.intuit.sbd.payroll.psp.query.logicaloperators.And;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Not;
import com.intuit.sbd.payroll.psp.query.logicaloperators.Or;

/**
 * User: achaves
 * Date: Nov 30, 2008
 * Time: 9:01:20 PM
 */
public abstract class Criterion<Q> extends Expression<Q>  {
    public Criterion<Q> And(Criterion<? super Q> pAndExpression) {
        return new And(this, pAndExpression);
    }

    public Criterion<Q> Or(Criterion<? super Q> pOrExpression) {
        return new Or(this, pOrExpression);
    }    

    public Criterion<Q> Not() {
        return new Not(this);
    }
}

