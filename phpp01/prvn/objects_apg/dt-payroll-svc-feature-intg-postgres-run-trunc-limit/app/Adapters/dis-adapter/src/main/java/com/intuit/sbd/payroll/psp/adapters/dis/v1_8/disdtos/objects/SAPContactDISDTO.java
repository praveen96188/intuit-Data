package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.domain.CommunicationType;
import com.intuit.sbd.payroll.psp.domain.ContactRole;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPContact")
public class SAPContactDISDTO {
    @XmlElement(name = "JobTitle")
    private String jobTitle;

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String pJobTitle) {
        this.jobTitle = pJobTitle;
    }

    @XmlElement(name = "Prefix")
    private String prefix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @XmlElement(name = "Suffix")
    private String suffix;

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @XmlElement(name = "FaxNumber")
    private String faxNumber;

    public String getFaxNumber() {
        return faxNumber;
    }

    public void setFaxNumber(String pFaxNumber) {
        this.faxNumber = pFaxNumber;
    }

    @XmlElement(name = "LastName")
    private String lastName;

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String pLastName) {
        this.lastName = pLastName;
    }

    @XmlElement(name = "FirstName")
    private String firstName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String pFirstName) {
        this.firstName = pFirstName;
    }

    @XmlElement(name = "MiddleName")
    private String middleName;

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String pMiddleName) {
        this.middleName = pMiddleName;
    }

    @XmlElement(name = "CommunicationTypeCd")
    private CommunicationType communicationTypeCd;

    public CommunicationType getCommunicationTypeCd() {
        return communicationTypeCd;
    }

    public void setCommunicationTypeCd(CommunicationType pCommunicationTypeCd) {
        this.communicationTypeCd = pCommunicationTypeCd;
    }

    @XmlElement(name = "Email")
    private String email;

    public String getEmail() {
        return email;
    }

    public void setEmail(String pEmail) {
        this.email = pEmail;
    }

    @XmlElement(name = "PhoneNumber")
    private String phoneNumber;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String pPhoneNumber) {
        this.phoneNumber = pPhoneNumber;
    }

    @XmlElement(name = "Address")
    private SAPAddressDISDTO address;

    public SAPAddressDISDTO getAddress() {
        return address;
    }

    public void setAddress(SAPAddressDISDTO pAddress) {
        this.address = pAddress;
    }

    @XmlElement(name = "ContactRoleCd")
    private ContactRole contactRoleCd;

    public ContactRole getContactRoleCd() {
        return contactRoleCd;
    }

    public void setContactRoleCd(ContactRole pContactRoleCd) {
        this.contactRoleCd = pContactRoleCd;
    }

    @XmlElement(name = "IsAccountSignatory")
    private Boolean isAccountSignatory;

    public Boolean getAccountSignatory() {
        return isAccountSignatory;
    }

    public void setAccountSignatory(Boolean pAccountSignatory) {
        isAccountSignatory = pAccountSignatory;
    }

    @XmlElement(name = "ContactId")
    private String contactId;

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }}
