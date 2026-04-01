package com.intuit.sbd.payroll.psp.batchjobs.amo;

import com.intuit.iep.customeraccount.customeraccountbase.v1.PostalAddressType;


/**
 * User: dweinberg
 * Date: Jun 30, 2010
 * Time: 2:54:19 PM
 */
public class PostalAddressDescription {

    public PostalAddressDescription(PostalAddressType address) {
        this.address = address;
    }

    public PostalAddressDescription(PostalAddressType address, String description) {
        this.address = address;
        this.description = description;
    }

    private PostalAddressType address;
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PostalAddressType getAddress() {
        return address;
    }

    public void setAddress(PostalAddressType address) {
        this.address = address;
    }

    public String getAddressKey(){
        String addressKey = (address.getAddressLine() != null ? address.getAddressLine() : "") +
                (address.getCity() != null ? address.getCity() : "")+
                (address.getStateOrProvince() != null ? address.getStateOrProvince() : "")+
                (address.getPostalCode() != null ? address.getPostalCode() : "");
        return addressKey;
    }
}
