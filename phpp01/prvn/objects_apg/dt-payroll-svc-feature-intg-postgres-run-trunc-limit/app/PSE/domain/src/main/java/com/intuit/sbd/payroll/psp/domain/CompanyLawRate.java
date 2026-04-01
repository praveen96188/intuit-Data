package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class CompanyLawRate extends BaseCompanyLawRate {

    private static final int BEGINNING_DAY_OF_BLACKOUT_PERIOD = 13;
    private static final int MAX_SUNSET_RATE_ENTRIES = 4;

    /**
     * Default constructor.
     */
    public CompanyLawRate() {
        super();
    }

    /**
     * Return the cache key
     *
     */
    public NaturalKey getNaturalRateDatesKey() {
        return getNaturalRateDatesKey(getCompanyLaw(), getRate(), getEffectiveDate(), getInvalidDate());
    }

    /**
     * Cache Key for this entity
     *
     * @param pCompanyLaw    company law associated with this rate
     * @param pRate          Rate
     * @param pEffectiveDate Effective Date
     * @return Cache Key
     */
    public static NaturalKey getNaturalRateDatesKey(CompanyLaw pCompanyLaw, double pRate, SpcfCalendar pEffectiveDate, SpcfCalendar pInvalidDate) {
        Object[] keys = new Object[4];
        keys[0] = pCompanyLaw.getId();
        keys[1] = pRate;
        keys[2] = (pEffectiveDate == null) ? "NULL_EFFECTIVE_DATE" : pEffectiveDate;
        keys[3] = (pInvalidDate == null) ? "NULL_INVALID_DATE" : pInvalidDate;
        return new NaturalKey(Company.class, keys);
    }

    public void cache() {
        Application.getSessionCache().addPrimaryKey(getNaturalRateDatesKey(), getId());
    }

    /**
     * Find a valid (invalid date is not null) company law rate entity from DB/cache
     *
     * @param pCompanyLaw     Company Law Entity
     * @param pRate           Rate
     * @param pEffectiveDate  Effective Date
     * @return CompanyLawRate entity
     */
    public static CompanyLawRate findValidCompanyLawRateByRateAndEffectiveDates(CompanyLaw pCompanyLaw, double pRate, SpcfCalendar pEffectiveDate, SpcfCalendar pInvalidDate) {
        NaturalKey naturalKey = getNaturalRateDatesKey(pCompanyLaw, pRate, pEffectiveDate, pInvalidDate);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);
        CompanyLawRate companyLawRate = null;

        if (primaryKey == null) {
            Expression<CompanyLawRate> query = new Query<CompanyLawRate>()
                    .Where(CompanyLawRate.CompanyLaw().equalTo(pCompanyLaw)
                                         .And(CompanyLawRate.Rate().equalTo(pRate))
                                         .And(CompanyLawRate.EffectiveDate().equalTo(pEffectiveDate))
                                         .And(CompanyLawRate.InvalidDate().equalTo(pInvalidDate)));
            DomainEntitySet<CompanyLawRate> currentRates = Application.find(CompanyLawRate.class, query);
            if (!currentRates.isEmpty()) {
                companyLawRate = currentRates.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, companyLawRate.getId());
            }
        } else {
            companyLawRate = Application.findById(CompanyLawRate.class, primaryKey);
        }

        return companyLawRate;
    }

    public static CompanyLawRate findEffectiveLawRate(CompanyLaw pCompanyLaw, SpcfCalendar pEffectiveAsOf) {
        return Application.find(CompanyLawRate.class, new Query<CompanyLawRate>()
                .Where(CompanyLawRate.CompanyLaw().equalTo(pCompanyLaw)
                                     .And(CompanyLawRate.EffectiveDate().lessOrEqualThan(pEffectiveAsOf))
                                     .And(CompanyLawRate.InvalidDate().isNull()))
                .OrderBy(CompanyLawRate.EffectiveDate().Descending())).getFirst();
    }

    public boolean isExpiredAsOf(SpcfCalendar asOfDate) {
        SpcfCalendar expirationDate = calculateExpirationDate();
        return expirationDate != null && expirationDate.before(asOfDate);
    }

    public SpcfCalendar calculateExpirationDate() {
        CompanyLawRate laterRate = getCompanyLaw().getCompanyLawRateCollection()
                .find(CompanyLawRate.InvalidDate().isNull()
                                    .And(CompanyLawRate.EffectiveDate().greaterThan(getEffectiveDate())))
                .sort(CompanyLawRate.EffectiveDate())
                .getFirst();

        if (laterRate == null) {
            return null;
        }
        SpcfCalendar expirationDate = laterRate.getEffectiveDate().copy();
        expirationDate.addDays(-1);
        return expirationDate;
    }
}
