package com.intuit.sbd.payroll.psp.adapters.taxcredits.dto;

/**
 * User: dweinberg
 * Date: Jan 22, 2010
 * Time: 12:59:15 PM
 */
public class EmployerInfo {

    private String contactName;
    private String telephoneNumber;
    private String telephoneExtension;
    private String ein;
    private Address legalAddress;
    private String companyLegalName;
    private String contactEmail;
    private String offerCode;
    private String companyType;
    private String authSignerEmail;
    private String fiscalYearStartDateString;

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getTelephoneExtension() {
        return telephoneExtension;
    }

    public void setTelephoneExtension(String telephoneExtension) {
        this.telephoneExtension = telephoneExtension;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    public Address getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(Address legalAddress) {
        this.legalAddress = legalAddress;
    }

    public String getCompanyLegalName() {
        return companyLegalName;
    }

    public void setCompanyLegalName(String companyLegalName) {
        this.companyLegalName = companyLegalName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    public String getFiscalYearStartDateString() {
        return fiscalYearStartDateString;
    }

    public void setFiscalYearStartDateString(String fiscalYearStartDateString) {
        this.fiscalYearStartDateString = fiscalYearStartDateString;
    }

    public String getCompanyType() {
        return companyType;
    }

    public void setCompanyType(String companyType) {
        this.companyType = companyType;
    }

    public String getAuthSignerEmail() {
        return authSignerEmail;
    }

    public void setAuthSignerEmail(String authSignerEmail) {
        this.authSignerEmail = authSignerEmail;
    }
}
