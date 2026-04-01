package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.ArrayList;

/**
 * User: kpaul
 * Date: Jun 23, 2009
 * Time: 1:52:07 AM
 */
public class SAPCompanyEventEmail {
    private String id;
    private String mTemplateType;
    private String mStatus;
    private String mEffectiveDate;
    private boolean mtlEnabled;

    private ArrayList<SAPCompanyEventEmailParam> mEmailParams = new ArrayList<SAPCompanyEventEmailParam>();

    public String getTemplateType() {
        return mTemplateType;
    }

    public void setTemplateType(String pTemplateType) {
        mTemplateType = pTemplateType;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String pStatus) {
        mStatus = pStatus;
    }

    public String getEffectiveDate() {
        return mEffectiveDate;
    }

    public void setEffectiveDate(String pEffectiveDate) {
        mEffectiveDate = pEffectiveDate;
    }

    public boolean getMtlEnabled() {
        return mtlEnabled;
    }

    public void setMtlEnabled(boolean pMtlEnabled) {
        mtlEnabled = pMtlEnabled;
    }

    public ArrayList<SAPCompanyEventEmailParam> getEmailParams() {
        return mEmailParams;
    }

    public void setEmailParams(ArrayList<SAPCompanyEventEmailParam> pEmailParams) {
        mEmailParams = pEmailParams;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
