package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.Company;
import com.intuit.sbd.payroll.psp.domain.QbdtTransactionInfo;
import com.intuit.spc.foundations.portability.SpcfUniqueId;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 9, 2010
 * Time: 1:40:15 PM
 */
public class QBDTTransactionInfoDTO {
    private SpcfUniqueId mId;
    private String mAgencyName;
    private String mReferenceNumber;
    private String mAccountName;
    private String mMemo;
    private boolean mOnService;
    private String mCleared;
    private String mTrackingClass;
    private boolean mIsDeleted;
    private long mToken = -1;
    private boolean mIsDirectDeposit;
    private boolean mSystemGenerated;

    public SpcfUniqueId getId() {
        return mId;
    }

    public void setId(SpcfUniqueId pId) {
        mId = pId;
    }

    public String getAgencyName() {
        return mAgencyName;
    }

    public void setAgencyName(String pAgencyName) {
        mAgencyName = pAgencyName;
    }

    public String getReferenceNumber() {
        return mReferenceNumber;
    }

    public void setReferenceNumber(String pReferenceNumber) {
        mReferenceNumber = pReferenceNumber;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public void setAccountName(String pAccountName) {
        mAccountName = pAccountName;
    }

    public String getMemo() {
        return mMemo;
    }

    public void setMemo(String pMemo) {
        mMemo = pMemo;
    }

    public boolean isOnService() {
        return mOnService;
    }

    public void setOnService(Boolean pOnService) {
        mOnService = pOnService;
    }

    public String getCleared() {
        return mCleared;
    }

    public void setCleared(String pCleared) {
        mCleared = pCleared;
    }

    public String getTrackingClass() {
        return mTrackingClass;
    }

    public void setTrackingClass(String pTrackingClass) {
        mTrackingClass = pTrackingClass;
    }

    public boolean isDeleted() {
        return mIsDeleted;
    }

    public void setIsDeleted(Boolean pIsDeleted) {
        mIsDeleted = pIsDeleted;
    }

    public long getToken() {
        return mToken;
    }

    public void setToken(Long pToken) {
        mToken = pToken;
    }

    public boolean isDirectDeposit() {
        return mIsDirectDeposit;
    }

    public void setIsDirectDeposit(boolean pIsDirectDeposit) {
        mIsDirectDeposit = pIsDirectDeposit;
    }

    public boolean isSystemGenerated() {
        return mSystemGenerated;
    }

    public void setSystemGenerated(boolean pSystemGenerated) {
        mSystemGenerated = pSystemGenerated;
    }

    public void copyQBDTTransactionInfoFromDTO(QbdtTransactionInfo pQbdtTransactionInfo) {
        pQbdtTransactionInfo.setAccountName(getAccountName());
        pQbdtTransactionInfo.setAgencyName(getAgencyName());
        pQbdtTransactionInfo.setCleared(getCleared());
        pQbdtTransactionInfo.setIsDeleted(isDeleted());
        pQbdtTransactionInfo.setIsDirectDeposit(isDirectDeposit());
        pQbdtTransactionInfo.setOnService(isOnService());
        pQbdtTransactionInfo.setMemo(getMemo());
        pQbdtTransactionInfo.setReferenceNumber(getReferenceNumber());
        pQbdtTransactionInfo.setSystemGenerated(isSystemGenerated());
        if(getToken() == Company.EXCLUDE_TOKEN) {
            pQbdtTransactionInfo.setToken(getToken());
        }
        pQbdtTransactionInfo.setTrackingClass(getTrackingClass());
    }

      public void createDTOFromQBDTTransactionInfo(QbdtTransactionInfo pQbdtTransactionInfo) {
        this.setAccountName(pQbdtTransactionInfo.getAccountName());
        this.setAgencyName(pQbdtTransactionInfo.getAgencyName());
        this.setCleared(pQbdtTransactionInfo.getCleared());
        this.setIsDeleted(pQbdtTransactionInfo.getIsDeleted());
        this.setOnService(pQbdtTransactionInfo.getOnService());
        this.setMemo(pQbdtTransactionInfo.getMemo());
        this.setReferenceNumber(pQbdtTransactionInfo.getReferenceNumber());
        this.setToken(pQbdtTransactionInfo.getToken());
        this.setTrackingClass(pQbdtTransactionInfo.getTrackingClass());
    }
}
