package com.intuit.sbd.payroll.psp.batchjobs.ThirdPartyJPMC;


import java.util.Calendar;


/**
 * Created by charithah418 on 6/1/15.
 */
public class JPMCEventMessage {
    //Primary Principal Information
    private String firstName;
    private String middleName;
    private String lastName;
    private Calendar dateOfBirth;
    private String ssn;
    private String email;
    private String phoneNumber;

    //Address Information
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String country;
    private String state;
    private String zipCode;

    //company Information
    private String sourceCompanyId;
    private String legalName;
    private String dba;
    private String industrySicCode;
    private String fedTaxId;
    private String realmId;

    //ofac report status whether update, add, delete.
    private String recordStatus;

    //Unique ID (PrimaryPrincipal)
    private String uniqueID;

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getRecordStatus() {
        return recordStatus;
    }

    public void setRecordStatus(String recordStatus) {
        this.recordStatus = recordStatus;
    }
    
    
    public JPMCEventMessage() {
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String pPhoneNumber) {
        phoneNumber = pPhoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        email = pEmail;
    }

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String pLegalName) {
        legalName = pLegalName;
    }

    public String getDba() {
        return dba;
    }

    public void setDba(String pDba) {
        dba = pDba;
    }

    public String getIndustrySicCode() {
        return industrySicCode;
    }

    public void setIndustrySicCode(String pIndustrySicCode) {
        industrySicCode = pIndustrySicCode;
    }

    public String getFedTaxId() {
        return fedTaxId;
    }

    public void setFedTaxId(String pFedTaxId) {
        fedTaxId = pFedTaxId;
    }

    public String getRealmId() { return realmId; }

    public void setRealmId(String pRealmId) { realmId = pRealmId; }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String pSsn) {
        ssn = pSsn;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String pMiddleName) {
        middleName = pMiddleName;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String pCountry) {
        country = pCountry;
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

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String pAddressLine1) {
        addressLine1 = pAddressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String pAddressLine2) {
        addressLine2 = pAddressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String pCity) {
        city = pCity;
    }

    public String getState() {
        return state;
    }

    public void setState(String pState) {
        state = pState;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String pZipCode) {
        zipCode = pZipCode;
    }

    public Calendar getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Calendar pDateOfBirth) {
        dateOfBirth = pDateOfBirth;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String pSourceCompanyId) {
        sourceCompanyId = pSourceCompanyId;
    }


}
