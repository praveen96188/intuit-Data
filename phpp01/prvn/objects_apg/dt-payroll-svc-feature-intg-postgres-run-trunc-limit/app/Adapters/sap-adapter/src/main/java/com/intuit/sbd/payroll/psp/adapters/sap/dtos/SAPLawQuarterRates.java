package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.List;

/**
 * User: dweinberg
 * Date: 2/25/13
 * Time: 3:34 PM
 */
public class SAPLawQuarterRates {
    private SAPLawItem law;
    private List<SAPQuarterRate> rates;

    public SAPLawItem getLaw() {
        return law;
    }

    public void setLaw(SAPLawItem pLaw) {
        law = pLaw;
    }

    public List<SAPQuarterRate> getRates() {
        return rates;
    }

    public void setRates(List<SAPQuarterRate> pRates) {
        rates = pRates;
    }
}
