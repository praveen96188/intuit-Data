package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Jan 4, 2011
 * Time: 10:17:02 AM
 */
public class SAPEnrollmentStatusChange {
    private Date mChangeDate;
    private String mModifiedBy;
    private String mStatus;

    public Date getChangeDate() {
        return mChangeDate;
    }

    public void setChangeDate(Date pChangeDate) {
        mChangeDate = pChangeDate;
    }

    public String getModifiedBy() {
        return mModifiedBy;
    }

    public void setModifiedBy(String pModifiedBy) {
        mModifiedBy = pModifiedBy;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String pStatus) {
        mStatus = pStatus;
    }
}
