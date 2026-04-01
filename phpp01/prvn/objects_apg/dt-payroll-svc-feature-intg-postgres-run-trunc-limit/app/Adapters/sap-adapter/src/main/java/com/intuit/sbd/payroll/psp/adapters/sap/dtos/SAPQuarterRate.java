package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 2/25/13
 * Time: 3:36 PM
 */
public class SAPQuarterRate {
    private SAPQuarter quarter;
    private double currentPercentage;
    private double newPercentage;

    public SAPQuarter getQuarter() {
        return quarter;
    }

    public void setQuarter(SAPQuarter pQuarter) {
        quarter = pQuarter;
    }

    public double getCurrentPercentage() {
        return currentPercentage;
    }

    public void setCurrentPercentage(double pCurrentPercentage) {
        currentPercentage = pCurrentPercentage;
    }

    public double getNewPercentage() {
        return newPercentage;
    }

    public void setNewPercentage(double pNewPercentage) {
        newPercentage = pNewPercentage;
    }
}
