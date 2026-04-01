package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.List;

/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 2:09 PM
 */
public class SAPQuarterLawRates {
    private SAPQuarter quarter;
    private boolean underBlackout;

    private List<SAPLawRate> lawRates;

    @SuppressWarnings("UnusedDeclaration")
    public SAPQuarterLawRates() {
    }

    public SAPQuarterLawRates(SAPQuarter pQuarter) {
        quarter = pQuarter;
        lawRates = new ArrayList<SAPLawRate>();
    }

    public SAPQuarter getQuarter() {
        return quarter;
    }

    public void setQuarter(SAPQuarter pQuarter) {
        quarter = pQuarter;
    }

    public boolean isUnderBlackout() {
        return underBlackout;
    }

    public void setUnderBlackout(boolean pUnderBlackout) {
        underBlackout = pUnderBlackout;
    }

    public List<SAPLawRate> getLawRates() {
        return lawRates;
    }

    public void setLawRates(List<SAPLawRate> pLawRates) {
        lawRates = pLawRates;
    }
}
