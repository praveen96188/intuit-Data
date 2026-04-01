package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 6, 2009
 * Time: 4:28:51 PM
 */
public class SAPPaymentTemplateYearPayment {
    private ArrayList<SAPPaymentTemplateQuarterPayment> templateQuarterPayments;
    private double pendingPaymentsTotal;
    private double paymentsMadeTotal;
    private double yearPaymentsTotal;
    private String paymentTemplateCd;
    private String taxYear;

    public ArrayList<SAPPaymentTemplateQuarterPayment> getTemplateQuarterPayments() {
        return templateQuarterPayments;
    }

    public void setTemplateQuarterPayments(ArrayList<SAPPaymentTemplateQuarterPayment> templateQuarterPayments) {
        this.templateQuarterPayments = templateQuarterPayments;
    }

    public double getPendingPaymentsTotal() {
        return pendingPaymentsTotal;
    }

    public void setPendingPaymentsTotal(double pendingPaymentsTotal) {
        this.pendingPaymentsTotal = pendingPaymentsTotal;
    }

    public double getPaymentsMadeTotal() {
        return paymentsMadeTotal;
    }

    public void setPaymentsMadeTotal(double paymentsMadeTotal) {
        this.paymentsMadeTotal = paymentsMadeTotal;
    }

    public double getYearPaymentsTotal() {
        return yearPaymentsTotal;
    }

    public void setYearPaymentsTotal(double yearPaymentsTotal) {
        this.yearPaymentsTotal = yearPaymentsTotal;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
    }

    public String getTaxYear() {
        return taxYear;
    }

    public void setTaxYear(String taxYear) {
        this.taxYear = taxYear;
    }
}
