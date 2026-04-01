package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: mwaqarbaig
 * Date: Jan 11, 2011
 * Time: 6:21:21 PM
 */
public class SAPRAFEnrollmentHistoryItem extends SAPBaseEnrollmentHistoryItem {
    public SAPRAFEnrollmentHistoryItem() {
        super();
    }

    private String filerType;
    private String firstFilingQuarter;
    private Boolean mCanDelete;
    private String mRejectedReason;

    public String getFilerType() {
        return filerType;
    }

    public void setFilerType(String pFilerType) {
        filerType = pFilerType;
    }

    public String getFirstFilingQuarter() {
        return firstFilingQuarter;
    }

    public void setFirstFilingQuarter(String pFirstFilingQuarter) {
        firstFilingQuarter = pFirstFilingQuarter;
    }

    public Boolean getCanDelete() {
        return mCanDelete;
    }

    public void setCanDelete(Boolean pCanDelete) {
        mCanDelete = pCanDelete;
    }

    public String getRejectedReason() {
        return mRejectedReason;
    }

    public void setRejectedReason(String pRejectedReason) {
        mRejectedReason = pRejectedReason;
    }
}
