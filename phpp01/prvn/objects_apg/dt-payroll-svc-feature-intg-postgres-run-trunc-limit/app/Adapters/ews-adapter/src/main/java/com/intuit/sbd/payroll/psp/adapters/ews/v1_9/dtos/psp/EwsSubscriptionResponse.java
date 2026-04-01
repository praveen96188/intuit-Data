package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEditionType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsTierType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "subscriptionNumber",
    "subType",
    "buyerEmailAddress",
    "billingAccountId",
    "licenseNumber",
    "entitlementOfferingCode",
    "edition",
    "tier",
    "assetItemNumber",
    "billingZip"
})
public class EwsSubscriptionResponse implements Cloneable {
    @XmlElement(name = "SubscriptionNumber", required = false)
    protected String subscriptionNumber;

    @XmlElement(name = "SubType", required = false)
    protected String subType;

    @XmlElement(name = "BuyerEmailAddress", required = true)
    protected String buyerEmailAddress;

    @XmlElement(name = "BillingAccountId", required = false)
    protected String billingAccountId;

    @XmlElement(name = "LicenseNumber", required = false)
    protected String licenseNumber;

    @XmlElement(name = "EntitlementOfferingCode", required = false)
    protected String entitlementOfferingCode;

    @XmlElement(name = "Edition", required = false)
    protected EwsEditionType edition;

    @XmlElement(name = "Tier", required = false)
    protected EwsTierType tier;

    @XmlElement(name = "AssetItemNumber", required = false)
    protected String assetItemNumber;

    @XmlElement(name = "BillingZip", required = false)
    protected String billingZip;

    public EwsSubscriptionResponse clone() throws CloneNotSupportedException {
        return (EwsSubscriptionResponse) super.clone();
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getBuyerEmailAddress() {
        return buyerEmailAddress;
    }

    public void setBuyerEmailAddress(String buyerEmailAddress) {
        this.buyerEmailAddress = buyerEmailAddress;
    }

    public String getBillingAccountId() {
        return billingAccountId;
    }

    public void setBillingAccountId(String billingAccountId) {
        this.billingAccountId = billingAccountId;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getEntitlementOfferingCode() {
        return entitlementOfferingCode;
    }

    public void setEntitlementOfferingCode(String entitlementOfferingCode) {
        this.entitlementOfferingCode = entitlementOfferingCode;
    }

    public EwsEditionType getEdition() {
        return edition;
    }

    public void setEdition(EwsEditionType edition) {
        this.edition = edition;
    }

    public EwsTierType getTier() {
        return tier;
    }

    public void setTier(EwsTierType tier) {
        this.tier = tier;
    }

    public String getAssetItemNumber() {
        return assetItemNumber;
    }

    public void setAssetItemNumber(String assetItemNumber) {
        this.assetItemNumber = assetItemNumber;
    }

    public String getBillingZip() {
        return billingZip;
    }

    public void setBillingZip(String billingZip) {
        this.billingZip = billingZip;
    }
}
