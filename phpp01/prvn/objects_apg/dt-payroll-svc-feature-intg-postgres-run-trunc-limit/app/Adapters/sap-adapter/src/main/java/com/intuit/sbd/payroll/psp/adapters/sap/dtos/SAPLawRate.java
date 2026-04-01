package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 2/21/13
 * Time: 2:08 PM
 */
public class SAPLawRate {

    private SAPLawItem law;
    private boolean hasCurrentRate;
    private double currentPercentage;
    private double newPercentage;

    private double minPercentage;
    private double maxPercentage;
    private double maxPrecision;

    public boolean isHasValuesInsteadOfRanges() {
        return hasValuesInsteadOfRanges;
    }

    public void setHasValuesInsteadOfRanges(boolean hasValuesInsteadOfRanges) {
        this.hasValuesInsteadOfRanges = hasValuesInsteadOfRanges;
    }

    private boolean hasValuesInsteadOfRanges;

    public SAPLawItem getLaw() {
        return law;
    }

    public void setLaw(SAPLawItem pLaw) {
        law = pLaw;
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

    public double getMinPercentage() {
        return minPercentage;
    }

    public void setMinPercentage(double pInPercentage) {
        minPercentage = pInPercentage;
    }

    public double getMaxPercentage() {
        return maxPercentage;
    }

    public void setMaxPercentage(double pAxPercentage) {
        maxPercentage = pAxPercentage;
    }

    public double getMaxPrecision() {
        return maxPrecision;
    }

    public void setMaxPrecision(double pAxPrecision) {
        maxPrecision = pAxPrecision;
    }

    public boolean getHasCurrentRate() {
        return hasCurrentRate;
    }

    public void setHasCurrentRate(boolean pHasCurrentRate) {
        hasCurrentRate = pHasCurrentRate;
    }
}
