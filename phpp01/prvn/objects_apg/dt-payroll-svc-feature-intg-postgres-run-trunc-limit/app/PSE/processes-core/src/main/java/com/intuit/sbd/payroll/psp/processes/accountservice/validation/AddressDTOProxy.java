package com.intuit.sbd.payroll.psp.processes.accountservice.validation;


import javax.validation.constraints.Max;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import com.intuit.payments.cdm.v2.client.enums.AddressTypeEnum;
import com.intuit.sbd.payroll.psp.api.dtos.AddressDTO;
import com.intuit.sbd.payroll.psp.processes.accountservice.validation.constraints.CustomRegex;

/**
 * Validation rules in UI
 * 
 *  US: {
 *         maxAddressLineLength: 40,
 *         minValidationLengthLine1: 2,
 *         minValidationLengthLine2: 1,
 *         addressLineRegex: /^[a-zA-Z0-9#&'(),-./\s]*$/,
 *         maxLengthCity: 25,
 *         minLengthCity: 2,
 *         minValidationLengthPostalCode: 5,
 *         maxValidationLengthPostalCode: 10,
 *         postCodeRegex: /^\d{5}(?:[-]\d{4})?$/,
 *         city: /^[\p{L}',-.& ]*$/u,
 *         poBox: /^\s*(p\s*\.?\s*[o0]\s*\.?\s*(box|bx|b)\s*\.?\s*\w?[#\-.:]?\s*\w?(\d+.*)*)|((?:Post(?:al)?\s*\.?\s*(?:Office\s*)?\s*\.?\s*(box|bx)\s*\.?\s*\w?[#\-.:]?\s*\w?(\d+.*)*))|(^\s*(box|bx)\s*\.?\s*\w?[#\-.:]?\s*\w?(\d+).*)$/i
 *     },
 */

public class AddressDTOProxy {

    @Size(min = 2, max = 40, message
            = "Address line 1 must be in range [2,40]")
    @CustomRegex(value = "\\s*(p\\s*\\.?\\s*[o0]\\s*\\.?\\s*(box|bx|b)\\s*\\.?\\s*\\w?[#\\-.:]?\\s*\\w?(\\d+.*)*)|((?:Post(?:al)?\\s*\\.?\\s*(?:Office\\s*)?\\s*\\.?\\s*(box|bx)\\s*\\.?\\s*\\w?[#\\-.:]?\\s*\\w?(\\d+.*)*))|(^\\s*(box|bx)\\s*\\.?\\s*\\w?[#\\-.:]?\\s*\\w?(\\d+).*)$",
    message = "Can not be a PO box address")
    private String addressLine1;

    //TODO check if this validation applies for addressLine2 also
    /*@CustomRegex(value = "\\s*(p\\s*\\.?\\s*[o0]\\s*\\.?\\s*(box|bx|b)\\s*\\.?\\s*\\w?[#\\-.:]?\\s*\\w?(\\d+.*)*)|((?:Post(?:al)?\\s*\\.?\\s*(?:Office\\s*)?\\s*\\.?\\s*(box|bx)\\s*\\.?\\s*\\w?[#\\-.:]?\\s*\\w?(\\d+.*)*))|(^\\s*(box|bx)\\s*\\.?\\s*\\w?[#\\-.:]?\\s*\\w?(\\d+).*)$",
            message = "Can not be a PO box address")*/
    private String addressLine2;
    private String addressLine3;

    @Size(min = 2, max =25, message
            = "City length must be in range [2,25]")
    @Pattern(regexp = "^[\\p{L}',-.& ]*$", message = "City name contains invalid character(s)")
    private String city;

    @Size(min = 2, max =2, message
            = "State length must be 2")
    private String region;

    @Size(min = 5, max =10, message
            = "Zip code length must be in range [5,10]")
    @Pattern(regexp = "^\\d{5,10}" ,message=" Zip Code can only be digits and length in range [5,10]")
    private String postalCode;
    private String zipCodeExtension;
    private String country;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String addressLine3) {
        this.addressLine3 = addressLine3;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getZipCodeExtension() {
        return zipCodeExtension;
    }

    public void setZipCodeExtension(String zipCodeExtension) {
        this.zipCodeExtension = zipCodeExtension;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

}
