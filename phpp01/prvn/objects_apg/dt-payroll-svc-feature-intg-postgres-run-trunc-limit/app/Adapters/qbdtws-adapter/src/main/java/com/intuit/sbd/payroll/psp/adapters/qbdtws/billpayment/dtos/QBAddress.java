package com.intuit.sbd.payroll.psp.adapters.qbdtws.billpayment.dtos;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "QBAddress")
public class QBAddress {

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String country;
    private String state;
    private String zipCode;
    private String zipCodeExtension;

    @XmlElement(name = "AddressLine1", required = true, nillable = false)
    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String pAddressLine1) {
        addressLine1 = pAddressLine1;
    }

    @XmlElement(name = "AddressLine2")
    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String pAddressLine2) {
        addressLine2 = pAddressLine2;
    }

    @XmlElement(name = "AddressLine3")
    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String pAddressLine3) {
        addressLine3 = pAddressLine3;
    }

    @XmlElement(name = "City", required = true, nillable = false)
    public String getCity() {
        return city;
    }

    public void setCity(String pCity) {
        city = pCity;
    }

    @XmlElement(name = "Country")
    public String getCountry() {
        return country;
    }

    public void setCountry(String pCountry) {
        country = pCountry;
    }

    @XmlElement(name = "State", required = true, nillable = false)
    public String getState() {
        return state;
    }

    public void setState(String pState) {
        state = pState;
    }

    @XmlElement(name = "ZipCode", required = true, nillable = false)
    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String pZipCode) {
        zipCode = pZipCode;
    }

    @XmlElement(name = "ZipCodeExtension")
    public String getZipCodeExtension() {
        return zipCodeExtension;
    }

    public void setZipCodeExtension(String pZipCodeExtension) {
        zipCodeExtension = pZipCodeExtension;
    }
}
