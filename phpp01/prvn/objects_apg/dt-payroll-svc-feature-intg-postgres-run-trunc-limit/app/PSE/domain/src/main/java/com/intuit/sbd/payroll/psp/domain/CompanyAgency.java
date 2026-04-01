package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.cache.NaturalKey;
import com.intuit.sbd.payroll.psp.query.Expression;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.SpcfUniqueId;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

/**
 * Hand-written business logic
 */
public class CompanyAgency extends BaseCompanyAgency {
    public NaturalKey getNaturalKey(Company pCompany) {
        return new NaturalKey(CompanyAgency.class, pCompany.getId(), getAgency().getAgencyId());
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Finders/counters
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static CompanyAgency findCompanyAgency(Company pCompany, String pAgencyId) {
        return findCompanyAgency(pCompany.getSourceSystemCd(), pCompany.getSourceCompanyId(), pAgencyId);
    }

    public static CompanyAgency findCompanyAgency(SourceSystemCode pSourceSystem, String pCompanyId, String pAgencyId) {
        CompanyAgency companyAgency = null;
        Company company = Company.findCompany(pCompanyId, pSourceSystem);
        //SpcfCalendar todayDate = PSPDate.getPSPTime();

        if (company == null) {
            return null;
        }

        NaturalKey naturalKey = new NaturalKey(CompanyAgency.class, company.getId(), pAgencyId);
        SpcfUniqueId primaryKey = Application.getSessionCache().getPrimaryKey(naturalKey);

        if (primaryKey != null) {
            companyAgency = Application.findById(CompanyAgency.class, primaryKey);
        }

        // todo when we start supporting multiple company agencies we need to uncomment the code below
        // todo and use a different date than today
        /*if (primaryKey != null) {
            companyAgency = Application.findById(CompanyAgency.class, primaryKey);
            // invalidate cache if responsibility date is not valid
            SpcfCalendar intuitResponsibilityStartDate = companyAgency.getIntuitResponsibilityStartDate();
            SpcfCalendar intuitResponsibilityEndDate = companyAgency.getIntuitResponsibilityEndDate();

            CalendarUtils.clearTime(todayDate);
            CalendarUtils.clearTime(intuitResponsibilityStartDate);
            if (intuitResponsibilityEndDate != null) {
                CalendarUtils.clearTime(intuitResponsibilityEndDate);
            }
            if (intuitResponsibilityStartDate.compareTo(todayDate) == 1 ||
                    (intuitResponsibilityEndDate != null && intuitResponsibilityEndDate.compareTo(todayDate) == -1)) {
                Application.getSessionCache().removeEntity(CompanyAgency.class, primaryKey.toString(), companyAgency);
                companyAgency = null;
            }
        }*/

        if (companyAgency == null) {
            DomainEntitySet<CompanyAgency> companyAgencies = Application.find(CompanyAgency.class, new Query<CompanyAgency>()
                    .Where(CompanyAgency.Company().equalTo(company)
                            .And(CompanyAgency.Agency().AgencyId().equalTo(pAgencyId))));
            /*.And(CompanyAgency.IntuitResponsibilityStartDate().lessOrEqualThan(todayDate))
            .And(CompanyAgency.IntuitResponsibilityEndDate().isNull().Or(CompanyAgency.IntuitResponsibilityEndDate().greaterOrEqualThan(todayDate)))));*/

            if (companyAgencies.size() > 1) {
                throw new RuntimeException(
                        "Query for company agency for company " + pSourceSystem + ":" + pCompanyId + " and agency " + pAgencyId + " did not return 0 or 1 results as expected");
            }

            if (!companyAgencies.isEmpty()) {
                companyAgency = companyAgencies.get(0);
                Application.getSessionCache().addPrimaryKey(naturalKey, companyAgency.getId());
        }
    }

        return companyAgency;
    }

    public CompanyAgencyFormTemplate getFilingForm(PaymentTemplate pPaymentTemplate) {
        return getCompanyAgencyFormTemplateCollection()
                .find(CompanyAgencyFormTemplate.InvalidDate().isNull()
                                               .And(CompanyAgencyFormTemplate.FormTemplate().PaymentTemplate().equalTo(pPaymentTemplate)))
                .sort(CompanyAgencyFormTemplate.EffectiveDate().Descending())
                .getFirst();
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////

    public CompanyAgency() {
        super();
    }


    public static CompanyAgency addCompanyAgency(Company pCompany, String pAgencyId, SpcfCalendar pEffectiveDate) {
        return addCompanyAgency(pCompany, pAgencyId, pEffectiveDate, true);
    }
    /**
     * Adds a CompanyAgency
     *
     * @param pCompany
     * @param pAgencyId
     * @param pEffectiveDate
     * @return
     */
    public static CompanyAgency addCompanyAgency(Company pCompany, String pAgencyId, SpcfCalendar pEffectiveDate, boolean addCompanyAgencyPaymentTemplates) {
        CompanyAgency companyAgency =
                CompanyAgency.findCompanyAgency(pCompany, pAgencyId);
        if (companyAgency == null) {
            Agency agency = Application.findById(Agency.class, pAgencyId);
            if (agency == null) {
                throw new RuntimeException("unable to find agency " + pAgencyId);
            }
            companyAgency = new CompanyAgency();

            // set start date to quarter start date
            companyAgency.setIntuitResponsibilityStartDate(CalendarUtils.getFirstDayOfQuarter(pEffectiveDate));

            // manage relationships
            pCompany.addCompanyAgency(companyAgency);
            companyAgency.setCompany(pCompany);
            companyAgency.setAgency(agency);

            companyAgency = Application.save(companyAgency);
            Application.getSessionCache().addPrimaryKey(companyAgency.getNaturalKey(pCompany), companyAgency.getId());

            if (addCompanyAgencyPaymentTemplates) {
                companyAgency.createCompanyPaymentTemplate();
            }

        }
        return companyAgency;
    }

    public void createCompanyPaymentTemplate() {
        Agency agency = getAgency();

        if (agency == null) {
            return;
        }

        DomainEntitySet<PaymentTemplate> paymentTemplates = agency.getPaymentTemplateCollection();
        for (PaymentTemplate paymentTemplate : paymentTemplates) {

            CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.findCompanyAgencyPaymentTemplate(this, paymentTemplate);
            if (companyAgencyPaymentTemplate == null) {
                companyAgencyPaymentTemplate = CompanyAgencyPaymentTemplate.createNewCompanyAgencyPaymentTemplate(paymentTemplate, this, null);
                companyAgencyPaymentTemplate.createEffectiveDepositFrequency();
            }

        }
    }

    public DomainEntitySet<CompanyAgencyFormTemplate> findValidFormTemplatesForCompanyAgency() {
        Expression<CompanyAgencyFormTemplate> query = new Query<CompanyAgencyFormTemplate>()
                .Where(CompanyAgencyFormTemplate.CompanyAgency().equalTo(this)
                        .And(CompanyAgencyFormTemplate.InvalidDate().isNull()));
        return Application.find(CompanyAgencyFormTemplate.class, query);
    }

    public void recalculatePaymentMethods() {
        for (CompanyAgencyPaymentTemplate companyAgencyPaymentTemplate : getCompanyAgencyPaymentTemplateCollection()) {
            companyAgencyPaymentTemplate.recalculatePaymentMethods();
        }
    }

}
