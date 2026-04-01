package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Address DIS DTO that will be returned by the WS
 * Most address fields are option when getting or setting an address,
 *    which allows the update address request to only pass in fields
 *    that they want to change.
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPAddress", propOrder = {"addressLine1", "addressLine2", "addressLine3", "city", "state", "country", "zipCode","zipExtension"})
public class SAPAddressDISDTO {

    @XmlElement(name = "AddressLine1")
    private String addressLine1;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    @XmlElement(name = "AddressLine2")
    private String addressLine2;

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    @XmlElement(name = "AddressLine3")
    private String addressLine3;

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    @XmlElement(name = "City")
    private String city;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @XmlElement(name = "State")
    private String state;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    @XmlElement(name = "Country")
    private String country;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @XmlElement(name = "ZipCode")
    private String zipCode;

    public String getZipCode() {
        return zipCode;
    }

    public void setZip(String zipCode) {
        this.zipCode = zipCode;
    }

    @XmlElement(name = "ZipExtension")
    private String zipExtension;

    public String getZipExtension() {
        return zipExtension;
    }

    public void setZipCodeExtension(String zipExtension) {
        this.zipExtension = zipExtension;
    }

}
