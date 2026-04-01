package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dweinberg
 * Date: 3/8/13
 * Time: 9:30 AM
 */
public class SAPQuarterCompanyFilingAmounts {
    private SAPQuarter quarter;
    private List<SAPCompanyFilingAmount> amounts;

    @SuppressWarnings("UnusedDeclaration")
    public SAPQuarterCompanyFilingAmounts() {
    }

    public SAPQuarterCompanyFilingAmounts(SAPQuarter pQuarter) {
        quarter = pQuarter;
        amounts = new ArrayList<SAPCompanyFilingAmount>();
    }

    public SAPQuarter getQuarter() {
        return quarter;
    }

    public void setQuarter(SAPQuarter pQuarter) {
        quarter = pQuarter;
    }

    public List<SAPCompanyFilingAmount> getAmounts() {
        return amounts;
    }

    public void setAmounts(List<SAPCompanyFilingAmount> pAmounts) {
        amounts = pAmounts;
    }
}
