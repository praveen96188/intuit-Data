package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.address;

/**
 * Construct used to store address information.  The multiplier is used to determine how many of each State/Zip is added
 * to the pool of choices
 */
public class StateZipData {
    String state;
    String zip;
    int multiplier;


    public StateZipData(String state, String zip, int multiplier) {
        this.state = state;
        this.zip = zip;
        this.multiplier = multiplier;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }
}
