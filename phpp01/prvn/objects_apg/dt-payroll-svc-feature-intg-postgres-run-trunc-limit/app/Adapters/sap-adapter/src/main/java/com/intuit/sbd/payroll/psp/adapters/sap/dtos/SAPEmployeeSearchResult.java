package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: ihannur
 * Date: 6/7/13
 * Time: 2:02 PM
 */
public class SAPEmployeeSearchResult {
    private String employeeName;
    private String employeeId;
    private String companyName;
    private String employeeSSN;
    private String employeeEmail;
    private SAPCompanyKey companyKey;

    public String getEmployeeEmail() {
        return employeeEmail;
    }

    public void setEmployeeEmail(String employeeEmail) {
        this.employeeEmail = employeeEmail;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String pEmployeeName) {
        employeeName = pEmployeeName;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String pEmployeeId) {
        employeeId = pEmployeeId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String pCompanyName) {
        companyName = pCompanyName;
    }

    public String getEmployeeSSN() {
        return employeeSSN;
    }

    public void setEmployeeSSN(String pEmployeeSSN) {
        employeeSSN = pEmployeeSSN;
    }

    public SAPCompanyKey getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(SAPCompanyKey pCompanyKey) {
        companyKey = pCompanyKey;
    }
}
