package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: May 27, 2009
 * Time: 8:44:05 AM 
 */
public class SAPLawTransactions {

    private SAPAgency agency;
    private SAPLawItem law;
    private double currentTaxesSum;
    private ArrayList<SAPTaxTransaction> taxTransactions;

    public SAPAgency getAgency() {
        return agency;
    }

    public void setAgency(SAPAgency pAgency) {
        agency = pAgency;
    }

    public SAPLawItem getLaw() {
        return law;
    }

    public void setLaw(SAPLawItem law) {
        this.law = law;
    }

    public double getCurrentTaxesSum() {
        return currentTaxesSum;
    }

    public void setCurrentTaxesSum(double currentTaxesSum) {
        this.currentTaxesSum = currentTaxesSum;
    }

    public ArrayList<SAPTaxTransaction> getTaxTransactions() {
        return taxTransactions;
    }

    public void setTaxTransactions(ArrayList<SAPTaxTransaction> taxTransactions) {
        this.taxTransactions = taxTransactions;
    }
}
