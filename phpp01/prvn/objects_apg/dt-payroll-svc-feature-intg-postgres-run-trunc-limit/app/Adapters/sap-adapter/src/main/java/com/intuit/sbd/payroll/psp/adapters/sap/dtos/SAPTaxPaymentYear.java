package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 6, 2009
 * Time: 7:07:21 AM
 */
public class SAPTaxPaymentYear {
    private String year;
    private ArrayList<SAPPaymentTemplate> paymentTemplates;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public ArrayList<SAPPaymentTemplate> getPaymentTemplates() {
        return paymentTemplates;
    }

    public void setPaymentTemplates(ArrayList<SAPPaymentTemplate> paymentTemplates) {
        this.paymentTemplates = paymentTemplates;
    }
}
