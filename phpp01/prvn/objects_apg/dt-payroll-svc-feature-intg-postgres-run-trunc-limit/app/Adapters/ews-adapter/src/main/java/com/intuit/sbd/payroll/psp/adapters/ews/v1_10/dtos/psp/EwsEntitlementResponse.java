package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.enums.EwsEntitlementStateCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "buyerEmailAddress",
        "billingAccountId",
        "subscriptionNumber",
        "subType",
        "state",
        "licenseNumber",
        "entitlementOfferingCode",
        "billingZip",
        "cancellationReason",
        "assetItemNumber",
        "hasMultipleActiveEINs",
        "paymentStatus"
})
public class EwsEntitlementResponse implements Cloneable {

    @XmlElement(name = "BuyerEmailAddress", required = false)
    protected String buyerEmailAddress;

    @XmlElement(name = "BillingAccountId", required = false)
    protected String billingAccountId;

    @XmlElement(name = "SubscriptionNumber", required = false)
    protected String subscriptionNumber;

    @XmlElement(name = "SubType", required = false)
    protected String subType;

    @XmlElement(name = "State", required = false)
    protected EwsEntitlementStateCode state;

    @XmlElement(name = "LicenseNumber", required = false)
    protected String licenseNumber;

    @XmlElement(name = "EntitlementOfferingCode", required = false)
    protected String entitlementOfferingCode;

    @XmlElement(name = "BillingZip", required = false)
    protected String billingZip;

    @XmlElement(name = "CancellationReason", required = false)
    protected String cancellationReason;

    @XmlElement(name = "AssetItemNumber", required = true)
    protected String assetItemNumber;

    @XmlElement(name = "HasMultipleActiveEINs", required = false)
    protected Boolean hasMultipleActiveEINs;

    @XmlElement(name = "PaymentStatus", required = true)
    protected String paymentStatus;

    public EwsEntitlementResponse clone() throws CloneNotSupportedException {
        return (EwsEntitlementResponse) super.clone();
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

    public EwsEntitlementStateCode getState() {
        return state;
    }

    public void setState(EwsEntitlementStateCode state) {
        this.state = state;
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

    public String getBillingZip() {
        return billingZip;
    }

    public void setBillingZip(String billingZip) {
        this.billingZip = billingZip;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getAssetItemNumber() {
        return assetItemNumber;
    }

    public void setAssetItemNumber(String assetItemNumber) {
        this.assetItemNumber = assetItemNumber;
    }

    public Boolean getHasMultipleActiveEINs() {
        return hasMultipleActiveEINs;
    }

    public void setHasMultipleActiveEINs(Boolean hasMultipleActiveEINs) {
        this.hasMultipleActiveEINs = hasMultipleActiveEINs;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String pPaymentStatus) {
        paymentStatus = pPaymentStatus;
    }
}
