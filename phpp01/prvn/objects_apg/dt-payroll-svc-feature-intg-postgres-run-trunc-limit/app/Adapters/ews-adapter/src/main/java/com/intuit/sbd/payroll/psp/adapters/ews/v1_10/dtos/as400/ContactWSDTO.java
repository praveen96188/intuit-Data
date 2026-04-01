/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;
import com.intuit.sbd.payroll.psp.util.Validator;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */
@XmlType(name = "ContactType", propOrder = {"title", "titleSuffix", "firstName", "middleInitial", "lastName",
        "jobTitle", "email", "workPhone", "homePhone", "homeAddress"})
public class ContactWSDTO implements Cloneable {
    private String title;
    private String titleSuffix;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String jobTitle;
    private String email;
    private String workPhone;
    private String homePhone;
    private AddressWSDTO homeAddress;

    public ContactWSDTO() {
        this.title = null;
        this.titleSuffix = null;
        this.firstName = null;
        this.middleInitial = null;
        this.lastName = null;
        this.jobTitle = null;
        this.email = null;
        this.workPhone = null;
        this.homePhone = null;
        this.homeAddress = null;
    }

    public ContactWSDTO clone() throws CloneNotSupportedException {
        ContactWSDTO clone = (ContactWSDTO) super.clone();

        if (homeAddress != null) {
            clone.setHomeAddress(homeAddress.clone());
        }

        return clone;
    }

    @XmlElement(name = "Title", nillable = false, required = false)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @XmlElement(name = "TitleSuffix", nillable = false, required = false)
    public String getTitleSuffix() {
        return titleSuffix;
    }

    public void setTitleSuffix(String titleSuffix) {
        this.titleSuffix = titleSuffix;
    }

    @XmlElement(name = "FirstName", nillable = false, required = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @XmlElement(name = "MiddleInitial", nillable = false, required = false)
    public String getMiddleInitial() {
        return middleInitial;
    }

    public void setMiddleInitial(String middleInitial) {
        this.middleInitial = middleInitial;
    }

    @XmlElement(name = "LastName", nillable = false, required = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @XmlElement(name = "JobTitle", nillable = false, required = false)
    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    @XmlElement(name = "Email", nillable = false, required = false)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @XmlElement(name = "WorkPhone", nillable = false, required = false)
    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    @XmlElement(name = "HomePhone", nillable = false, required = false)
    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    @XmlElement(name = "HomeAddress", nillable = false, required = false)
    public AddressWSDTO getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(AddressWSDTO homeAddress) {
        this.homeAddress = homeAddress;
    }

    public void validateTitle() throws Exception {
        if (!Validation.validateValue(this.title, true, "^(\\P{M}\\p{M}*){0,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Title", "Contact"));
        }
    }

    public void validateTitleSuffix() throws Exception {
        if (!Validation.validateValue(this.titleSuffix, true, "^(\\P{M}\\p{M}*){0,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("TitleSuffix", "Contact"));
        }
    }

    public void validateFirstName() throws Exception {
        if (!Validation.validateValue(this.firstName, false, "^(\\P{M}\\p{M}*){1,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("FirstName", "Contact"));
        }
    }

    public void validateMiddleInitial() throws Exception {
        if (!Validation.validateValue(this.middleInitial, true, "^(\\P{M}\\p{M}*){0,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("MiddleInitial", "Contact"));
        }
    }

    public void validateLastName() throws Exception {
        if (!Validation.validateValue(this.lastName, false, "^(\\P{M}\\p{M}*){1,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LastName", "Contact"));
        }
    }

    public void validateJobTitle() throws Exception {
        if (!Validation.validateValue(this.jobTitle, true, "^(\\P{M}\\p{M}*){0,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("JobTitle", "Contact"));
        }
    }

    public void validateEmail() throws Exception {
        if (!Validator.isValidEmail(this.email)) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Email", "Contact"));
        }
    }

    public void validateWorkPhone() throws Exception {
        if (!Validation.validateValue(this.workPhone, false, "^(\\P{M}\\p{M}*){1,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("WorkPhone", "Contact"));
        }
    }

    public void validateHomePhone() throws Exception {
        if (!Validation.validateValue(this.homePhone, true, "^(\\P{M}\\p{M}*){0,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("HomePhone", "Contact"));
        }
    }

    public void validateHomeAddress() throws Exception {
        if (this.homeAddress == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("HomeAddress"));
        }
    }
    
}
