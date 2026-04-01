package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: mwaqarbaig
 * Date: Jan 4, 2011
 * Time: 10:16:16 AM
 */
public class SAPEftpsEnrollmentItem extends SAPBaseEnrollmentHistoryItem {

    private boolean secondaryEnrollment;

    public SAPEftpsEnrollmentItem() {
        super();
    }

    public boolean getSecondaryEnrollment() {
        return secondaryEnrollment;
    }

    public void setSecondaryEnrollment(boolean pSecondaryEnrollment) {
        secondaryEnrollment = pSecondaryEnrollment;
    }
}
