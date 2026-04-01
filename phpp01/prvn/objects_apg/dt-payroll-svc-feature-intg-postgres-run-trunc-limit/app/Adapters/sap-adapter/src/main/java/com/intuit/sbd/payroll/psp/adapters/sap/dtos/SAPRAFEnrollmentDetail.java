package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Jan 11, 2011
 * Time: 11:11:14 AM
 */
public class SAPRAFEnrollmentDetail extends SAPEnrollmentDetail {

    private String rejectionReason;
    private Date creationDate;
    /*  modifiedDate will server purpose for LastUpdateDate, TapeDate, Rejection Date & UnEnrollmentDate.
        Besides, it _will_ probably get that value from that column. */

    /*  can be called the 'other' date  for clarity and consistency */
    private Date modifiedDate;
    private boolean mCompanyHasPayrolls;

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String pRejectionReason) {
        rejectionReason = pRejectionReason;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date pCreationDate) {
        creationDate = pCreationDate;
    }

    public Date getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        modifiedDate = pModifiedDate;
    }

    public boolean isCompanyHasPayrolls() {
        return mCompanyHasPayrolls;
    }

    public void setCompanyHasPayrolls(boolean pCompanyHasPayrolls) {
        mCompanyHasPayrolls = pCompanyHasPayrolls;
    }
}
