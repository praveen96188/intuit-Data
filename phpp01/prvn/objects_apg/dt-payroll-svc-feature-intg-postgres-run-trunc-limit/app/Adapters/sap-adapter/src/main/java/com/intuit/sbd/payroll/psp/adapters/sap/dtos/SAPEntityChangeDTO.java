package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Feb 11, 2011
 * Time: 12:06:30 PM
 */
public class SAPEntityChangeDTO {
    private SAPCompanyKey mCompanyKey;
    private String mOldEIN;
    private Date mEffectiveDate;
    private Date mChangeDate;
    private String mNewEIN;
    private String mAgentId;
    private Boolean mIsSuccessor;
    private Boolean mHasNewDataFile;
    private Boolean mIsError;

    public SAPCompanyKey getCompanyKey() {
        return mCompanyKey;
    }

    public void setCompanyKey(SAPCompanyKey pCompanyKey) {
        mCompanyKey = pCompanyKey;
    }

    public String getOldEIN() {
        return mOldEIN;
    }

    public void setOldEIN(String pOldEIN) {
        mOldEIN = pOldEIN;
    }

    public Date getEffectiveDate() {
        return mEffectiveDate;
    }

    public void setEffectiveDate(Date pEffectiveDate) {
        mEffectiveDate = pEffectiveDate;
    }

    public Date getChangeDate() {
        return mChangeDate;
    }

    public void setChangeDate(Date pChangeDate) {
        mChangeDate = pChangeDate;
    }

    public String getNewEIN() {
        return mNewEIN;
    }

    public void setNewEIN(String pNewEIN) {
        mNewEIN = pNewEIN;
    }

    public String getAgentId() {
        return mAgentId;
    }

    public void setAgentId(String pAgentId) {
        mAgentId = pAgentId;
    }

    public Boolean getIsSuccessor() {
        return mIsSuccessor;
    }

    public void setIsSuccessor(Boolean pIsSuccessor) {
        mIsSuccessor = pIsSuccessor;
    }

    public Boolean getHasNewDataFile() {
        return mHasNewDataFile;
    }

    public void setHasNewDataFile(Boolean pHasNewDataFile) {
        mHasNewDataFile = pHasNewDataFile;
    }

    public Boolean getIsError() {
        return mIsError;
    }

    public void setIsError(Boolean pIsError) {
        mIsError = pIsError;
    }
}
