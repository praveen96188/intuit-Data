package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: mwaqarbaig
 * Date: Jan 27, 2011
 * Time: 1:33:06 PM
 */
public class SAPFilerType {
    private String mFilerType;
    private SAPQuarter mEffectiveQuarter;

    private Date mModifiedDate;
    private Date mInvalidDate;
    private String mModifierId;

    public String getFilerType() {
        return mFilerType;
    }

    public void setFilerType(String pFilerType) {
        mFilerType = pFilerType;
    }

    public SAPQuarter getEffectiveQuarter() {
        return mEffectiveQuarter;
    }

    public void setEffectiveQuarter(SAPQuarter pEffectiveQuarter) {
        mEffectiveQuarter = pEffectiveQuarter;
    }

    public Date getModifiedDate() {
        return mModifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        mModifiedDate = pModifiedDate;
    }

    public Date getInvalidDate() {
        return mInvalidDate;
    }

    public void setInvalidDate(Date pInvalidDate) {
        mInvalidDate = pInvalidDate;
    }

    public String getModifierId() {
        return mModifierId;
    }

    public void setModifierId(String pModifierId) {
        mModifierId = pModifierId;
    }
}
