package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 17, 2009
 * Time: 8:09:10 AM
 */
public class SAPEmployeeTaxLedgerItem {
    private String employeeName;
    private String socialSecurityNumber;
    private double totalWages;
    private double taxableWages;
    private double taxAmount;
    private double taxTips;
    private boolean mShowTaxTips;

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String socialSecurityNumber) {
        this.socialSecurityNumber = socialSecurityNumber;
    }

    public double getTotalWages() {
        return totalWages;
    }

    public void setTotalWages(double totalWages) {
        this.totalWages = totalWages;
    }

    public double getTaxableWages() {
        return taxableWages;
    }

    public void setTaxableWages(double taxableWages) {
        this.taxableWages = taxableWages;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTaxTips() {
        return taxTips;
    }

    public void setTaxTips(double pTaxTips) {
        taxTips = pTaxTips;
    }

    public boolean getShowTaxTips() {
        return mShowTaxTips;
    }

    public void setShowTaxTips(boolean pShowTaxTips) {
        mShowTaxTips = pShowTaxTips;
    }
}
