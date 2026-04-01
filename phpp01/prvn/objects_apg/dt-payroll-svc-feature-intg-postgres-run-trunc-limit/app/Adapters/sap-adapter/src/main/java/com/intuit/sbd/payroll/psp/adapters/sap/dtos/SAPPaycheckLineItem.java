package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dhaddan
 * Date: Mar 15, 2010
 * Time: 1:55:03 PM
 */
public class SAPPaycheckLineItem {
    private String lineItemGseq;
    private String payrollItemCategory;
    private String payrollItemType;
    private String sourcePayrollItemName;
    private double amount;

    public String getLineItemGseq() {
        return lineItemGseq;
    }

    public void setLineItemGseq(String lineItemGseq) {
        this.lineItemGseq = lineItemGseq;
    }

    public String getPayrollItemCategory() {
        return payrollItemCategory;
    }

    public void setPayrollItemCategory(String payrollItemCategory) {
        this.payrollItemCategory = payrollItemCategory;
    }

    public String getPayrollItemType() {
        return payrollItemType;
    }

    public void setPayrollItemType(String payrollItemType) {
        this.payrollItemType = payrollItemType;
    }

    public String getSourcePayrollItemName() {
        return sourcePayrollItemName;
    }

    public void setSourcePayrollItemName(String sourcePayrollItemName) {
        this.sourcePayrollItemName = sourcePayrollItemName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
