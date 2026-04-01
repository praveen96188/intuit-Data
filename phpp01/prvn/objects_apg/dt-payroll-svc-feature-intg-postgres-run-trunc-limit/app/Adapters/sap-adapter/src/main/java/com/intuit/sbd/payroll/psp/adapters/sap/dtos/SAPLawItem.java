package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: May 16, 2009
 * Time: 7:43:30 PM
 */
public class SAPLawItem {
    private String name;
    private String lawId;
    private String description;
    private String paymentTemplateCd;

    private boolean negativeLiability;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLawId() {
        return lawId;
    }

    public void setLawId(String lawId) {
        this.lawId = lawId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getNegativeLiability() {
        return negativeLiability;
    }

    public void setNegativeLiability(boolean negativeLiability) {
        this.negativeLiability = negativeLiability;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }
}
