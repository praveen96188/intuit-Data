package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: mwaqarbaig
 * Date: Oct 28, 2011
 * Time: 9:25:57 AM
 */
public class SAPDataSyncSelectedItem {
    private String mItemId;
    private String mItemType;
    private int mToken;
    private String mDescription;

    public String getItemId() {
        return mItemId;
    }

    public void setItemId(String pItemId) {
        mItemId = pItemId;
    }

    public String getItemType() {
        return mItemType;
    }

    public void setItemType(String pItemType) {
        mItemType = pItemType;
    }

    public int getToken() {
        return mToken;
    }

    public void setToken(int pToken) {
        mToken = pToken;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String pDescription) {
        mDescription = pDescription;
    }
}
