package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 3/4/13
 * Time: 10:31 AM
 */
public class SAPAgencyIdRequirement {
    private boolean isFulfilled;
    private String requirementString;

    public boolean getIsFulfilled() {
        return isFulfilled;
    }

    public void setIsFulfilled(boolean pFulfilled) {
        isFulfilled = pFulfilled;
    }

    public String getRequirementString() {
        return requirementString;
    }

    public void setRequirementString(String pRequirementString) {
        requirementString = pRequirementString;
    }
}
