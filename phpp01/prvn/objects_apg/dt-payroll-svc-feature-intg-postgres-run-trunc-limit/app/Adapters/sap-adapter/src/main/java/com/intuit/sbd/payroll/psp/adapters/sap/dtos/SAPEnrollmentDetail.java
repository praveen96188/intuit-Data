package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Dec 29, 2010
 * Time: 10:44:19 AM
 */
public class SAPEnrollmentDetail {
    private String companyName;
    private SAPCompanyKey companyKey;
    private String ein;
    private String status;
    private Date rejectionDate;
    private String enrollmentId;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String pCompanyName) {
        companyName = pCompanyName;
    }

    public SAPCompanyKey getCompanyKey() {
        return companyKey;
    }

    public void setCompanyKey(SAPCompanyKey pCompanyKey) {
        companyKey = pCompanyKey;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }

    public Date getRejectionDate() {
        return rejectionDate;
    }

    public void setRejectionDate(Date pRejectionDate) {
        rejectionDate = pRejectionDate;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(String pEnrollmentId) {
        enrollmentId = pEnrollmentId;
    }
}
