/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
    @author Jeff Jones
 */
@XmlType(name = "AddressType", propOrder = {"address1", "address2", "city", "state", "zip"})
public class AddressWSDTO implements Cloneable {

    private String address1;
    private String address2;
    private String city;
    private String state;
    private String zip;

    public AddressWSDTO() {
        this.address1 = null;
        this.address2 = null;
        this.city = null;
        this.state = null;
        this.zip = null;
    }

    public AddressWSDTO clone() throws CloneNotSupportedException {
        return (AddressWSDTO) super.clone();
    }

    @XmlElement(name = "Address1", nillable = false, required = false)
    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    @XmlElement(name = "Address2", nillable = false, required = false)
    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    @XmlElement(name = "City", nillable = false, required = false)
    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    @XmlElement(name = "State", nillable = false, required = false)
    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state == null ? null: state.toUpperCase();
    }

    @XmlElement(name = "Zip", nillable = false, required = false)
    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }

    public void validateAddress1() throws Exception {
        if (!Validation.validateValue(this.address1, false, "^(\\P{M}\\p{M}*){1,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Address1", "Address"));
        }
    }

    public void validateAddress2() throws Exception {
        if (!Validation.validateValue(this.address2, true, "^(\\P{M}\\p{M}*){0,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Address2", "Address"));
        }
    }

    public void validateCity() throws Exception {
        if (!Validation.validateValue(this.city, false, "^(\\P{M}\\p{M}*){1,255}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("City", "Address"));
        }
    }

    public void validateState() throws Exception {
        if (!Validation.validateValue(this.state, false, "^(\\P{M}\\p{M}*){1,21}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("State", "Address"));
        }
    }

    public void validateZip() throws Exception {
        if (!Validation.validateValue(this.zip, false, "((\\d){5})(\\-)?((\\d){4})?$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Zip", "Address"));
        }
    }

}
