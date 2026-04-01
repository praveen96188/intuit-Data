package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * User: dweinberg
 * Date: Jun 15, 2010
 * Time: 3:07:00 PM
 */
public class SAPEntitlementInfo {
    private String id;
    private String licenseNumber;
    private String eoc;
    private String orderNumber;
    private String customerId;
    private String contactEmail;
    private Date nextChargeDate;
    private String orderSourceCode;
    private String subscriptionNumber;
    private String status;
    private Date subscriptionStartDate;
    private String billingZipCode;
    private Date subscriptionEndDate;
    private boolean retail;

    // This is a duplicate field, added this in case if we don't find Entitlement Code as we may not have all the required information in messages always to find Entitlement code
    private String assetItemNumber;

    private SAPEntitlementCodeInfo entitlementCodeInfo;

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getEoc() {
        return eoc;
    }

    public void setEoc(String eoc) {
        this.eoc = eoc;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public Date getNextChargeDate() {
        return nextChargeDate;
    }

    public void setNextChargeDate(Date nextChargeDate) {
        this.nextChargeDate = nextChargeDate;
    }

    public SAPEntitlementCodeInfo getEntitlementCodeInfo() {
        return entitlementCodeInfo;
    }

    public void setEntitlementCodeInfo(SAPEntitlementCodeInfo entitlementCodeInfo) {
        this.entitlementCodeInfo = entitlementCodeInfo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrderSourceCode() {
        return orderSourceCode;
    }

    public void setOrderSourceCode(String orderSourceCode) {
        this.orderSourceCode = orderSourceCode;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBillingZipCode() {
        return billingZipCode;
    }

    public void setBillingZipCode(String billingZipCode) {
        this.billingZipCode = billingZipCode;
    }

    public Date getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    public void setSubscriptionStartDate(Date pSubscriptionStartDate) {
        subscriptionStartDate = pSubscriptionStartDate;
    }

    public String getAssetItemNumber() {
        return assetItemNumber;
    }

    public void setAssetItemNumber(String pAssetItemNumber) {
        assetItemNumber = pAssetItemNumber;
    }

    public Date getSubscriptionEndDate() {
        return subscriptionEndDate;
    }

    public void setSubscriptionEndDate(Date pSubscriptionEndDate) {
        subscriptionEndDate = pSubscriptionEndDate;
    }

    public boolean isRetail() {
        return retail;
    }

    public void setRetail(boolean pRetail) {
        retail = pRetail;
    }
}
