package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/14/12
 * Time: 5:07 PM
 */
@XmlRootElement()
@XmlType(name = "LegalInfo")
public class LegalInfo {
    private String legalName;
    private AddressDTO legalAddress;

    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String pLegalName) {
        legalName = pLegalName;
    }

    public AddressDTO getLegalAddress() {
        return legalAddress;
    }

    public void setLegalAddress(AddressDTO pLegalAddress) {
        legalAddress = pLegalAddress;
    }
}
