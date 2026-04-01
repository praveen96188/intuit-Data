package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: May 16, 2009
 * Time: 7:39:27 PM
 */
public class SAPDepositFrequency {
    private String mDepositFrequency;
    private Date mEffectiveDate;
    private Date mModifiedDate;
    private Date mInvalidDate;
    private String mModifierId;
    private boolean isCurrent;
    private String obsoleteFrequency;

    public String getDepositFrequency() {
        return mDepositFrequency;
    }

    public void setDepositFrequency(String pDepositFrequency) {
        this.mDepositFrequency = pDepositFrequency;
    }

    public Date getEffectiveDate() {
        return mEffectiveDate;
    }

    public void setEffectiveDate(Date pEffectiveDate) {
        this.mEffectiveDate = pEffectiveDate;
    }

    public Date getModifiedDate() {
        return mModifiedDate;
    }

    public Date getInvalidDate() {
        return mInvalidDate;
    }

    public void setInvalidDate(Date pInvalidDate) {
        mInvalidDate = pInvalidDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        mModifiedDate = pModifiedDate;
    }

    public String getModifierId() {
        return mModifierId;
    }

    public void setModifierId(String pModifierId) {
        mModifierId = pModifierId;
    }

    public boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(boolean pCurrent) {
        isCurrent = pCurrent;
    }

    public String getObsoleteFrequency() {
        return obsoleteFrequency;
    }

    public void setObsoleteFrequency(String pObsoleteFrequency) {
        obsoleteFrequency = pObsoleteFrequency;
    }
}
