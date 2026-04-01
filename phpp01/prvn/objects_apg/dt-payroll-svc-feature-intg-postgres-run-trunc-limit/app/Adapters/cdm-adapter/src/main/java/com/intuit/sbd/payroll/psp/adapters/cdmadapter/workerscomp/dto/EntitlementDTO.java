package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto;

/**
 * Author: Sriram Nutakki
 * Date created: 8/15/13
 */
public class EntitlementDTO {

    protected boolean active;
    protected boolean primary;
    protected String assetItemCode;
    protected String editionType;
    protected String subscriptionNumber;

    /**
     * Gets the value of the active property.
     *
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the value of the active property.
     *
     */
    public void setActive(boolean value) {
        this.active = value;
    }

    /**
     * Gets the value of the primary property.
     *
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * Sets the value of the primary property.
     *
     */
    public void setPrimary(boolean value) {
        this.primary = value;
    }

    /**
     * Gets the value of the assetItemCode property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getAssetItemCode() {
        return assetItemCode;
    }

    /**
     * Sets the value of the assetItemCode property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setAssetItemCode(String value) {
        this.assetItemCode = value;
    }

    /**
     * Gets the value of the editionType property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    public String getEditionType() {
        return editionType;
    }

    /**
     * Sets the value of the editionType property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setEditionType(String value) {
        this.editionType = value;
    }

    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    public void setSubscriptionNumber(String pSubscriptionNumber) {
        subscriptionNumber = pSubscriptionNumber;
    }
}
