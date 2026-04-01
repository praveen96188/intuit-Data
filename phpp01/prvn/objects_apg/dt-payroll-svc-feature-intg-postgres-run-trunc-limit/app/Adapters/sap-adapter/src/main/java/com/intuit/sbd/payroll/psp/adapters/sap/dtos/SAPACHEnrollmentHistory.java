package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

public class SAPACHEnrollmentHistory extends SAPBaseEnrollmentHistory {
    private ArrayList<SAPACHEnrollmentHistoryItem> mEnrollments;

    public SAPACHEnrollmentHistory() {
        super();
    }

    public ArrayList<SAPACHEnrollmentHistoryItem> getEnrollments() {
        if (null == mEnrollments) {
            mEnrollments = new ArrayList<SAPACHEnrollmentHistoryItem>();
        }
        return mEnrollments;
    }

    public void setEnrollments(ArrayList<SAPACHEnrollmentHistoryItem> pEnrollments) {
        mEnrollments = pEnrollments;
    }
}
