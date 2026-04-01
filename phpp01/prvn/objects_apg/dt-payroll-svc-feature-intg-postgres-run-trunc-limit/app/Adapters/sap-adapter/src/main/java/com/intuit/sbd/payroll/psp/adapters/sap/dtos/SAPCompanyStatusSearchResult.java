package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: Jul 13, 2009
 * Time: 5:34:11 PM
 */
public class SAPCompanyStatusSearchResult extends SAPCompanySearchResult{
    private int numberOfStrikes;
    private double balanceDue;

    public int getNumberOfStrikes() {
        return numberOfStrikes;
    }

    public void setNumberOfStrikes(int numberOfStrikes) {
        this.numberOfStrikes = numberOfStrikes;
    }

    public double getBalanceDue() {
        return balanceDue;
    }

    public void setBalanceDue(double balanceDue) {
        this.balanceDue = balanceDue;
    }
}
