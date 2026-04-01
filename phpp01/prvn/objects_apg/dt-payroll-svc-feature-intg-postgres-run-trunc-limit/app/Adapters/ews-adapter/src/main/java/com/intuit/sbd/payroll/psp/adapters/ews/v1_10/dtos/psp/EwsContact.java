package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;
import com.intuit.sbd.payroll.psp.util.Validator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Calendar;

/**
    @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "title",
        "titleSuffix",
        "firstName",
        "middleName",
        "lastName",
        "jobTitle",
        "eMail",
        "workPhone",
        "homePhone",
        "address",
        "authenticationId",
        "socialSecurityNumber",
        "dateOfBirth"
})
public class EwsContact implements Cloneable {

    @XmlElement(name = "Title", required = false)
    protected String title;

    @XmlElement(name = "TitleSuffix", required = false)
    protected String titleSuffix;

    @XmlElement(name = "FirstName", required = true)
    protected String firstName;

    @XmlElement(name = "MiddleName", required = false)
    protected String middleName;

    @XmlElement(name = "LastName", required = true)
    protected String lastName;

    @XmlElement(name = "JobTitle", required = false)
    protected String jobTitle;

    @XmlElement(name = "EMail", required = true)
    protected String eMail;

    @XmlElement(name = "WorkPhone", required = true)
    protected String workPhone;

    @XmlElement(name = "HomePhone", required = false)
    protected String homePhone;

    @XmlElement(name = "Address", required = false)
    protected EwsAddress address;

    @XmlElement(name = "AuthenticationId", required = false)
    protected String authenticationId;

    @XmlElement(name = "SocialSecurityNumber", required = false)
    protected String socialSecurityNumber;

    @XmlElement(name = "DateOfBirth", required = false)
    protected Calendar dateOfBirth;

    public EwsContact clone() throws CloneNotSupportedException {
        EwsContact clone = (EwsContact) super.clone();

        if (address != null) {
            clone.setAddress(address.clone());
        }

        return clone;
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

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public EwsAddress getAddress() {
        return address;
    }

    public void setAddress(EwsAddress address) {
        this.address = address;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }

    public void setAuthenticationId(String authenticationId) {
        this.authenticationId = authenticationId;
    }

    public String getSocialSecurityNumber() {
        if(socialSecurityNumber != null)
        {
            socialSecurityNumber = socialSecurityNumber.replaceAll("-| ","");
        }
        return socialSecurityNumber;
    }

    public void setSocialSecurityNumber(String pSocialSecurityNumber) {

        socialSecurityNumber = pSocialSecurityNumber;
    }

    public Calendar getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Calendar pDateOfBirth) {
        dateOfBirth = pDateOfBirth;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.title, true, "^(\\P{M}\\p{M}*){0,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Title", "Contact"));
        }

        if (!Validation.validateValue(this.titleSuffix, true, "^(\\P{M}\\p{M}*){0,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("TitleSuffix", "Contact"));
        }

        if (!Validation.validateValue(this.firstName, false, "^(\\P{M}\\p{M}*){1,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("FirstName", "Contact"));
        }

        if (!Validation.validateValue(this.middleName, true, "^(\\P{M}\\p{M}*){0,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("MiddleName", "Contact"));
        }

        if (!Validation.validateValue(this.lastName, false, "^(\\P{M}\\p{M}*){1,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LastName", "Contact"));
        }

        if (!Validation.validateValue(this.jobTitle, true, "^(\\P{M}\\p{M}*){0,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("JobTitle", "Contact"));
        }

        if (!Validator.isValidEmail(this.eMail)) {
            throw new EwsException(EwsMessages.fieldDataNotValid("eMail", "Contact"));
        }

        if (!Validation.validateValue(this.workPhone, false, "^(\\P{M}\\p{M}*){1,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("WorkPhone", "Contact"));
        }

        if (!Validation.validateValue(this.homePhone, true, "^(\\P{M}\\p{M}*){0,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("HomePhone", "Contact"));
        }

        if (!Validation.validateValue(this.authenticationId, true, "^(\\P{M}\\p{M}*){0,50}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AuthenticationId", "Contact"));
        }

        if (this.address != null) {
            this.address.validate();
        }
    }

    public boolean isAuthenticationIdNullOrEmpty() {
        return !(this.authenticationId != null && this.authenticationId.length() > 0);
    }
}
