package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 6, 2009
 * Time: 4:30:18 PM
 */
public class SAPPaymentTemplateQuarterPayment {
    private String quarter;
    private String year;
    private String paymentTemplateName;
    private String paymentTemplateCd;
    private double pendingPaymentsTotal;
    private double paymentsMadeTotal;
    private double quarterPaymentsTotal;
    private boolean notStarted;
    private ArrayList<SAPPayment> pendingPayments;
    private ArrayList<SAPPayment> paymentsMade;

    public String getQuarter() {
        return quarter;
    }

    public void setQuarter(String quarter) {
        this.quarter = quarter;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getPaymentTemplateName() {
        return paymentTemplateName;
    }

    public void setPaymentTemplateName(String paymentTemplateName) {
        this.paymentTemplateName = paymentTemplateName;
    }

    public String getPaymentTemplateCd() {
        return paymentTemplateCd;
    }

    public void setPaymentTemplateCd(String paymentTemplateCd) {
        this.paymentTemplateCd = paymentTemplateCd;
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

    public double getQuarterPaymentsTotal() {
        return quarterPaymentsTotal;
    }

    public void setQuarterPaymentsTotal(double quarterPaymentsTotal) {
        this.quarterPaymentsTotal = quarterPaymentsTotal;
    }

    public boolean getNotStarted() {
        return notStarted;
    }

    public void setNotStarted(boolean notStarted) {
        this.notStarted = notStarted;
    }

    public ArrayList<SAPPayment> getPendingPayments() {
        return pendingPayments;
    }

    public void setPendingPayments(ArrayList<SAPPayment> pendingPayments) {
        this.pendingPayments = pendingPayments;
    }

    public ArrayList<SAPPayment> getPaymentsMade() {
        return paymentsMade;
    }

    public void setPaymentsMade(ArrayList<SAPPayment> paymentsMade) {
        this.paymentsMade = paymentsMade;
    }
}
