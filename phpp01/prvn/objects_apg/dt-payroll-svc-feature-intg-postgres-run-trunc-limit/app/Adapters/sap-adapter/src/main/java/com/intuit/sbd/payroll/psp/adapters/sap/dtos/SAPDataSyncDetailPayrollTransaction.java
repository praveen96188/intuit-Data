package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 11/5/11
 * Time: 9:36 PM
 */
public class SAPDataSyncDetailPayrollTransaction extends SAPDataSyncDetail {
    private int payrollTransactionId;
    private String payrollTransactionType;
    private String employeeId;
    private String employeeName;
    private Date transactionDate;
    private double amount;
    private boolean isQBOnly;

    public int getPayrollTransactionId() {
        return payrollTransactionId;
    }

    public void setPayrollTransactionId(int payrollTransactionId) {
        this.payrollTransactionId = payrollTransactionId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        employeeId = pEmployeeId;
    }

    public String getPayrollTransactionType() {
        return payrollTransactionType;
    }

    public void setPayrollTransactionType(String payrollTransactionType) {
        this.payrollTransactionType = payrollTransactionType;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public boolean getIsQBOnly() {
        return isQBOnly;
    }

    public void setIsQBOnly(boolean QBOnly) {
        isQBOnly = QBOnly;
    }
}
