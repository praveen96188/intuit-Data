package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 1/30/11
 * Time: 9:33 AM
 */
public class SAPManualLedgerTaxLine {
    private double amount=0;
    private double wageAmount=0;
    private SAPLawItem law;
    private SAPQTDYTDs originalQTDYTD=new SAPQTDYTDs();
    private boolean companyLawExists;

    public SAPLawItem getLaw() {
        return law;
    }

    public void setLaw(SAPLawItem law) {
        this.law = law;
    }

    public SAPQTDYTDs getOriginalQTDYTD() {
        return originalQTDYTD;
    }

    public void setOriginalQTDYTD(SAPQTDYTDs originalQTDYTD) {
        this.originalQTDYTD = originalQTDYTD;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getWageAmount() {
        return wageAmount;
    }

    public void setWageAmount(double wageAmount) {
        this.wageAmount = wageAmount;
    }

    public boolean isCompanyLawExists() {
        return companyLawExists;
    }

    public void setCompanyLawExists(boolean companyLawExists) {
        this.companyLawExists = companyLawExists;
    }
}
