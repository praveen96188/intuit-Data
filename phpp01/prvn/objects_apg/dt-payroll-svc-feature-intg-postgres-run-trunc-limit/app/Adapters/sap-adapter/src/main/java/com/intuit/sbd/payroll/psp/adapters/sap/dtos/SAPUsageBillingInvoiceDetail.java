package com.intuit.sbd.payroll.psp.adapters.sap.dtos;


/**
 * User: vidhyak689
 * Date: 8/20/12
 * Time: 1:18 PM
 */
public class SAPUsageBillingInvoiceDetail {
    private String billPOID = null;
    private String payrollItemChargeId = null;
    private double subscriptionFee = 0.0d;
    private double employeeFee = 0.0d;
    private double credit = 0.0d;
    private double total = 0.0d;
    private int numEmployessPaidPrevMonth = 0;
    private Boolean isPayrollItem;

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

    public String getBillPOID() {
        return billPOID;
    }

    public void setBillPOID(String pBillPOID) {
        billPOID = pBillPOID;
    }

    public Boolean getIsPayrollItem() {
        return isPayrollItem;
    }

    public void setIsPayrollItem(Boolean pPayrollItem) {
        isPayrollItem = pPayrollItem;
    }

    public String getPayrollItemChargeId() {
        return payrollItemChargeId;
    }

    public void setPayrollItemChargeId(String pPayrollItemChargeId) {
        payrollItemChargeId = pPayrollItemChargeId;
    }
}
