/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/ContactDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;
import com.intuit.sbd.payroll.psp.domain.CommunicationType;
import com.intuit.sbd.payroll.psp.domain.ContactRole;

public class ContactDTO {
    private String contactId;
    private String title;
    private String titleSuffix;
    private String jobTitle;
    private String lastName;
    private String firstName;
    private String middleName;
    private CommunicationType communicationTypeCd;
    private String email;
    private String phoneNumber;
    private String secondPhoneNumber;
    private String faxNumber;
    private AddressDTO address;
    private ContactRole contactRoleCd;
    private Boolean isAccountSignatory;
    private String iAMAuthenticationId;
    private String socialSecurityNumber;
    private DateDTO dateOfBirth;

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitleSuffix() {
        return titleSuffix;
    }

    public void setTitleSuffix(String titleSuffix) {
        this.titleSuffix = titleSuffix;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String faxNumber) {
        this.faxNumber = faxNumber;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        this.lastName = pLastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String pFirstName) {
        this.firstName = pFirstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String pMiddleName) {
        this.middleName = pMiddleName;
    }

    public CommunicationType getCommunicationTypeCd() {
        return communicationTypeCd;
    }

    public void setCommunicationTypeCd(CommunicationType pCommunicationTypeCd) {
        this.communicationTypeCd = pCommunicationTypeCd;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        this.email = pEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String pPhoneNumber) {
        this.phoneNumber = pPhoneNumber;
    }

    public String getSecondPhoneNumber() {
        return secondPhoneNumber;
    }

    public void setSecondPhoneNumber(String secondPhoneNumber) {
        this.secondPhoneNumber = secondPhoneNumber;
    }

    public AddressDTO getAddress() {
        return address;
    }

    public void setAddress(AddressDTO pAddress) {
        this.address = pAddress;
    }

    public ContactRole getContactRoleCd() {
        return contactRoleCd;
    }

    public void setContactRoleCd(ContactRole pContactRoleCd) {
        this.contactRoleCd = pContactRoleCd;
    }

    public Boolean getAccountSignatory() {
        return isAccountSignatory;
    }

    public void setAccountSignatory(Boolean pAccountSignatory) {
        isAccountSignatory = pAccountSignatory;
    }

    public String getIAMAuthenticationId() {
        return iAMAuthenticationId;
    }

    public void setIAMAuthenticationId(String iAMAuthenticationId) {
        this.iAMAuthenticationId = iAMAuthenticationId;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String pSocialSecurityNumber) {
        socialSecurityNumber = pSocialSecurityNumber;
    }

    public DateDTO getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(DateDTO pDateOfBirth) {
        dateOfBirth = pDateOfBirth;
    }

    public String getFullName() {
        String lastName = getLastName();
        lastName = (lastName == null) ? "" : lastName;

        String firstName = getFirstName();
        firstName = (firstName == null) ? "" : firstName;

        String fullName = lastName;
        if (fullName.length() > 0) {
           fullName+=", ";
        }
        fullName += firstName;

        String middleName = getMiddleName();
        middleName = (middleName == null) ? "" : middleName;
        fullName += " " + middleName;

        return fullName.trim();
    }
    
    public ProcessResult validateContactDTO() {
        ProcessResult validationResult = new ProcessResult();

        String contactFullName = firstName + ' ' + lastName;

        if (contactId==null) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "SourceContactId");
        }

        if (firstName == null ||
                !(Validator.isValidLength(firstName, 1, 80))) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "FirstName");
        }

        if (lastName == null ||
                !(Validator.isValidLength(lastName, 1, 80))) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "LastName");
        }

        if (!Validator.isValidLength(middleName, 0, 80)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "MiddleName");
        }

        if (email == null || !Validator.isValidEmail(email)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "Email");
        }

        if (phoneNumber == null || !(Validator.isValidLength(phoneNumber, 1, 80))) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "Phone");
        }

        if (!Validator.isValidLength(title, 0, 20)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "Title");
        }

        if (!Validator.isValidLength(titleSuffix, 0, 20)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "TitleSuffix");
        }

        if (!Validator.isValidLength(jobTitle, 0, 80)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "JobTitle");
        }
        
        if (!Validator.isValidLength(faxNumber, 0, 80)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "Fax");
        }

        if (!Validator.isValidLength(secondPhoneNumber, 0, 80)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "SecondPhone");
        }

        if (address != null) {
            //address = null is no change; address = <null, null...> = no address or delete address
            //only check if that's not the case
            if (! address.equals(new AddressDTO())) {
            validationResult.merge(address.validateAddressDTO());
        }
        }

        if (contactRoleCd == null) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "ContactRoleCd");
        }

        if (isAccountSignatory == null) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "IsAccountSignatory");
        }

        if (socialSecurityNumber != null && socialSecurityNumber.trim().length() > 0 && !Validator.isValidLength(socialSecurityNumber, 9, 9)) {
            validationResult.getMessages().InvalidValue(EntityName.Contact, contactFullName, "SocialSecurityNumber");
        }
        return validationResult;
    }

    public static String generateContactKey(ContactDTO pContact) {
        String contactRoleCode = pContact.getContactRoleCd().toString();
        contactRoleCode = (contactRoleCode == null) ? "" : contactRoleCode;

        String key = contactRoleCode;

        String firstName = pContact.getFirstName();
        firstName = (firstName == null) ? "" : firstName;
        key += firstName;

        String lastName = pContact.getLastName();
        lastName = (lastName == null) ? "" : lastName;
        key += lastName;

        String middleName = pContact.getMiddleName();
        middleName = (middleName == null) ? "" : middleName;
        key += middleName;

        return key;
    }
}
