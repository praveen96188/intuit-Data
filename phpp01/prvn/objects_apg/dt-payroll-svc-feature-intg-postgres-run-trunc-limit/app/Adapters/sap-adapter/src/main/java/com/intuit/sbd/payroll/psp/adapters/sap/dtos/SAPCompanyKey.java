package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.Company;

/**
 * User: dweinberg
 * Date: Jul 13, 2009
 * Time: 9:46:14 AM
 */
public class SAPCompanyKey {
    private String sourceSystemCd;
	private String companyId;

    public SAPCompanyKey() {
    }

    public SAPCompanyKey(Company company) {
        this.companyId = company.getSourceCompanyId();
        this.sourceSystemCd = company.getSourceSystemCd().toString();
    }

    public SAPCompanyKey(String sourceSystemCd, String companyId) {
        this.sourceSystemCd = sourceSystemCd;
        this.companyId = companyId;
    }

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String sourceSystemCd) {
        this.sourceSystemCd = sourceSystemCd;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
}
