package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.PstubItemType;

/**
 * Created with IntelliJ IDEA.
 * User: yifengs302
 * Date: 2/20/13
 * Time: 4:21 PM
 * To change this template use File | Settings | File Templates.
 */
public class PstubPayItemDTO {
    private String mAcctName;
    private String mCurAmt;
    private String mEmployeePaid;
    private String mIncomeSubjectToTax;
    private String mName;
    private String mPItemListId;
    private String mQtyAmt;
    private String mQtyTime;
    private String mRate;
    private PstubItemType mType;
    private String mWageBase;
    private String mYTD;

    public PstubPayItemDTO(PstubItemType pType) {
        mType = pType;
    }

    public String getAcctName() {
        return mAcctName;
    }

    public void setAcctName(String pAcctName) {
        mAcctName = pAcctName;
    }

    public String getCurAmt() {
        return mCurAmt;
    }

    public void setCurAmt(String pCurAmt) {
        mCurAmt = pCurAmt;
    }

    public String getEmployeePaid() {
        return mEmployeePaid;
    }

    public void setEmployeePaid(String pEmployeePaid) {
        mEmployeePaid = pEmployeePaid;
    }

    public String getIncomeSubjectToTax() {
        return mIncomeSubjectToTax;
    }

    public void setIncomeSubjectToTax(String pIncomeSubjectToTax) {
        mIncomeSubjectToTax = pIncomeSubjectToTax;
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

    public String getQtyAmt() {
        return mQtyAmt;
    }

    public void setQtyAmt(String pQtyAmt) {
        mQtyAmt = pQtyAmt;
    }

    public String getQtyTime() {
        return mQtyTime;
    }

    public void setQtyTime(String pQtyTime) {
        mQtyTime = pQtyTime;
    }

    public String getRate() {
        return mRate;
    }

    public void setRate(String pRate) {
        mRate = pRate;
    }

    public PstubItemType getType() {
        return mType;
    }

    public void setType(PstubItemType pType) {
        mType = pType;
    }

    public String getWageBase() {
        return mWageBase;
    }

    public void setWageBase(String pWageBase) {
        mWageBase = pWageBase;
    }

    public String getYTD() {
        return mYTD;
    }

    public void setYTD(String pYTD) {
        mYTD = pYTD == null ? "0.00" : pYTD;
    }
}
