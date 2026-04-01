package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: mwaqarbaig
 * Date: Oct 24, 2011
 * Time: 12:24:19 PM
 */
public class SAPDataSyncDetail {
    private String detailId;

    private int token;
    private String memo;
    private String classString;
    private boolean isDeleted;

    public String getDetailId() {
        return detailId;
    }

    public void setDetailId(String detailId) {
        this.detailId = detailId;
    }

    public int getToken() {
        return token;
    }

    public void setToken(int token) {
        this.token = token;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getClassString() {
        return classString;
    }

    public void setClassString(String classString) {
        this.classString = classString;
    }

    public boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean deleted) {
        isDeleted = deleted;
    }
}
