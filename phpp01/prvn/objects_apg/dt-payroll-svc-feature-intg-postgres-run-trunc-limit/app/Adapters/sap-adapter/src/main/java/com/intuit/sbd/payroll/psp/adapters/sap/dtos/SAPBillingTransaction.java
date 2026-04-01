package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 31, 2008
 * Time: 4:46:19 PM
 */
public class SAPBillingTransaction {
    private String financialTxnId;
    private double financialAmount;
    private String financialTxnType;
    private double financialReturnAmount;

    private String salesTaxTxnId;
    private double salesTaxAmount;
    private double salesTaxReturnAmount;

    public String getFinancialTxnId() {
        return financialTxnId;
    }

    public void setFinancialTxnId(String financialTxnId) {
        this.financialTxnId = financialTxnId;
    }

    public double getFinancialAmount() {
        return financialAmount;
    }

    public void setFinancialAmount(double financialAmount) {
        this.financialAmount = financialAmount;
    }

    public String getFinancialTxnType() {
        return financialTxnType;
    }

    public void setFinancialTxnType(String financialTxnType) {
        this.financialTxnType = financialTxnType;
    }

    public double getFinancialReturnAmount() {
        return financialReturnAmount;
    }

    public void setFinancialReturnAmount(double financialReturnAmount) {
        this.financialReturnAmount = financialReturnAmount;
    }

    public String getSalesTaxTxnId() {
        return salesTaxTxnId;
    }

    public void setSalesTaxTxnId(String salesTaxTxnId) {
        this.salesTaxTxnId = salesTaxTxnId;
    }

    public double getSalesTaxAmount() {
        return salesTaxAmount;
    }

    public void setSalesTaxAmount(double salesTaxAmount) {
        this.salesTaxAmount = salesTaxAmount;
    }

    public double getSalesTaxReturnAmount() {
        return salesTaxReturnAmount;
    }

    public void setSalesTaxReturnAmount(double salesTaxReturnAmount) {
        this.salesTaxReturnAmount = salesTaxReturnAmount;
    }
}
