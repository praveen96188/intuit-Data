package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: vidhyak689
 * Date: 8/20/12
 * Time: 1:18 PM
 */
public class SAPUsageBillingHistory {
    private String billId = null;
    private Date billingStartDate = null;
    private Date billingEndDate = null;
    private Date statementDate = null;

    private double subscriptionFee = 0.0d;
    private double employeeFee = 0.0d;
    private double credit = 0.0d;
    private double total = 0.0d;
    private int numEmployessPaidPrevMonth = 0;


    public Date getBillingStartDate() {
        return billingStartDate;
    }

    public void setBillingStartDate(Date billingStartDate) {
        this.billingStartDate = billingStartDate;
    }

    public Date getBillingEndDate() {
        return billingEndDate;
    }

    public void setBillingEndDate(Date billingEndDate) {
        this.billingEndDate = billingEndDate;
    }

    public Date getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(Date statementDate) {
        this.statementDate = statementDate;
    }

    public double getSubscriptionFee() {
        return subscriptionFee;
    }

    public void setSubscriptionFee(double subscriptionFee) {
        this.subscriptionFee = subscriptionFee;
    }

    public double getEmployeeFee() {
        return employeeFee;
    }

    public void setEmployeeFee(double employeeFee) {
        this.employeeFee = employeeFee;
    }

    public double getCredit() {
        return credit;
    }

    public void setCredit(double credit) {
        this.credit = credit;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public int getNumEmployessPaidPrevMonth() {
        return numEmployessPaidPrevMonth;
    }

    public void setNumEmployessPaidPrevMonth(int numEmployessPaidPrevMonth) {
        this.numEmployessPaidPrevMonth = numEmployessPaidPrevMonth;
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String pBillId) {
        billId = pBillId;
    }
}
