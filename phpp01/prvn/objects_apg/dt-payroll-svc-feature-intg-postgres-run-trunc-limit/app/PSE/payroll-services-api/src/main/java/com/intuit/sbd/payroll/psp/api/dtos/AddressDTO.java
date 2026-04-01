/*
 * $Id: //psp/dev/PSE/PayrollServicesAPI/src/com/intuit/sbd/payroll/psp/api/dtos/AddressDTO.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.api.dtos;

import com.intuit.sbd.payroll.psp.domain.Address;
import com.intuit.sbd.payroll.psp.processes.ProcessResult;
import com.intuit.sbd.payroll.psp.processes.messages.EntityName;
import com.intuit.sbd.payroll.psp.util.Validator;

public class AddressDTO {
    private AddressDTOValidator validator = new AddressDTOValidator();

    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String city;
    private String state;
    private String zipCode;
    private String zipCodeExtension;
    private String country;

    public AddressDTOValidator getValidator() {
        return validator;
    }

    public void setValidator(AddressDTOValidator validator) {
        this.validator = validator;
    }

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

    public ProcessResult validateAddressDTO() {
        return validator.validate(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AddressDTO that = (AddressDTO) o;

        if (addressLine1 != null ? !addressLine1.equals(that.addressLine1) : that.addressLine1 != null) return false;
        if (addressLine2 != null ? !addressLine2.equals(that.addressLine2) : that.addressLine2 != null) return false;
        if (addressLine3 != null ? !addressLine3.equals(that.addressLine3) : that.addressLine3 != null) return false;
        if (city != null ? !city.equals(that.city) : that.city != null) return false;
        if (country != null ? !country.equals(that.country) : that.country != null) return false;
        if (state != null ? !state.equals(that.state) : that.state != null) return false;
        if (zipCode != null ? !zipCode.equals(that.zipCode) : that.zipCode != null) return false;
        if (zipCodeExtension != null ? !zipCodeExtension.equals(that.zipCodeExtension) : that.zipCodeExtension != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = addressLine1 != null ? addressLine1.hashCode() : 0;
        result = 31 * result + (addressLine2 != null ? addressLine2.hashCode() : 0);
        result = 31 * result + (addressLine3 != null ? addressLine3.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        result = 31 * result + (zipCodeExtension != null ? zipCodeExtension.hashCode() : 0);
        result = 31 * result + (country != null ? country.hashCode() : 0);
        return result;
    }
}
