/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPAddress.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlElement;

/**
 * SAPAddress -- DTO to represent an address for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SAPAddress {

    @XmlElement(name = "Description", required = true)
    protected String description;

    @XmlElement(name = "AddressLine1", required = true)
    protected String addressLine1;

    @XmlElement(name = "AddressLine2", required = false)
    protected String addressLine2;

    @XmlElement(name = "AddressLine3", required = false)
    protected String addressLine3;

    @XmlElement(name = "City", required = true)
    protected String city;

    @XmlElement(name = "State", required = true)
    protected String state;

    @XmlElement(name = "ZipCode", required = true)
    protected String zipCode;

    @XmlElement(name = "ZipCodeExtension", required = false)
    protected String zipCodeExtension;

    @XmlElement(name = "Country", required = true)
    protected String country;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String pAddressLine1) {
        this.addressLine1 = pAddressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String pAddressLine2) {
        this.addressLine2 = pAddressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String pAddressLine3) {
        this.addressLine3 = pAddressLine3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String pCity) {
        this.city = pCity;
    }

    public String getState() {
        return state;
    }

    public void setState(String pState) {
        this.state = pState;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String pZipCode) {
        this.zipCode = pZipCode;
    }

    public String getZipCodeExtension() {
        return zipCodeExtension;
    }

    public void setZipCodeExtension(String pZipCodeExtension) {
        this.zipCodeExtension = pZipCodeExtension;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String pCountry) {
        this.country = pCountry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}