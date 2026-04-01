package com.intuit.sbd.payroll.psp.adapters.ivr.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: Jun 14, 2010
 * Time: 2:25:26 PM
 */
@XmlRootElement()
@XmlType(name = "GetServiceKeyRequest")
public class GetServiceKeyRequest {

    private String mEin;
    private String mLicenseNumber;
    private String mEoc;

    @XmlElement(name = "EIN", required = true)
    public String getEin() {
        return mEin;
    }

    public void setEin(String pEin) {
        mEin = pEin;
    }

    @XmlElement(name = "LicenseNumber", required = true)
    public String getLicenseNumber() {
        return mLicenseNumber;
    }

    public void setLicenseNumber(String pLicenseNumber) {
        mLicenseNumber = pLicenseNumber;
    }

    @XmlElement(name = "EOC", required = true)
    public String getEoc() {
        return mEoc;
    }

    public void setEoc(String pEoc) {
        mEoc = pEoc;
    }

}
