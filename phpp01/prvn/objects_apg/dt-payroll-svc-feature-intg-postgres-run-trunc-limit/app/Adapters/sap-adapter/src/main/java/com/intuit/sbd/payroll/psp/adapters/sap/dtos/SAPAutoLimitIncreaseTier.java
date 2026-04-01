package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: rnorian
 * Date: Aug 31, 2009
 * Time: 10:19:16 PM
 */
public class SAPAutoLimitIncreaseTier {
    private String level;
    private String sourceSystemCd;
    private String payrollsRun;
    private String daysSinceFirstPayroll;
    private String increaseMultiplier;
    private String companyCap;
    private String employeeCap;


    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String sourceSystemCd) {
        this.sourceSystemCd = sourceSystemCd;
    }

    public String getPayrollsRun() {
        return payrollsRun;
    }

    public void setPayrollsRun(String payrollsRun) {
        this.payrollsRun = payrollsRun;
    }

    public String getDaysSinceFirstPayroll() {
        return daysSinceFirstPayroll;
    }

    public void setDaysSinceFirstPayroll(String daysSinceFirstPayroll) {
        this.daysSinceFirstPayroll = daysSinceFirstPayroll;
    }

    public String getIncreaseMultiplier() {
        return increaseMultiplier;
    }

    public void setIncreaseMultiplier(String increaseMultiplier) {
        this.increaseMultiplier = increaseMultiplier;
    }

    public String getCompanyCap() {
        return companyCap;
    }

    public void setCompanyCap(String companyCap) {
        this.companyCap = companyCap;
    }

    public String getEmployeeCap() {
        return employeeCap;
    }

    public void setEmployeeCap(String employeeCap) {
        this.employeeCap = employeeCap;
    }
}
