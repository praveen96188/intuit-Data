package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 11/2/11
 * Time: 9:20 AM
 */
public class SAPQBDTTokens {
    private String highToken;
    private String payrollTxNextId;
    private String paycheckNextId;
    private String employeeNextId;
    private String payrollItemNextId;

    public String getHighToken() {
        return highToken;
    }

    public void setHighToken(String highToken) {
        this.highToken = highToken;
    }

    public String getPayrollTxNextId() {
        return payrollTxNextId;
    }

    public void setPayrollTxNextId(String payrollTxNextId) {
        this.payrollTxNextId = payrollTxNextId;
    }

    public String getPaycheckNextId() {
        return paycheckNextId;
    }

    public void setPaycheckNextId(String paycheckNextId) {
        this.paycheckNextId = paycheckNextId;
    }

    public String getEmployeeNextId() {
        return employeeNextId;
    }

    public void setEmployeeNextId(String employeeNextId) {
        this.employeeNextId = employeeNextId;
    }

    public String getPayrollItemNextId() {
        return payrollItemNextId;
    }

    public void setPayrollItemNextId(String payrollItemNextId) {
        this.payrollItemNextId = payrollItemNextId;
    }
}
