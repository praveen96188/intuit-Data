package com.intuit.sbd.payroll.psp.batchjobs.printedchecks.dtos;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jun 24, 2011
 * Time: 3:27:59 PM
 */
public class PayeeDTO {
    private String mNameLine1;
    private String mNameLine2;
    private String mAddressLine1;
    private String mAddressLine2;
    private String mCity;
    private String mState;
    private String mZip;

    public String getNameLine1() {
        return mNameLine1;
    }

    public void setNameLine1(String pNameLine1) {
        mNameLine1 = pNameLine1;
    }

    public String getNameLine2() {
        return mNameLine2;
    }

    public void setNameLine2(String pNameLine2) {
        mNameLine2 = pNameLine2;
    }

    public String getAddressLine1() {
        return mAddressLine1;
    }

    public void setAddressLine1(String pAddressLine1) {
        mAddressLine1 = pAddressLine1;
    }

    public String getAddressLine2() {
        return mAddressLine2;
    }

    public void setAddressLine2(String pAddressLine2) {
        mAddressLine2 = pAddressLine2;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String pCity) {
        mCity = pCity;
    }

    public String getState() {
        return mState;
    }

    public void setState(String pState) {
        mState = pState;
    }

    public String getZip() {
        return mZip;
    }

    public void setZip(String pZip) {
        mZip = pZip;
    }
}
