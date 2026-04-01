package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;

/**
 * Hand-written business logic
 */
public class WageLimit extends BaseWageLimit {

    static String cacheKey = "CachedWageLimits";

    /**
     * Default constructor.
     */
    public WageLimit() {
        super();
    }

    public static WageLimit findWageLimitAmount(int pYear, int pQuarter, String pLawId) {

        DomainEntitySet<WageLimit> wageLimits;
        if (Application.getSessionCache().isDataObjectCollectionCached(WageLimit.class, cacheKey)) {
            wageLimits = Application.getSessionCache().getDataObjectCollection(WageLimit.class, cacheKey);
        } else {
            wageLimits = Application.findObjects(WageLimit.class);
            Application.getSessionCache().addDataObjectCollection(WageLimit.class, cacheKey, wageLimits);
        }

        String yearQuarter = String.valueOf(pYear) + String.valueOf(pQuarter);
        wageLimits = wageLimits.find(WageLimit.Law().LawId().equalTo(pLawId).And(WageLimit.EffectiveYearQuarter().equalTo(yearQuarter)));

        if (wageLimits.size() > 1) {
            throw new RuntimeException("More than one wage limit is found for EffectiveYearQuarter:" + yearQuarter + " Law Id:" + pLawId);
        }

        return wageLimits.getFirst();
    }


}