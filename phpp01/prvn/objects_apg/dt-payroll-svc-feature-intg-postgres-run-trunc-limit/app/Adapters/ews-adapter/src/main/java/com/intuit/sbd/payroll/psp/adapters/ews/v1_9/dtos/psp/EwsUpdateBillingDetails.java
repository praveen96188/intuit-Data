package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.*;

/**
 * @author Jeff Jones
 */

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "psid",
    "licenseNumber",
    "entitlementOfferingCode",    
    "ewsBillingDetails"
})
public class EwsUpdateBillingDetails extends EwsRequest implements Cloneable {

    @XmlElement(name = "PSID", required = true)
    protected String psid;

    @XmlElement(name = "LicenseNumber", required = true)
    protected String licenseNumber;

    @XmlElement(name = "EntitlementOfferingCode", required = true)
    protected String entitlementOfferingCode;

    @XmlElement(name = "BillingDetails", required = true)
    protected EwsBillingDetails ewsBillingDetails;

    public String getPsid() {
        return psid;
    }

    public void setPsid(String psid) {
        this.psid = psid;
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

    public EwsBillingDetails getEwsBillingDetails() {
        return ewsBillingDetails;
    }

    public void setEwsBillingDetails(EwsBillingDetails ewsBillingDetails) {
        this.ewsBillingDetails = ewsBillingDetails;
    }

    public void validate() throws Exception {
        super.validate();

        if (!Validation.validateValue(this.psid, false, "\\p{Digit}{9,9}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("PSID", "UpdateBillingDetails"));
        }

        if (!Validation.validateValue(this.licenseNumber, false, "^(\\P{M}\\p{M}*){1,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LicenseNumber", "UpdateBillingDetails"));
        }

        if (!Validation.validateValue(this.entitlementOfferingCode, false, "^(\\P{M}\\p{M}*){1,20}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("EntitlementOfferingCode", "UpdateBillingDetails"));
        }
        
        ewsBillingDetails.validate();
    }
}
