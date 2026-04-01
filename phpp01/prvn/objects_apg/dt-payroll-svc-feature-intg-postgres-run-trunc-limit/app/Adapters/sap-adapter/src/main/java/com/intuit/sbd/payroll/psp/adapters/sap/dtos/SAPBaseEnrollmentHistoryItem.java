package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: mwaqarbaig
 * Date: Jan 11, 2011
 * Time: 5:14:44 PM
 */
public class SAPBaseEnrollmentHistoryItem {
    private String enrollmentId;
    private String ein;
    private String legalName;
    private String legalZip;
    private ArrayList<SAPEnrollmentStatusChange> mStatusChanges;

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String pEnrollmentId) {
        enrollmentId = pEnrollmentId;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String pLegalName) {
        legalName = pLegalName;
    }

    public String getLegalZip() {
        return legalZip;
    }

    public void setLegalZip(String pLegalZip) {
        legalZip = pLegalZip;
    }

    public ArrayList<SAPEnrollmentStatusChange> getStatusChanges() {
        if (null == mStatusChanges) {
            mStatusChanges = new ArrayList<SAPEnrollmentStatusChange>();
        }
        return mStatusChanges;
    }

    public void setStatusChanges(ArrayList<SAPEnrollmentStatusChange> pStatusChanges) {
        mStatusChanges = pStatusChanges;
    }

}
