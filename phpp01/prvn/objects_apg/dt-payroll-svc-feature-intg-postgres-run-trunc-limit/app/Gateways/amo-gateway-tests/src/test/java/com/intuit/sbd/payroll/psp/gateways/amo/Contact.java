package com.intuit.sbd.payroll.psp.gateways.amo;

/**
 * Created by IntelliJ IDEA.
 * User: znorcross
 * Date: Jul 3, 2010
 * Time: 9:37:15 PM
 */
public class Contact {
    private String mEmailAddress;
    private String mFirstName;
    private String mMiddleName;
    private String mLastName;

    public Contact(String pEmailAddress, String pFirstName, String pMiddleName, String pLastName) {
        mEmailAddress = pEmailAddress;
        mFirstName = pFirstName;
        mMiddleName = pMiddleName;
        mLastName = pLastName;
    }

    public String getEmailAddress() {
        return mEmailAddress;
    }

    public void setEmailAddress(String pEmailAddress) {
        mEmailAddress = pEmailAddress;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String pFirstName) {
        mFirstName = pFirstName;
    }

    public String getMiddleName() {
        return mMiddleName;
    }

    public void setMiddleName(String pMiddleName) {
        mMiddleName = pMiddleName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String pLastName) {
        mLastName = pLastName;
    }
}
