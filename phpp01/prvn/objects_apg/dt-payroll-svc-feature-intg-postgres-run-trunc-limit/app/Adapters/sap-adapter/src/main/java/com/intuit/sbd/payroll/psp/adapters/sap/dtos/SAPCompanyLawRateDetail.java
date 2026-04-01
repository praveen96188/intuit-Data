package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Jan 20, 2011
 * Time: 3:31:48 PM
 */
public class SAPCompanyLawRateDetail {
    private String mLawName;
    private String mLawId;
    private Double mRate;
    private SAPQuarter mEffectiveQuarter;
    private Date mChangeDate;
    private String mChangedBy;
    private Date mInvalidDate;
    private Boolean isCurrent;
    private String mAgencyId;
    private Boolean mExempt;
    private Boolean mReimbursable;
    private Boolean mInactive;
    private String mSourceLawID;
    private String mSourceLawDescription;
    private Date createdDate;
    private String createdBy;

    public String getLawName() {
        return mLawName;
    }

    public void setLawName(String pLawName) {
        mLawName = pLawName;
    }

    public String getLawId() {
        return mLawId;
    }

    public void setLawId(String pLawId) {
        mLawId = pLawId;
    }

    public Double getRate() {
        return mRate;
    }

    public void setRate(Double pRate) {
        mRate = pRate;
    }

    public SAPQuarter getEffectiveQuarter() {
        return mEffectiveQuarter;
    }

    public void setEffectiveQuarter(SAPQuarter pEffectiveQuarter) {
        mEffectiveQuarter = pEffectiveQuarter;
    }

    public Date getChangeDate() {
        return mChangeDate;
    }

    public void setChangeDate(Date pChangeDate) {
        mChangeDate = pChangeDate;
    }

    public String getChangedBy() {
        return mChangedBy;
    }

    public void setChangedBy(String pChangedBy) {
        mChangedBy = pChangedBy;
    }

    public Date getInvalidDate() {
        return mInvalidDate;
    }

    public void setInvalidDate(Date pInvalidDate) {
        mInvalidDate = pInvalidDate;
    }

    public String getAgencyId() {
        return mAgencyId;
    }

    public void setAgencyId(String pAgencyId) {
        mAgencyId = pAgencyId;
    }

    public Boolean getExempt() {
        return mExempt;
    }

    public void setExempt(Boolean pExempt) {
        mExempt = pExempt;
    }

    public Boolean getReimbursable() {
        return mReimbursable;
    }

    public void setReimbursable(Boolean pReimbursable) {
        mReimbursable = pReimbursable;
    }

    public String getSourceLawID() {
        return mSourceLawID;
    }

    public void setSourceLawID(String pSourceLawID) {
        mSourceLawID = pSourceLawID;
    }

    public String getSourceLawDescription() {
        return mSourceLawDescription;
    }

    public void setSourceLawDescription(String pSourceLawDescription) {
        mSourceLawDescription = pSourceLawDescription;
    }

    public Boolean getInactive() {
        return mInactive;
    }

    public void setInactive(Boolean mInactive) {
        this.mInactive = mInactive;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean pCurrent) {
        isCurrent = pCurrent;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date pCreatedDate) {
        createdDate = pCreatedDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String pCreatedBy) {
        createdBy = pCreatedBy;
    }
}
