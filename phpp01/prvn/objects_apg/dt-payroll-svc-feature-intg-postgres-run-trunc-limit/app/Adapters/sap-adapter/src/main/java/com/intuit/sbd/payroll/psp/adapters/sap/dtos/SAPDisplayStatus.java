package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
* Date: Jul 16, 2009
* Time: 1:07:39 PM
*/
public class SAPDisplayStatus {
    private String displayStatus;
    private String displaySubStatus;
    private String displayDetails;

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    public String getDisplayDetails() {
        return displayDetails;
    }

    public void setDisplayDetails(String displayDetails) {
        this.displayDetails = displayDetails;
    }

    public String getDisplaySubStatus() {
        return displaySubStatus;
    }

    public void setDisplaySubStatus(String displaySubStatus) {
        this.displaySubStatus = displaySubStatus;
    }
}
