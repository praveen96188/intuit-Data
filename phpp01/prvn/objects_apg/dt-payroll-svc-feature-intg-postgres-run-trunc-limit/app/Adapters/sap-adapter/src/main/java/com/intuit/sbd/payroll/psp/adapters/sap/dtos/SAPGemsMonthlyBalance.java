package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: Sep 18, 2009
 * Time: 3:42:22 PM
 */
public class SAPGemsMonthlyBalance {
    private String account;
    private String company;
    private String department;
    private String groupCode;
    private String interCompany;
    private double reportedBalance;
    private String uploadStatus;

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(String groupCode) {
        this.groupCode = groupCode;
    }

    public String getInterCompany() {
        return interCompany;
    }

    public void setInterCompany(String interCompany) {
        this.interCompany = interCompany;
    }

    public double getReportedBalance() {
        return reportedBalance;
    }

    public void setReportedBalance(double reportedBalance) {
        this.reportedBalance = reportedBalance;
    }

    public String getUploadStatus() {
        return uploadStatus;
    }

    public void setUploadStatus(String uploadStatus) {
        this.uploadStatus = uploadStatus;
    }
}
