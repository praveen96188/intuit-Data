package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/20/13
 * Time: 4:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PstubDDItemDTO {
    private String mAcctName;
    private String mAcctNumber;
    private String mAcctType;
    private String mBankName;
    private String mCurAmt;
    private String mName;
    private String mPItemListId;
    private String mRoutingNumber;

    public String getAcctName() {
        return mAcctName;
    }

    public void setAcctName(String pAcctName) {
        mAcctName = pAcctName;
    }

    public String getAcctNumber() {
        return mAcctNumber;
    }

    public void setAcctNumber(String pAcctNumber) {
        mAcctNumber = pAcctNumber;
    }

    public String getAcctType() {
        return mAcctType;
    }

    public void setAcctType(String pAcctType) {
        mAcctType = pAcctType;
    }

    public String getBankName() {
        return mBankName;
    }

    public void setBankName(String pBankName) {
        mBankName = pBankName;
    }

    public String getCurAmt() {
        return mCurAmt;
    }

    public void setCurAmt(String pCurAmt) {
        mCurAmt = pCurAmt;
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

    public String getRoutingNumber() {
        return mRoutingNumber;
    }

    public void setRoutingNumber(String pRoutingNumber) {
        mRoutingNumber = pRoutingNumber;
    }
}
