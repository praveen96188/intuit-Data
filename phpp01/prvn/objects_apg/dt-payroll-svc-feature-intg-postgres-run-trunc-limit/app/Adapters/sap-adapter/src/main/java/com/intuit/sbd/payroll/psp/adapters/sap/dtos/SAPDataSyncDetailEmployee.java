package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: mwaqarbaig
 * Date: Oct 17, 2011
 * Time: 8:47:33 AM
 */
public class SAPDataSyncDetailEmployee extends SAPDataSyncDetail {
    private String employeeId;
    private String employeeName;

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
}
