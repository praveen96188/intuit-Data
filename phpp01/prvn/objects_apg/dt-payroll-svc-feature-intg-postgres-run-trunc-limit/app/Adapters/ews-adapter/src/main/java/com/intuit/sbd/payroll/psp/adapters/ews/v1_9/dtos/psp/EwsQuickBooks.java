package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;
import com.intuit.sbd.payroll.psp.util.OFXAPPVERObject;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
        "appVersion",
        "licenseNumber"
})
public class EwsQuickBooks implements Cloneable {

    @XmlElement(name = "AppVersion", required = true)
    protected String appVersion;

    @XmlElement(name = "LicenseNumber", required = true)
    protected String licenseNumber;

    public EwsQuickBooks clone() throws CloneNotSupportedException {
        return (EwsQuickBooks) super.clone();
    }    

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public void validate() throws Exception {
        if (!Validation.validateValue(this.appVersion, false, OFXAPPVERObject.appVersionRegExPatten)) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AppVersion", "QuickBooks"));
        }

        if (!Validation.validateValue(this.getLicenseNumber(), false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LicenseNumber", "QuickBooks"));
        }
    }
}
