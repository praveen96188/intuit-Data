package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

public class SAPPItemSet {
    private ArrayList<SAPPItem> companyPayrollItems = null;
    private ArrayList<SAPCompanyLaw> companyLaws = null;

    public ArrayList<SAPPItem> getCompanyPayrollItems() {
        return companyPayrollItems;
    }

    public void setCompanyPayrollItems(ArrayList<SAPPItem> pCompanyPayrollItems) {
        companyPayrollItems = pCompanyPayrollItems;
    }

    public ArrayList<SAPCompanyLaw> getCompanyLaws() {
        return companyLaws;
    }

    public void setCompanyLaws(ArrayList<SAPCompanyLaw> pCompanyLaws) {
        companyLaws = pCompanyLaws;
    }
}
