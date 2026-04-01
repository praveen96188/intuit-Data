package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: mwaqarbaig
 * Date: Jun 24, 2011
 * Time: 10:49:06 AM
 */
public class SAPPaymentMethod {
    private Date mModifiedDate;
    private String mPaymentMethodName;
    private Boolean mIsAgentEnabled;
    private Boolean mIsEnabled;
    private String mChangedBy;
    private List<String> mRequirements;
    private List<String> mAdditionalRequirements;
    private int mPaymentMethodOrder;
    private boolean hasManualRequirement;

    public Date getModifiedDate() {
        return mModifiedDate;
    }

    public void setModifiedDate(Date pModifiedDate) {
        mModifiedDate = pModifiedDate;
    }

    public String getPaymentMethodName() {
        return mPaymentMethodName;
    }

    public void setPaymentMethodName(String pPaymentMethodName) {
        mPaymentMethodName = pPaymentMethodName;
    }

    public Boolean getIsAgentEnabled() {
        return mIsAgentEnabled;
    }

    public void setIsAgentEnabled(Boolean pIsAgentEnabled) {
        mIsAgentEnabled = pIsAgentEnabled;
    }

    public Boolean getIsEnabled() {
        return mIsEnabled;
    }

    public void setIsEnabled(Boolean pIsEnabled) {
        mIsEnabled = pIsEnabled;
    }

    public String getChangedBy() {
        return mChangedBy;
    }

    public void setChangedBy(String pChangedBy) {
        mChangedBy = pChangedBy;
    }

    public List<String> getRequirements() {
        if (mRequirements == null) {
            mRequirements = new ArrayList<String>();
        }
        return mRequirements;
    }

    public void setRequirements(List<String> pRequirements) {
        if (pRequirements == null) {
            pRequirements = new ArrayList<String>();
        }
        mRequirements = pRequirements;
    }

    public List<String> getAdditionalRequirements() {
        if (mAdditionalRequirements == null) {
            mAdditionalRequirements = new ArrayList<String>();
        }
        return mAdditionalRequirements;
    }

    public void setAdditionalRequirements(List<String> mAddlRequirements) {
        this.mAdditionalRequirements = mAddlRequirements;
    }

    public int getPaymentMethodOrder() {
        return mPaymentMethodOrder;
    }

    public void setPaymentMethodOrder(int pPaymentMethodOrder) {
        mPaymentMethodOrder = pPaymentMethodOrder;
    }

    public boolean getHasManualRequirement() {
        return hasManualRequirement;
    }

    public void setHasManualRequirement(boolean pHasManualRequirement) {
        hasManualRequirement = pHasManualRequirement;
    }
}
