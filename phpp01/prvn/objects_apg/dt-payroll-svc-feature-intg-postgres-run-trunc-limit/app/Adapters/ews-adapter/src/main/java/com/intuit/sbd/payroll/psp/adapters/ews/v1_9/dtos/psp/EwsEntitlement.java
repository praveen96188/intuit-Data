package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsEditionType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.enums.EwsTierType;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;
import com.intuit.sbd.payroll.psp.security.PspPrincipal;
import com.intuit.sbd.payroll.psp.util.Validator;

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
    "licenseNumber",
    "entitlementOfferingCode",
    "edition",
    "tier",
    "assetItemNumber",
    "priceCode",
    "addEin",
    "ewsBillingDetails"
})
public class EwsEntitlement extends EwsBaseEntitlement implements Cloneable {
    @XmlElement(name = "BuyerEmailAddress", required = true)
    protected String buyerEmailAddress;

    @XmlElement(name = "LicenseNumber", required = true)
    protected String licenseNumber;

    @XmlElement(name = "EntitlementOfferingCode", required = true)
    protected String entitlementOfferingCode;

    @XmlElement(name = "Edition", required = false)
    protected EwsEditionType edition;

    @XmlElement(name = "Tier", required = false)
    protected EwsTierType tier;

    @XmlElement(name = "AssetItemNumber", required = true)
    protected String assetItemNumber;

    @XmlElement(name = "PriceCode", required = false)
    protected String priceCode;

    @XmlElement(name = "AddEin", required = true)
    protected Boolean addEin;

    @XmlElement(name = "BillingDetails", required = false)
    protected EwsBillingDetails ewsBillingDetails;

    public EwsEntitlement clone() throws CloneNotSupportedException {
        EwsEntitlement clone = (EwsEntitlement) super.clone();

        if (ewsBillingDetails != null) {
            clone.setEwsBillingDetails(ewsBillingDetails.clone());
        }

        return clone;
    }

    public String getBuyerEmailAddress() {
        return buyerEmailAddress;
    }

    public void setBuyerEmailAddress(String buyerEmailAddress) {
        this.buyerEmailAddress = buyerEmailAddress;
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

    public String getPriceCode() {
        return priceCode;
    }

    public void setPriceCode(String priceCode) {
        this.priceCode = priceCode;
    }

    public Boolean getAddEin() {
        return addEin;
    }

    public void setAddEin(Boolean addEin) {
        this.addEin = addEin;
    }

    public EwsBillingDetails getEwsBillingDetails() {
        return ewsBillingDetails;
    }

    public void setEwsBillingDetails(EwsBillingDetails ewsBillingDetails) {
        this.ewsBillingDetails = ewsBillingDetails;
    }

    public void validate() throws Exception {
        PspPrincipal principal = Application.getCurrentPrincipal();
        if (!principal.isAgent()) {
            if (!Validator.isValidEmail(this.buyerEmailAddress)) {
                throw new EwsException(EwsMessages.fieldDataNotValid("BuyerEmailAddress", "Entitlement"));
            }
        }

        if (!Validation.validateValue(this.licenseNumber, false, "^(\\P{M}\\p{M}*){1,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LicenseNumber", "Entitlement"));
        }

        if (!Validation.validateValue(this.entitlementOfferingCode, false, "^(\\P{M}\\p{M}*){1,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("EntitlementOfferingCode", "Entitlement"));
        }

        if (!Validation.validateValue(this.assetItemNumber, false, "^(\\P{M}\\p{M}*){1,7}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AssetItemNumber", "Entitlement"));
        }

        if (!Validation.validateValue(this.priceCode, true, "^(\\P{M}\\p{M}*){0,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PriceCode", "Entitlement"));
        }               
    }
}
