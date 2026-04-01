package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/20/13
 * Time: 4:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class PstubPaidTimeoffItemDTO {
    private String mAcctName;
    private String mAvailable;
    private String mName;
    private String mPItemListId;
    private String mYTDUsed;

    public String getAcctName() {
        return mAcctName;
    }

    public void setAcctName(String pAcctName) {
        mAcctName = pAcctName;
    }

    public String getName() {
        return mName;
    }

    public void setName(String pName) {
        mName = pName;
    }

    public String getPItemListId() {
        return mPItemListId;
    }

    public void setPItemListId(String pPItemListId) {
        mPItemListId = pPItemListId;
    }

    public String getAvailable() {
        return mAvailable;
    }

    public void setAvailable(String pAvailable) {
        mAvailable = pAvailable;
    }

    public String getYTDUsed() {
        return mYTDUsed;
    }

    public void setYTDUsed(String pYTDUsed) {
        mYTDUsed = pYTDUsed;
    }
}
