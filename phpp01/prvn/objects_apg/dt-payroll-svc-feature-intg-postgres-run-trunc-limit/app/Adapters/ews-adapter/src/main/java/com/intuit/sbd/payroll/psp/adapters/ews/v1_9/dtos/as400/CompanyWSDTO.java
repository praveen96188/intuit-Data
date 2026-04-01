/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import java.util.Calendar;

/**
    @author Jeff Jones
 */
@XmlType(name = "CompanyType", propOrder = {"psid", "ein", "payrollAdmin", "primaryPrincipal", "secondaryPrincipal",
        "quickBooks", "legalInfo", "dba", "pin", "oldPIN", "pinExists", "w2DeliveryPref", "clientPacketDeliveryPref",
        "shippingAddress", "mailingAddress", "licenceNumber", "lastConnectedDateTime"})
public class CompanyWSDTO implements Cloneable {
    private String psid;
    private String ein;
    private ContactWSDTO payrollAdmin;
    private ContactWSDTO primaryPrincipal;
    private ContactWSDTO secondaryPrincipal;
    private QuickBooksWSDTO quickBooks;
    private LegalWSDTO legalInfo;
    private String dba;
    private String pin;
    private String oldPIN;
    private Boolean pinExists;
    private DeliveryEnum w2DeliveryPref;
    private DeliveryEnum clientPacketDeliveryPref;
    private AddressWSDTO shippingAddress;
    private AddressWSDTO mailingAddress;
    private String licenceNumber;
    private Calendar lastConnectedDateTime;

    public CompanyWSDTO() {
        this.psid = null;
        this.ein = null;
        this.payrollAdmin = null;
        this.primaryPrincipal = null;
        this.secondaryPrincipal = null;
        this.quickBooks = null;
        this.legalInfo = null;
        this.dba = null;
        this.pin = null;
        this.oldPIN = null;
        this.pinExists = null;
        this.w2DeliveryPref = null;
        this.clientPacketDeliveryPref = null;
        this.shippingAddress = null;
        this.mailingAddress = null;
        this.licenceNumber = null;
        this.lastConnectedDateTime = null;
    }
    
    public CompanyWSDTO clone() throws CloneNotSupportedException {
        CompanyWSDTO clone = (CompanyWSDTO) super.clone();

        if (payrollAdmin != null) {
            clone.setPayrollAdmin(payrollAdmin.clone());    
        }
        if (primaryPrincipal != null) {
            clone.setPrimaryPrincipal(primaryPrincipal.clone());
        }
        if (secondaryPrincipal != null) {
            clone.setSecondaryPrincipal(secondaryPrincipal.clone());
        }
        if (quickBooks != null) {
            clone.setQuickBooks(quickBooks.clone());
        }
        if (legalInfo != null) {
            clone.setLegalInfo(legalInfo.clone());
        }
        if (shippingAddress != null) {
            clone.setShippingAddress(shippingAddress.clone());
        }
        if (mailingAddress != null) {
            clone.setMailingAddress(mailingAddress.clone());
        }

        return clone;
    }

    @XmlElement(name = "PSID", nillable = false, required = false)
    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
    }

    @XmlElement(name = "EIN", nillable = false, required = false)
    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
    }

    @XmlElement(name = "PayrollAdmin", nillable = false, required = false)
    public ContactWSDTO getPayrollAdmin() {
        return payrollAdmin;
    }

    public void setPayrollAdmin(ContactWSDTO payrollAdmin) {
        this.payrollAdmin = payrollAdmin;
    }

    @XmlElement(name = "PrimaryPrincipal", nillable = false, required = false)
    public ContactWSDTO getPrimaryPrincipal() {
        return primaryPrincipal;
    }

    public void setPrimaryPrincipal(ContactWSDTO primaryPrincipal) {
        this.primaryPrincipal = primaryPrincipal;
    }

    @XmlElement(name = "SecondaryPrincipal", nillable = false, required = false)
    public ContactWSDTO getSecondaryPrincipal() {
        return secondaryPrincipal;
    }

    public void setSecondaryPrincipal(ContactWSDTO secondaryPrincipal) {
        this.secondaryPrincipal = secondaryPrincipal;
    }

    @XmlElement(name = "QuickBooks", nillable = false, required = false)
    public QuickBooksWSDTO getQuickBooks() {
        return quickBooks;
    }

    public void setQuickBooks(QuickBooksWSDTO quickBooks) {
        this.quickBooks = quickBooks;
    }

    @XmlElement(name = "LegalInfo", nillable = false, required = false)
    public LegalWSDTO getLegalInfo() {
        return legalInfo;
    }

    public void setLegalInfo(LegalWSDTO legalInfo) {
        this.legalInfo = legalInfo;
    }

    @XmlElement(name = "DBA", nillable = false, required = false)
    public String getDba() {
        return dba;
    }

    public void setDba(String dba) {
        this.dba = dba;
    }

    @XmlElement(name = "PIN", nillable = false, required = false)
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    @XmlElement(name = "OldPIN", nillable = false, required = false)
    public String getOldPIN() {
        return oldPIN;
    }

    public void setOldPIN(String oldPIN) {
        this.oldPIN = oldPIN;
    }

    @XmlElement(name = "PINExists", nillable = false, required = false)
    public Boolean getPinExists() {
        return pinExists;
    }

    public void setPinExists(Boolean pinExists) {
        this.pinExists = pinExists;
    }

    @XmlElement(name = "W2DeliveryPref", nillable = false, required = false)
    public DeliveryEnum getW2DeliveryPref() {
        return w2DeliveryPref;
    }

    public void setW2DeliveryPref(DeliveryEnum w2DeliveryPref) {
        this.w2DeliveryPref = w2DeliveryPref;
    }

    @XmlElement(name = "ClientPacketDeliveryPref", nillable = false, required = false)
    public DeliveryEnum getClientPacketDeliveryPref() {
        return clientPacketDeliveryPref;
    }

    public void setClientPacketDeliveryPref(DeliveryEnum clientPacketDeliveryPref) {
        this.clientPacketDeliveryPref = clientPacketDeliveryPref;
    }

    @XmlElement(name = "ShippingAddress", nillable = false, required = false)
    public AddressWSDTO getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(AddressWSDTO shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    @XmlElement(name = "MailingAddress", nillable = false, required = false)
    public AddressWSDTO getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(AddressWSDTO mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    @XmlElement(name = "LicenceNumber", nillable = false, required = false)
    public String getLicenceNumber() {
        return licenceNumber;
    }

    public void setLicenceNumber(String licenceNumber) {
        this.licenceNumber = licenceNumber;
    }

    @XmlElement(name = "LastConnectedDateTime", nillable = false, required = false)
    public Calendar getLastConnectedDateTime() {
        return lastConnectedDateTime;
    }

    public void setLastConnectedDateTime(Calendar lastConnectedDateTime) {
        this.lastConnectedDateTime = lastConnectedDateTime;
    }

    public void validatePsid() throws Exception {
        if (this.psid == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("PSID", "Company"));
        }

        if (!Validation.validateValue(this.psid, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Company"));
        }
    }

    public void validateEin() throws Exception {
        if (this.ein == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("EIN", "Company"));
        }

        if (!Validation.validateValue(this.ein, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("EIN", "Company"));
        }
    }

    public void validatePsidOrEin() throws Exception {
        if ((this.ein == null) && (this.psid == null)) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("PSID or EIN", "Company"));
        }

        if (this.psid != null) {
            if (!Validation.validateValue(this.psid, false, "\\p{Digit}{9,9}")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "Company"));
            }
        } else {
            if (!Validation.validateValue(this.ein, false, "\\p{Digit}{9,9}")) {
                throw new EwsException(EwsMessages.fieldDataNotValid("EIN", "Company"));
            }
        }
    }

    public void validatePayrollAdmin() throws Exception {
        if (this.payrollAdmin == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("PayrollAdmin"));
        }
    }

    public void validatePrimaryPrincipal() throws Exception {
        if (this.primaryPrincipal == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("PrimaryPrincipal"));
        }
    }

    public void validateSecondaryPrincipal() throws Exception {
        if (this.secondaryPrincipal == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("SecondaryPrincipal"));
        }
    }

    public void validateQuickBooks() throws Exception {
        if (this.quickBooks == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("QuickBooks"));
        }
    }

    public void validateLegalInfo() throws Exception {
        if (this.legalInfo == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("LegalInfo"));
        }
    }

    public void validateDba() throws Exception {
        if (!Validation.validateValue(this.dba, true, "^(\\P{M}\\p{M}*){0,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("DBA", "Company"));
        }
    }

    public void validate4Pin() throws Exception {
        if (!Validation.validateValue(this.pin, false, "^(\\P{M}\\p{M}*){4,50}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PIN", "Company"));
        }
    }

    public void validate8Pin() throws Exception {
        if (!Validation.validateValue(this.pin, false, "^(\\P{M}\\p{M}*){8,12}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PIN", "Company"));
        }
    }

    public void validateOldPIN() throws Exception {
        if (!Validation.validateValue(this.oldPIN, false, "\\p{Graph}{4,50}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("OldPIN", "Company"));
        }
    }

    public void validateMailingAddress() throws Exception {
        if (this.mailingAddress == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("MailingAddress"));
        }
    }

    public void validateLicenceNumber() throws Exception {
        if (!Validation.validateValue(this.licenceNumber, false, "^(\\P{M}\\p{M}*){0,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LicenceNumber", "Company"));
        }
    }
}
