package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

public class SAPEmployeeTaxabilityInfo {

    private String jurisdiction;
    private String taxType;
    private String filingStatus;
    private int allowances;
    private double extraWithHolding;
    private double claimDependents;
    private double otherIncome;
    private double deductions;
    private boolean multipleJobs;
    private String fedW4EmployeePref;
    private boolean subjectTo;

    public String getJurisdiction() {
        return jurisdiction;
    }

    public void setJurisdiction(String jurisdiction) {
        this.jurisdiction = jurisdiction;
    }

    public String getTaxType() {
        return taxType;
    }

    public void setTaxType(String taxType) {
        this.taxType = taxType;
    }

    public String getFilingStatus() {
        return filingStatus;
    }

    public void setFilingStatus(String filingStatus) {
        this.filingStatus = filingStatus;
    }

    public int getAllowances() {
        return allowances;
    }

    public void setAllowances(int allowances) {
        this.allowances = allowances;
    }

    public double getExtraWithHolding() {
        return extraWithHolding;
    }

    public void setExtraWithHolding(double extraWithHolding) {
        this.extraWithHolding = extraWithHolding;
    }

    public double getClaimDependents() { return claimDependents; }

    public void setClaimDependents(double claimDependents) { this.claimDependents = claimDependents; }

    public double getOtherIncome() { return otherIncome; }

    public void setOtherIncome(double otherIncome) { this.otherIncome = otherIncome; }

    public double getDeductions() { return deductions; }

    public void setDeductions(double deductions) { this.deductions = deductions; }

    public boolean getMultipleJobs() { return multipleJobs; }

    public void setMultipleJobs(boolean multipleJobs) { this.multipleJobs = multipleJobs; }

    public String getFedW4EmployeePref() { return fedW4EmployeePref; }

    public void setFedW4EmployeePref(String fedW4EmployeePref) { this.fedW4EmployeePref = fedW4EmployeePref; }

    public boolean getSubjectTo() {
        return subjectTo;
    }

    public void setSubjectTo(boolean subjectTo) {
        this.subjectTo = subjectTo;
    }
}
