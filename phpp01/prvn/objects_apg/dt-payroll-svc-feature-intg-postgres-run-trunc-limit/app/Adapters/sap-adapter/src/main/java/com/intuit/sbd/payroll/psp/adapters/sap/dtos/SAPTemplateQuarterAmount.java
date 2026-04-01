package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 12/5/12
 * Time: 4:02 PM
 */
public class SAPTemplateQuarterAmount {
    private String paymentTemplateCd;
    private SAPQuarter quarter;
    private double amount;
    private boolean isAnnual;

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String pPaymentTemplateCd) {
        paymentTemplateCd = pPaymentTemplateCd;
    }

    public SAPQuarter getQuarter() {
        return quarter;
    }

    public void setQuarter(SAPQuarter pQuarter) {
        quarter = pQuarter;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double pAmount) {
        amount = pAmount;
    }

    public boolean getIsAnnual() {
        return isAnnual;
    }

    public void setIsAnnual(boolean pAnnual) {
        isAnnual = pAnnual;
    }
}
