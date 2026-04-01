package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.address;


/**
 * DTO used for transfering Address information
 */
public class AddressDTO {

    private String address1;
    private String city;
    private String state;
    private String zipCode;


    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

}
