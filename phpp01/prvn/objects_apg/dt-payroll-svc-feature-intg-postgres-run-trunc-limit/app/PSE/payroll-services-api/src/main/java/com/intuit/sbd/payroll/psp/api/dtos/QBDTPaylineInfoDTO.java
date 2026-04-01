package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.QbdtNumericType;
import com.intuit.sbd.payroll.psp.domain.QbdtPaylineInfo;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Oct 16, 2010
 * Time: 10:26:55 AM
 */
public class QBDTPaylineInfoDTO {
    private double mRate;
    private String mTrackingClass;
    private String mJob;
    private String mItem;
    private String mWcCode;
    private double mQuantity;
    private boolean mExpenseByJob;
    private QbdtNumericType mRateType;
    private QbdtNumericType mQuantityType;

    public double getRate() {
        return mRate;
    }

    public void setRate(double pRate) {
        mRate = pRate;
    }

    public String getTrackingClass() {
        return mTrackingClass;
    }

    public void setTrackingClass(String pTrackingClass) {
        mTrackingClass = pTrackingClass;
    }

    public String getJob() {
        return mJob;
    }

    public void setJob(String pJob) {
        mJob = pJob;
    }

    public String getItem() {
        return mItem;
    }

    public void setItem(String pItem) {
        mItem = pItem;
    }

    public String getWcCode() {
        return mWcCode;
    }

    public void setWcCode(String pWcCode) {
        mWcCode = pWcCode;
    }

    public double getQuantity() {
        return mQuantity;
    }

    public void setQuantity(double pQuantity) {
        mQuantity = pQuantity;
    }

    public boolean isExpenseByJob() {
        return mExpenseByJob;
    }

    public void setExpenseByJob(boolean pExpenseByJob) {
        mExpenseByJob = pExpenseByJob;
    }

    public QbdtNumericType getRateType() {
        return mRateType;
    }

    public void setRateType(QbdtNumericType pRateType) {
        mRateType = pRateType;
    }

    public QbdtNumericType getQuantityType() {
        return mQuantityType;
    }

    public void setQuantityType(QbdtNumericType pQuantityType) {
        mQuantityType = pQuantityType;
    }

    public void copyToDomain(QbdtPaylineInfo pQbdtpaylineinfo) {
        if(pQbdtpaylineinfo == null) {
            return;
        }

        pQbdtpaylineinfo.setExpenseByJob(isExpenseByJob());
        pQbdtpaylineinfo.setItem(getItem());
        pQbdtpaylineinfo.setJob(getJob());
        pQbdtpaylineinfo.setQuantity(getQuantity());
        pQbdtpaylineinfo.setQuantityType(getQuantityType());
        pQbdtpaylineinfo.setRate(getRate());
        pQbdtpaylineinfo.setRateType(getRateType());
        pQbdtpaylineinfo.setTrackingClass(getTrackingClass());
        pQbdtpaylineinfo.setWcCode(getWcCode());
    }
}
