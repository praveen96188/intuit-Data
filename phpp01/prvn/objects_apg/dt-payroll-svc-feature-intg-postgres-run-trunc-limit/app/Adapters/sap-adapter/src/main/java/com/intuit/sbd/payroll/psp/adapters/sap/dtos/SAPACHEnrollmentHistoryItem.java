package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

public class SAPACHEnrollmentHistoryItem extends SAPBaseEnrollmentHistoryItem {
    public SAPACHEnrollmentHistoryItem() {
        super();
    }

    private Boolean mCanDelete;
    private String mRejectedReason;
    private String agencyId;

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

    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String pAgencyId) {
        agencyId = pAgencyId;
    }
}
