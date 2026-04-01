package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: 3/14/12
 * Time: 1:20 PM
 */
public class SAPTaxExemptInfo {
    private String exemptStatus;
    private Date expirationDate;
    private boolean isCurrentlyExempt;

    public String getExemptStatus() {
        return exemptStatus;
    }

    public void setExemptStatus(String exemptStatus) {
        this.exemptStatus = exemptStatus;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean getIsCurrentlyExempt() {
        return isCurrentlyExempt;
    }

    public void setIsCurrentlyExempt(boolean currentlyExempt) {
        isCurrentlyExempt = currentlyExempt;
    }
}
