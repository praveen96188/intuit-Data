package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: mwaqarbaig
 * Date: Oct 17, 2011
 * Time: 8:47:33 AM
 */
public class SAPDataSyncDetailPayrollItem extends SAPDataSyncDetail {
    private int mPayrollItemId;
    private String mPayrollItemName;
    private String mPayrollItemType;
    private boolean mEE;
    private boolean mInactive;

    public int getPayrollItemId() {
        return mPayrollItemId;
    }

    public void setPayrollItemId(int mPayrollItemId) {
        this.mPayrollItemId = mPayrollItemId;
    }

    public String getPayrollItemName() {
        return mPayrollItemName;
    }

    public void setPayrollItemName(String pPayrollItemName) {
        mPayrollItemName = pPayrollItemName;
    }

    public String getPayrollItemType() {
        return mPayrollItemType;
    }

    public void setPayrollItemType(String pPayrollItemType) {
        mPayrollItemType = pPayrollItemType;
    }

    public boolean isEE() {
        return mEE;
    }

    public void setEE(boolean pEE) {
        mEE = pEE;
    }

    public boolean isInactive() {
        return mInactive;
    }

    public void setInactive(boolean pInactive) {
        mInactive = pInactive;
    }

}
