package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 11/5/11
 * Time: 9:38 PM
 */
public class SAPDataSyncDetailPaycheck extends SAPDataSyncDetail {
    private int paycheckId;
    private String employeeId;
    private String employeeName;
    private Date checkDate;
    private String checkNumber;
    private double amount;
    private String paycheckType;
    public int getPaycheckId() {
        return paycheckId;
    }

    public void setPaycheckId(int paycheckId) {
        this.paycheckId = paycheckId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        employeeId = pEmployeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(Date checkDate) {
        this.checkDate = checkDate;
    }

    public String getCheckNumber() {
        return checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setPaycheckType(String paycheckType) {
        this.paycheckType = paycheckType;
    }

    public String getPaycheckType() {
        return paycheckType;
    }
}
