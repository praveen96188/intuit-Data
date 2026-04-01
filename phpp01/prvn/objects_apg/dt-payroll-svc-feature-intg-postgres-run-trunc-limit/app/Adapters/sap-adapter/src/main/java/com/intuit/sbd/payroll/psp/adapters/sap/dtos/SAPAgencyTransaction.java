package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Mar 16, 2009
 * Time: 3:08:50 PM
 */
public class SAPAgencyTransaction extends SAPPayrollTransaction {
    private String taxDescription;
    private String taxAbbreviation;
    private String agencyName;
    private String agencyAbbreviation;

    public String getTaxDescription() {
        return taxDescription;
    }

    public void setTaxDescription(String taxDescription) {
        this.taxDescription = taxDescription;
    }

    public String getTaxAbbreviation() {
        return taxAbbreviation;
    }

    public void setTaxAbbreviation(String taxAbbreviation) {
        this.taxAbbreviation = taxAbbreviation;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String agencyName) {
        this.agencyName = agencyName;
    }

    public String getAgencyAbbreviation() {
        return agencyAbbreviation;
    }

    public void setAgencyAbbreviation(String agencyAbbreviation) {
        this.agencyAbbreviation = agencyAbbreviation;
    }
}
