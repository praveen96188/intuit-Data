package com.intuit.sbd.payroll.psp.adapters.ivr.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: Jun 14, 2010
 * Time: 3:10:41 PM
 */
@XmlType(name = "ServiceKeyInfo")
public class ServiceKeyInfo {

    private String serviceKey;
    private String licenseNumber;
    private String eoc;

    @XmlElement(name = "ServiceKey", required = true)
    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    @XmlElement(name = "LicenseNumber", required = true)
    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    @XmlElement(name = "EOC", required = true)
    public String getEoc() {
        return eoc;
    }

    public void setEoc(String eoc) {
        this.eoc = eoc;
    }
}
