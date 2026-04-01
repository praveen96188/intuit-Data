package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsDeliveryType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "ein",
        "payrollAdmin",
        "primaryPrincipal",
        "secondaryPrincipal",
        "legalInfo",
        "dba",
        "PinExists",
        "w2DeliveryPreference",
        "clientPacketDeliveryPreference",
        "mailingAddress",
        "realmId",
        "isOnHold",
        "onHoldReason",
        "industry"
})
public class EwsCompanyResponse implements Cloneable {

    @XmlElement(name = "EIN", required = false)
    protected String ein;

    @XmlElement(name = "PayrollAdmin", required = false)
    protected EwsContact payrollAdmin;

    @XmlElement(name = "PrimaryPrincipal", required = false)
    protected EwsContact primaryPrincipal;

    @XmlElement(name = "SecondaryPrincipal", required = false)
    protected EwsContact secondaryPrincipal;

    @XmlElement(name = "LegalInfo", required = false)
    protected EwsLegalInfo legalInfo;

    @XmlElement(name = "DBA", required = false)
    protected String dba;

    @XmlElement(name = "PinExists", required = false)
    protected boolean PinExists;

    @XmlElement(name = "W2DeliveryPreference", required = false)
    protected EwsDeliveryType w2DeliveryPreference;

    @XmlElement(name = "ClientPacketDeliveryPreference", required = false)
    protected EwsDeliveryType clientPacketDeliveryPreference;

    @XmlElement(name = "MailingAddress", required = false)
    protected EwsAddress mailingAddress;

    @XmlElement(name = "RealmId", required = false)
    protected String realmId;

    @XmlElement(name = "IsOnHold", required = false)
    protected boolean isOnHold;

    @XmlElement(name = "OnHoldReason", required = false)
    protected String onHoldReason;

    @XmlElement(name="industry", required = false)
    protected String industry;

    public EwsCompanyResponse clone() throws CloneNotSupportedException {
        EwsCompanyResponse clone = (EwsCompanyResponse) super.clone();

        if (payrollAdmin != null) {
            clone.setPayrollAdmin(payrollAdmin.clone());
        }

        if (primaryPrincipal != null) {
            clone.setPrimaryPrincipal(primaryPrincipal.clone());
        }

        if (secondaryPrincipal != null) {
            clone.setSecondaryPrincipal(secondaryPrincipal.clone());
        }

        if (legalInfo != null) {
            clone.setLegalInfo(legalInfo.clone());
        }

        if (mailingAddress != null) {
            clone.setMailingAddress(mailingAddress.clone());
        }

        return clone;
    }

    public String getEin() {
        return ein;
    }

    public void setEin(String ein) {
        this.ein = ein;
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

    public boolean isPinExists() {
        return PinExists;
    }

    public void setPinExists(boolean pinExists) {
        PinExists = pinExists;
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

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }

    public boolean isOnHold() {
        return isOnHold;
    }

    public void setOnHold(boolean onHold) {
        isOnHold = onHold;
    }

    public String getOnHoldReason() {
        return onHoldReason;
    }

    public void setOnHoldReason(String pOnHoldReason) {
        onHoldReason = pOnHoldReason;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String pIndustry) {
        industry = pIndustry;
    }
}
