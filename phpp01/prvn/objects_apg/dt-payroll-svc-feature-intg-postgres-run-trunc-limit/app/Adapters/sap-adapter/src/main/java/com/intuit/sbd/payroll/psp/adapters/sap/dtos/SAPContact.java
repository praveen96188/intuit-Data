/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPContact.java#2 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.adapters.sap.adapter.SAPTranslator;
import com.intuit.sbd.payroll.psp.domain.CommunicationType;
import com.intuit.sbd.payroll.psp.domain.ContactRole;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;
import java.util.Date;

/**
 * SAPContact -- DTO to represent a company for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SAPContact {

    @XmlElement(name = "Description", required = false)
    protected String description;

    @XmlElement(name = "ContactId", required = true)
    protected String contactId;

    @XmlElement(name = "LastName", required = true)
    protected String lastName;

    @XmlElement(name = "FirstName", required = true)
    protected String firstName;

    @XmlElement(name = "MiddleName", required = false)
    protected String middleName;

    @XmlElement(name = "CommunicationTypeCd", required = true)
    protected CommunicationType communicationTypeCd;

    @XmlElement(name = "Email", required = true)
    protected String email;

    @XmlElement(name = "PhoneNumber", required = true)
    protected String phoneNumber;

    @XmlElement(name = "Address", required = true)
    protected SAPAddress address;

    @XmlElement(name = "ContactRoleCd", required = true)
    protected ContactRole contactRoleCd;

    @XmlElement(name = "IsAccountSignatory", required = true)
    protected Boolean isAccountSignatory;

    @XmlElement(name = "FaxNumber", required = false)
    protected String faxNumber;

    @XmlElement(name = "Prefix", required = false)
    protected String prefix;

    @XmlElement(name = "Suffix", required = false)
    protected String suffix;

    @XmlElement(name = "JobTitle", required = false)
    protected String jobTitle;

    @XmlElement(name = "HasInvalidEmail", required = true)
    protected Boolean hasInvalidEmail;

    @XmlElement(name = "SocialSecurityNumber", required = false)
    protected String socialSecurityNumber;

    @XmlElement(name = "DateOfBirth", required = false)
    protected Date dateOfBirth;



    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String pJobTitle) {
        this.jobTitle = pJobTitle;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String pFaxNumber) {
        this.faxNumber = pFaxNumber;
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

    public SAPAddress getAddress() {
        return address;
    }

    public void setAddress(SAPAddress pAddress) {
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

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getHasInvalidEmail() {
        return hasInvalidEmail;
    }

    public void setHasInvalidEmail(Boolean pHasInvalidEmail) {
        hasInvalidEmail = pHasInvalidEmail;
    }

    public String getSocialSecurityNumber() {
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String pSocialSecurityNumber) {
        socialSecurityNumber = pSocialSecurityNumber;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date pDateOfBirth) {
       dateOfBirth = SAPTranslator.getGMTFormatDateWithDSTHandled(pDateOfBirth);
    }

}