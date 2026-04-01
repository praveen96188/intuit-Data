package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsDeliveryType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "payrollAdmin",
        "quickBooks",
        "legalInfo",
        "dba",
        "w2DeliveryPreference",
        "clientPacketDeliveryPreference",
        "mailingAddress",
        "primaryPrincipal",
        "secondaryPrincipal"
})
public class EwsCompany extends EwsBaseCompany implements Cloneable {

    @XmlElement(name = "PayrollAdmin", required = true)
    protected EwsContact payrollAdmin;

    @XmlElement(name = "PrimaryPrincipal", required = true)
    protected EwsContact primaryPrincipal;

    @XmlElement(name = "SecondaryPrincipal", required = false)
    protected EwsContact secondaryPrincipal;

    @XmlElement(name = "QuickBooks", required = false)
    protected EwsQuickBooks quickBooks;

    @XmlElement(name = "LegalInfo", required = false)
    protected EwsLegalInfo legalInfo;

    @XmlElement(name = "DBA", required = false)
    protected String dba;

    @XmlElement(name = "W2DeliveryPreference", required = false, defaultValue = "mail")
    protected EwsDeliveryType w2DeliveryPreference;

    @XmlElement(name = "ClientPacketDeliveryPreference", required = false, defaultValue = "electronic")
    protected EwsDeliveryType clientPacketDeliveryPreference;

    @XmlElement(name = "MailingAddress", required = false)
    protected EwsAddress mailingAddress;

    public EwsCompany() {
        super();
    }

    public EwsCompany clone() throws CloneNotSupportedException {
        EwsCompany clone = (EwsCompany) super.clone();

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

        if (mailingAddress != null) {
            clone.setMailingAddress(mailingAddress.clone());
        }

        return clone;
    }

    public EwsContact getPayrollAdmin() {
        return payrollAdmin;
    }

    public void setPayrollAdmin(EwsContact payrollAdmin) {
        this.payrollAdmin = payrollAdmin;
    }

    public EwsContact getPrimaryPrincipal() {
        return primaryPrincipal;
    }

    public void setPrimaryPrincipal(EwsContact primaryPrincipal) {
        this.primaryPrincipal = primaryPrincipal;
    }

    public EwsContact getSecondaryPrincipal() {
        return secondaryPrincipal;
    }

    public void setSecondaryPrincipal(EwsContact secondaryPrincipal) {
        this.secondaryPrincipal = secondaryPrincipal;
    }

    public EwsQuickBooks getQuickBooks() {
        return quickBooks;
    }

    public void setQuickBooks(EwsQuickBooks quickBooks) {
        this.quickBooks = quickBooks;
    }

    public EwsLegalInfo getLegalInfo() {
        return legalInfo;
    }

    public void setLegalInfo(EwsLegalInfo legalInfo) {
        this.legalInfo = legalInfo;
    }

    public String getDba() {
        return dba;
    }

    public void setDba(String dba) {
        this.dba = dba;
    }

    public EwsDeliveryType getW2DeliveryPreference() {
        return w2DeliveryPreference;
    }

    public void setW2DeliveryPreference(EwsDeliveryType w2DeliveryPreference) {
        this.w2DeliveryPreference = w2DeliveryPreference;
    }

    public EwsDeliveryType getClientPacketDeliveryPreference() {
        return clientPacketDeliveryPreference;
    }

    public void setClientPacketDeliveryPreference(EwsDeliveryType clientPacketDeliveryPreference) {
        this.clientPacketDeliveryPreference = clientPacketDeliveryPreference;
    }

    public EwsAddress getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(EwsAddress mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    public void validate() throws Exception {

        if (this.ein == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("EIN", "Company"));
        }
        super.validate();

        payrollAdmin.validate();
        primaryPrincipal.validate();

        if (secondaryPrincipal != null) {
            secondaryPrincipal.validate();
        }

        if (quickBooks != null) {
            quickBooks.validate();
        }        

        if (legalInfo != null) {
            legalInfo.validate();
        }

        if (mailingAddress != null) {
            mailingAddress.validate();
        }

        if (!Validation.validateValue(this.realmId, true, "^(\\P{M}\\p{M}*){0,50}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("RealmId", "Company"));
        }
    }    
}
