package com.intuit.sbd.payroll.psp.adapters.qbdtws.ui;

/**
 * User: rnorian
 * Date: Mar 16, 2010
 * Time: 10:42:39 PM
 */
public class PaycheckValidationMessage extends ValidationMessage {
    private String checkDate;
    private String netAmount;
    private String employeeName;

    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }

    public String getNetAmount() {
        return netAmount;
    }

    public void setNetAmount(String netAmount) {
        this.netAmount = netAmount;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

}
