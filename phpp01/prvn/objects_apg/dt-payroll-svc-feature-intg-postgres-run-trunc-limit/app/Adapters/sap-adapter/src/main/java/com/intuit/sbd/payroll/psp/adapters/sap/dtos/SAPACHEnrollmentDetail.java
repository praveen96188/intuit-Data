package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

public class SAPACHEnrollmentDetail extends SAPEnrollmentDetail {

    private String rejectionReason;
    private Date creationDate;
    private Date modifiedDate;
    private Date effectiveDate;
    private String aid;

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

    public String getAid() {
        return aid;
    }

    public void setAid(String pAid) {
        aid = pAid;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(Date pEffectiveDate) {
        effectiveDate = pEffectiveDate;
    }
}
