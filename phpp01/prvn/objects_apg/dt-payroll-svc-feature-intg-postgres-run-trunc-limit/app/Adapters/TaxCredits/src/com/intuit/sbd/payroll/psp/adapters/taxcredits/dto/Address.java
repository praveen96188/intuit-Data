package com.intuit.sbd.payroll.psp.adapters.taxcredits.dto;

/**
 * User: dweinberg
 * Date: Jan 22, 2010
 * Time: 12:50:09 PM
 */
public class Address {
    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;
    private String county;

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
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

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getAllAddressLinesAsOneLine() {
        if (getAddress2() != null && !getAddress2().equals("")) {
            return getAddress1() + "; " + getAddress2();
        } else {
            return getAddress1();
        }
    }

    public String getCityStateZipAsOneLine() {
        return getCity() + ", " + getState() + " " + getZip();
    }
}
