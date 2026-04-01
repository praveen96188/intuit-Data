package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: mwaqarbaig
 * Date: Jan 4, 2011
 * Time: 10:03:19 AM
 */
public class SAPEftpsEnrollmentHistory extends SAPBaseEnrollmentHistory {
    private ArrayList<SAPEftpsEnrollmentItem> mEnrollments;

    public SAPEftpsEnrollmentHistory() {
        super();
    }

    public ArrayList<SAPEftpsEnrollmentItem> getEnrollments() {
        if (null == mEnrollments) {
            mEnrollments = new ArrayList<SAPEftpsEnrollmentItem>();
        }
        return mEnrollments;
    }

    public void setEnrollments(ArrayList<SAPEftpsEnrollmentItem> pEnrollments) {
        mEnrollments = pEnrollments;
    }
}
