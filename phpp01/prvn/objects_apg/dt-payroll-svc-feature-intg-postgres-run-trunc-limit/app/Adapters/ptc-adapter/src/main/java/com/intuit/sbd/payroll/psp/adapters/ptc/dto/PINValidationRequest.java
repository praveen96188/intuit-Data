package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import org.apache.commons.lang.StringUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/13/12
 * Time: 3:54 PM
 */
@XmlRootElement()
@XmlType(name = "PINValidationRequest")
public class PINValidationRequest extends PSPCompanyRequest {

    private String ein;

    private String pin;

    @XmlElement(required = true)
    public String getEin() {
        return ein;
    }

    public void setEin(String pEin) {
        ein = pEin;
    }

    @XmlElement(required = true)
    public String getPin() {
        return pin;
    }

    public void setPin(String pPin) {
        pin = pPin;
    }

    @Override
    public String toString() {
        return "PINValidationRequest{" +
                "ein='" + ein + '\'' +
                ", sourceSystemCode='" + getSourceSystemCode() + '\'' +
                ", psid='" + getPsid() + '\'' +
                ", pin='" + (StringUtils.isEmpty(pin) ? "" : "*********") + '\'' +
                '}';
    }
}
