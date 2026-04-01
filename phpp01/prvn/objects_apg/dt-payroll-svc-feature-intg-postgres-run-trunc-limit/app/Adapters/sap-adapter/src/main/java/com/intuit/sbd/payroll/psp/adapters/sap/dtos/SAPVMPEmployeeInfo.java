package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

/**
 * User: ihannur
 * Date: 6/19/13
 * Time: 3:51 PM
 */
public class SAPVMPEmployeeInfo {

    private String employeeSeq;
    private String firstName;
    private String lastName;
    private String middleName;
    private String socialSecurityNumber;
    private String emailAddress;
    private String userId;
    private String consumerId;

    public String getEmployeeSeq() {
        return employeeSeq;
    }

    public void setEmployeeSeq(String pEmployeeSeq) {
        employeeSeq = pEmployeeSeq;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String pFirstName) {
        firstName = pFirstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        lastName = pLastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String pMiddleName) {
        middleName = pMiddleName;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String pSocialSecurityNumber) {
        socialSecurityNumber = pSocialSecurityNumber;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String pEmailAddress) {
        emailAddress = pEmailAddress;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String pUserId) {
        userId = pUserId;
    }

    public String getConsumerId() {
        return consumerId;
    }

    public void setConsumerId(String pConsumerId) {
        consumerId = pConsumerId;
    }
}
