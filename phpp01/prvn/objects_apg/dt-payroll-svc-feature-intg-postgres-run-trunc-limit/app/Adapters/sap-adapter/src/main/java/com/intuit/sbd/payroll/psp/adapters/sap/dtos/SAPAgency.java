package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: cyoder
 * Date: May 6, 2009
 * Time: 3:36:09 PM
 */
public class SAPAgency {

    private String agencyId;
    private String agencyAbbrev;
    private String agencyName;
    private ArrayList<SAPPaymentTemplate> paymentTemplates;

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    public ArrayList<SAPPaymentTemplate> getPaymentTemplates() {
        return paymentTemplates;
    }

    public void setPaymentTemplates(ArrayList<SAPPaymentTemplate> paymentTemplates) {
        this.paymentTemplates = paymentTemplates;
    }

    public String getAgencyAbbrev() {
        return agencyAbbrev;
    }

    public void setAgencyAbbrev(String pAgencyAbbrev) {
        agencyAbbrev = pAgencyAbbrev;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public void setAgencyName(String pAgencyName) {
        agencyName = pAgencyName;
    }
}
