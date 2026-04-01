package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 8/21/12
 * Time: 3:49 PM
 */
public class SAPCompanyFilingAmount {
    private String name;
    private String value;
    private String previousQuarterValue;
    private boolean isRate;
    private boolean hasCurrentValue;

    private String newValue;

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String pValue) {
        value = pValue;
    }

    public boolean getIsRate() {
        return isRate;
    }

    public void setIsRate(boolean pRate) {
        isRate = pRate;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String pNewValue) {
        newValue = pNewValue;
    }

    public boolean getHasCurrentValue() {
        return hasCurrentValue;
    }

    public void setHasCurrentValue(boolean pHasCurrentValue) {
        hasCurrentValue = pHasCurrentValue;
    }

    public String getPreviousQuarterValue() {
        return previousQuarterValue;
    }

    public void setPreviousQuarterValue(String pPreviousQuarterValue) {
        previousQuarterValue = pPreviousQuarterValue;
    }
}
