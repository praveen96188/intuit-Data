package com.intuit.sbd.payroll.psp.adapters.cdmadapter.workerscomp.dto;



/**
 * Created with IntelliJ IDEA.
 * User: afroza786
 * Date: 7/9/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */


public class AddressDTO {
    private String addressLine1;
    private String addressLine2;
    private String addressLine3;
    private String zipCode;
    private String zipExtension;
    private String city;
    private String state;
    private String country;

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String pAddressLine1) {
        addressLine1 = pAddressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String pAddressLine2) {
        addressLine2 = pAddressLine2;
    }

    public String getAddressLine3() {
        return addressLine3;
    }

    public void setAddressLine3(String pAddressLine3) {
        addressLine3 = pAddressLine3;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String pZipCode) {
        zipCode = pZipCode;
    }

    public String getZipExtension() {
        return zipExtension;
    }

    public void setZipExtension(String pZipExtension) {
        zipExtension = pZipExtension;
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

    public String getCountry() {
        return country;
    }

    public void setCountry(String pCountry) {
        country = pCountry;
    }
}
