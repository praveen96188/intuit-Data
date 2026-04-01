package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: mwaqarbaig
 * Date: Jan 11, 2011
 * Time: 6:17:44 PM
 */
public class SAPRAFEnrollmentHistory extends SAPBaseEnrollmentHistory {
    private ArrayList<SAPRAFEnrollmentHistoryItem> mEnrollments;

    public SAPRAFEnrollmentHistory() {
        super();
    }

    public ArrayList<SAPRAFEnrollmentHistoryItem> getEnrollments() {
        if (null == mEnrollments) {
            mEnrollments = new ArrayList<SAPRAFEnrollmentHistoryItem>();
        }
        return mEnrollments;
    }

    public void setEnrollments(ArrayList<SAPRAFEnrollmentHistoryItem> pEnrollments) {
        mEnrollments = pEnrollments;
    }
}
