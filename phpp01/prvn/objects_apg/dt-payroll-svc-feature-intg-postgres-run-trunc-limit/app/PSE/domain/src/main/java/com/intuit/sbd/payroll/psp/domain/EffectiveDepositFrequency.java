package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class EffectiveDepositFrequency extends BaseEffectiveDepositFrequency {
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/Counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns a list of active deposit frequencies for a company / payment template
     *
     * @param pCompany
     * @param pPaymentTemplate
     * @param pEffectiveDate
     * @return
     */
    public static DomainEntitySet<EffectiveDepositFrequency> findEffectiveDepositFrequencies(Company pCompany,
                                                                                             PaymentTemplate pPaymentTemplate,
                                                                                             SpcfCalendar pEffectiveDate) {

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = findEffectiveDepositFrequencies(pCompany, pPaymentTemplate, pEffectiveDate, true);

        return effectiveFrequencies;
    }

    /**
     * Returns a list of deposit frequencies for a company / payment template
     *
     * @param pCompany
     * @param pPaymentTemplate
     * @param pEffectiveDate
     * @return
     */
    public static DomainEntitySet<EffectiveDepositFrequency> findEffectiveDepositFrequencies(Company pCompany,
                                                                                             PaymentTemplate pPaymentTemplate,
                                                                                             SpcfCalendar pEffectiveDate,
                                                                                             Boolean pActive) {

        Criterion<EffectiveDepositFrequency> edfCriteria = CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(pCompany)
                .And(CompanyAgencyPaymentTemplate().CompanyAgency().Agency().equalTo(pPaymentTemplate.getAgency()))
                .And(CompanyAgencyPaymentTemplate().PaymentTemplate().equalTo(pPaymentTemplate));

        if (pEffectiveDate != null) {
            CalendarUtils.clearTime(pEffectiveDate);
            edfCriteria = edfCriteria.And(EffectiveDate().greaterOrEqualThan(pEffectiveDate));
        }

        if (pActive != null) {
            if (pActive) {
                edfCriteria = edfCriteria.And(InvalidDate().isNull());
            } else {
                edfCriteria = edfCriteria.And(InvalidDate().isNotNull());
            }
        }
        Expression<EffectiveDepositFrequency> edfQuery =
                new Query<EffectiveDepositFrequency>()
                        .Where(edfCriteria)
                        .OrderBy(EffectiveDate().Descending());

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = Application.find(EffectiveDepositFrequency.class, edfQuery);

        return effectiveFrequencies;
    }

    /**
     * Returns the active deposit frequency for a specific date
     *
     * @param pCompany
     * @param pPaymentTemplate
     * @param pEffectiveDate
     * @return
     */
    public static EffectiveDepositFrequency findEffectiveDepositFrequencyAtDate(Company pCompany, PaymentTemplate pPaymentTemplate, SpcfCalendar pEffectiveDate) {

        Criterion<EffectiveDepositFrequency> edfCriteria = CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(pCompany)
                .And(CompanyAgencyPaymentTemplate().CompanyAgency().Agency().equalTo(pPaymentTemplate.getAgency()))
                .And(CompanyAgencyPaymentTemplate().PaymentTemplate().equalTo(pPaymentTemplate))
                .And(InvalidDate().isNull())
                .And(EffectiveDate().lessOrEqualThan(pEffectiveDate).Or(EffectiveDate().isNull()));

        Expression<EffectiveDepositFrequency> edfQuery =
                new Query<EffectiveDepositFrequency>()
                        .Where(edfCriteria)
                        .OrderBy(EffectiveDate().Descending());

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = Application.find(EffectiveDepositFrequency.class, edfQuery);
        if (effectiveFrequencies.size() > 0) {
            return effectiveFrequencies.get(0);
        }

        return null;
    }

    public static DomainEntitySet<EffectiveDepositFrequency> findEffectiveDepositFrequencyBefore(Company pCompany, PaymentTemplate pPaymentTemplate, SpcfCalendar pEffectiveDate) {
        CalendarUtils.clearTime(pEffectiveDate);
        Criterion<EffectiveDepositFrequency> edfCriteria = CompanyAgencyPaymentTemplate().CompanyAgency().Company().equalTo(pCompany)
                .And(InvalidDate().isNull())
                .And(CompanyAgencyPaymentTemplate().CompanyAgency().Agency().equalTo(pPaymentTemplate.getAgency()))
                .And(CompanyAgencyPaymentTemplate().PaymentTemplate().equalTo(pPaymentTemplate))
                .And(EffectiveDate().lessOrEqualThan(pEffectiveDate));

        Expression<EffectiveDepositFrequency> edfQuery =
                new Query<EffectiveDepositFrequency>()
                        .Where(edfCriteria)
                        .OrderBy(EffectiveDate());

        DomainEntitySet<EffectiveDepositFrequency> effectiveFrequencies = Application.find(EffectiveDepositFrequency.class, edfQuery);

        return effectiveFrequencies;
    }

    /**
     * @param pSourceSystemCode
     * @param pServiceCode
     * @return
     */

    public static DomainEntitySet<EffectiveDepositFrequency> findCompanyDepositFrequenciesBySourceSystemAndService(
            SourceSystemCode pSourceSystemCode, ServiceCode pServiceCode) {
        String[] paramNames = new String[2];
        paramNames[0] = "sourceSystemCd";
        paramNames[1] = "serviceCd";

        Object[] paramValues = new Object[2];
        paramValues[0] = pSourceSystemCode;
        paramValues[1] = pServiceCode;

        DomainEntitySet<EffectiveDepositFrequency> retList =
                Application.findByNamedQuery("findCompanyDepositFrequenciesBySourceSystemAndService", paramNames, paramValues);

        retList.find(InvalidDate().isNull());
        return retList;
    }

    /**
     * Returns the active deposit frequency for a specific date
     *
     * @param pPaymentTemplateFrequency
     * @param pEffectiveDate
     * @return
     */
    public static DomainEntitySet<EffectiveDepositFrequency> findCompaniesAndEffectiveDepositFrequencyAtDate(PaymentTemplateFrequency pPaymentTemplateFrequency, SpcfCalendar pEffectiveDate) {
        CalendarUtils.clearTime(pEffectiveDate);
        String[] params = new String[4];
        params[0] = "agency";
        params[1] = "paymentTemplate";
        params[2] = "paymentFrequencyId";
        params[3] = "effectiveDate";

        Object[] values = new Object[params.length];
        values[0] = pPaymentTemplateFrequency.getPaymentTemplate().getAgency();
        values[1] = pPaymentTemplateFrequency.getPaymentTemplate();
        values[2] = pPaymentTemplateFrequency.getPaymentFrequencyId();
        values[3] = pEffectiveDate;

        return Application.findByNamedQueryUsingCache(EffectiveDepositFrequency.class, "findCompaniesAndEffectiveDepositFrequencyAtDate", params, values);
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public EffectiveDepositFrequency() {
        super();
    }


}