package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeType;
import com.intuit.sbd.payroll.psp.domain.QbdtEmployeeSeasonal;
/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 8, 2010
 * Time: 12:48:55 PM
 */
public class QBDTEmployeeInfoDTO {
    private String mListId;
    private String mBillPayAccount;
    private String mInitials;
    private String mPrintAsName;
    private String mTrackingClass;
    private boolean mUseDD;
    private boolean mUseTime;
    private boolean mEnforceSubjectTo;
    private String mTitle;
    private String mAltPhone;
    private boolean mIsDeleted;
    private QbdtEmployeeType mQBDTEmployeeType;
    private boolean mIsAssisted = false;
    private QbdtEmployeeSeasonal mIsSeasonal;

    public boolean getIsAssisted() {
        return mIsAssisted;
    }

    public void setIsAssisted(boolean pIsAssisted) {
        mIsAssisted = pIsAssisted;
    }

    public String getListId() {
        return mListId;
    }

    public void setListId(String pListId) {
        mListId = pListId;
    }

    public String getBillPayAccount() {
        return mBillPayAccount;
    }

    public void setBillPayAccount(String pBillPayAccount) {
        mBillPayAccount = pBillPayAccount;
    }

    public String getInitials() {
        return mInitials;
    }

    public void setInitials(String pInitials) {
        mInitials = pInitials;
    }

    public String getPrintAsName() {
        return mPrintAsName;
    }

    public void setPrintAsName(String pPrintAsName) {
        mPrintAsName = pPrintAsName;
    }

    public String getTrackingClass() {
        return mTrackingClass;
    }

    public void setTrackingClass(String pTrackingClass) {
        mTrackingClass = pTrackingClass;
    }

    public boolean isUseDD() {
        return mUseDD;
    }

    public void setUseDD(boolean pUseDD) {
        mUseDD = pUseDD;
    }

    public boolean isUseTime() {
        return mUseTime;
    }

    public void setUseTime(boolean pUseTime) {
        mUseTime = pUseTime;
    }

    public boolean isEnforceSubjectTo() {
        return mEnforceSubjectTo;
    }

    public void setEnforceSubjectTo(boolean pEnforceSubjectTo) {
        mEnforceSubjectTo = pEnforceSubjectTo;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String pTitle) {
        mTitle = pTitle;
    }

    public String getAltPhone() {
        return mAltPhone;
    }

    public void setAltPhone(String pAltPhone) {
        mAltPhone = pAltPhone;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    public void setIsDeleted(boolean pIsDeleted) {
        mIsDeleted = pIsDeleted;
    }

    public QbdtEmployeeType getQBDTEmployeeType() {
        return mQBDTEmployeeType;
    }

    public void setQBDTEmployeeType(QbdtEmployeeType pQBDTEmployeeType) {
        mQBDTEmployeeType = pQBDTEmployeeType;
    }

	public QbdtEmployeeSeasonal isSeasonal() {
		return mIsSeasonal;
	}

	public void setIsSeasonal(QbdtEmployeeSeasonal pIsSeasonal) {
		this.mIsSeasonal = pIsSeasonal;
	}
    
}
