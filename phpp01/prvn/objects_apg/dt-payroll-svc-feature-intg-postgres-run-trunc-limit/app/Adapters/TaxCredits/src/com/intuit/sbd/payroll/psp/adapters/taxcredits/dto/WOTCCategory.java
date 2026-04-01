package com.intuit.sbd.payroll.psp.adapters.taxcredits.dto;

/**
 * User: dweinberg
 * Date: Jan 28, 2010
 * Time: 1:23:15 PM
 */
public class WOTCCategory {
    private String category;
    private double taxRate0;
    private double taxRate1;
    private double taxRate2;
    private double wageBase;
    private double maxCredit;

    //transient
    private String documentationFileName;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTaxRate0() {
        return taxRate0;
    }

    public void setTaxRate0(double taxRate0) {
        this.taxRate0 = taxRate0;
    }

    public double getTaxRate1() {
        return taxRate1;
    }

    public void setTaxRate1(double taxRate1) {
        this.taxRate1 = taxRate1;
    }

    public double getTaxRate2() {
        return taxRate2;
    }

    public void setTaxRate2(double taxRate2) {
        this.taxRate2 = taxRate2;
    }

    public double getWageBase() {
        return wageBase;
    }

    public void setWageBase(double wageBase) {
        this.wageBase = wageBase;
    }

    public double getMaxCredit() {
        return maxCredit;
    }

    public void setMaxCredit(double maxCredit) {
        this.maxCredit = maxCredit;
    }

    public String getDocumentationName() {
        return documentationFileName;
    }

    public void setDocumentationFileName(String documentationFileName) {
        this.documentationFileName = documentationFileName;
    }
}
