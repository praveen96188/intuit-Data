package com.intuit.sbd.payroll.psp.api.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 16, 2010
 * Time: 10:19:06 AM
 */
public class QBDTPaycheckInfoDTO {
    private String mListId;
    private boolean mProrate;
    private String mCheckNumber;
    private String mMemo;
    private String mCleared;
    private Boolean mOnService;
    private String mTrackingClass;
    private String mAccountName;
    private Long mToken;
    private double mVacationHoursAccrued;
    private double mSickHoursAccrued;

    public String getListId() {
        return mListId;
    }

    public void setListId(String pListId) {
        mListId = pListId;
    }

    public boolean isProrate() {
        return mProrate;
    }

    public void setProrate(boolean pProrate) {
        mProrate = pProrate;
    }

    public String getCheckNumber() {
        return mCheckNumber;
    }

    public void setCheckNumber(String pCheckNumber) {
        mCheckNumber = pCheckNumber;
    }

    public String getMemo() {
        return mMemo;
    }

    public void setMemo(String pMemo) {
        mMemo = pMemo;
    }

    public String getCleared() {
        return mCleared;
    }

    public void setCleared(String pCleared) {
        mCleared = pCleared;
    }

    public Boolean isOnService() {
        return mOnService;
    }

    public void setOnService(Boolean pOnService) {
        mOnService = pOnService;
    }

    public String getTrackingClass() {
        return mTrackingClass;
    }

    public void setTrackingClass(String pTrackingClass) {
        mTrackingClass = pTrackingClass;
    }

    public String getAccountName() {
        return mAccountName;
    }

    public void setAccountName(String pAccountName) {
        mAccountName = pAccountName;
    }

    public Long getToken() {
        return mToken;
    }

    public void setToken(Long pToken) {
        mToken = pToken;
    }

    public double getVacationHoursAccrued() {
        return mVacationHoursAccrued;
    }

    public void setVacationHoursAccrued(double pVacationHoursAccrued) {
        mVacationHoursAccrued = pVacationHoursAccrued;
    }

    public double getSickHoursAccrued() {
        return mSickHoursAccrued;
    }

    public void setSickHoursAccrued(double pSickHoursAccrued) {
        mSickHoursAccrued = pSickHoursAccrued;
    }
}
