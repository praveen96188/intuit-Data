package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.query.Criterion;

/**
 * Hand-written business logic
 */
public class GemsLedgerPostingRule extends BaseGemsLedgerPostingRule {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    public GemsLedgerPostingRule() {
        super();
    }

    public static GemsLedgerPostingRule findGemsLedgerPostingRule(LedgerAccountCode pLedgerAccountCode, ReportingType pReportingType) {

        Criterion expr = LedgerAccount().LedgerAccountCd().equalTo(pLedgerAccountCode);
        if (pReportingType != null) {
            expr = expr.And(ReportingType().equalTo(pReportingType));
        }
        DomainEntitySet<GemsLedgerPostingRule> gemsLedgerPostingRules = Application.findObjects(GemsLedgerPostingRule.class).find(expr);

        if (gemsLedgerPostingRules.size() > 1) {
            Application.getLogger(GemsLedgerPostingRule.class).error(String.format("Found more than one GemsLedgerPostingRule are found for Service %s, LedgerAccountCd: %s.",
                    pReportingType != null ? pReportingType.toString() : "NULL", pLedgerAccountCode.toString()));
        }

        return gemsLedgerPostingRules.getFirst();
    }

    /**
     * Function to identify service type - Company is decided as Assisted, there is Tax service in ActiveCurrent. Otherwise it is DD if one of DD service is in ActiveCurrent.
     * If no active services, check for latest cancelled Tax and other DD services if status effective date is more than 2 minutes apart. If within 2 minutes, consider as Assisted.
     *
     * @param pCompany
     * @return ServiceType
     */
    public static ReportingType getGemsAccountServiceType(Company pCompany) {
        CompanyOffering companyOffering = pCompany.getOffering(ServiceCode.DirectDeposit);
        if(companyOffering != null && companyOffering.getOffering() != null) {
            return companyOffering.getOffering().getReportingType();
        }
        return null;
    }

}

