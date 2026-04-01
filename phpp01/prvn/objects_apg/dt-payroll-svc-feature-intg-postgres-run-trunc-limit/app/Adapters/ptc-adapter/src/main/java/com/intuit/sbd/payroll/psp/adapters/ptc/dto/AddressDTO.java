package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/14/12
 * Time: 5:19 PM
 */
@XmlRootElement()
@XmlType(name = "AddressDTO")
public class AddressDTO {
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zipCode;

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String pAddress1) {
        address1 = pAddress1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String pAddress2) {
        address2 = pAddress2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String pCity) {
        city = pCity;
    }

    public String getState() {
        return state;
    }

    public void setState(String pState) {
        state = pState;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String pZipCode) {
        zipCode = pZipCode;
    }
}
