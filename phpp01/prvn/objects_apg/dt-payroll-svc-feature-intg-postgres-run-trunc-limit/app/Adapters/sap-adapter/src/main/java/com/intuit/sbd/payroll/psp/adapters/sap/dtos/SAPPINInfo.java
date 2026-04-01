package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: dweinberg
 * Date: 12/18/12
 * Time: 10:02 AM
 */
public class SAPPINInfo {
    private boolean pinCreated;
    private boolean pinLocked;

    public boolean getPinCreated() {
        return pinCreated;
    }

    public void setPinCreated(boolean pPinCreated) {
        pinCreated = pPinCreated;
    }

    public boolean getPinLocked() {
        return pinLocked;
    }

    public void setPinLocked(boolean pPinLocked) {
        pinLocked = pPinLocked;
    }
}
