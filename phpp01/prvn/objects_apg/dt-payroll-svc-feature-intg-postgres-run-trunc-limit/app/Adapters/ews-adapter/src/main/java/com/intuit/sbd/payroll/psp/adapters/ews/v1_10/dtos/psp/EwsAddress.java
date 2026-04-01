package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.psp;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {
    "addressLine1",
    "addressLine2",
    "city",
    "state",
    "zip"
})
public class EwsAddress implements Cloneable {

    @XmlElement(name = "AddressLine1", required = true)
    protected String addressLine1;

    @XmlElement(name = "AddressLine2", required = false)
    protected String addressLine2;

    @XmlElement(name = "City", required = true)
    protected String city;

    @XmlElement(name = "State", required = true)
    protected String state;

    @XmlElement(name = "Zip", required = true)
    protected String zip;

    public EwsAddress clone() throws CloneNotSupportedException {
        return (EwsAddress) super.clone();
    }

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

    public void validate() throws Exception {        
        if (!Validation.validateValue(this.addressLine1, false, "^(\\P{M}\\p{M}*){1,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AddressLine1", "Address"));
        }

        if (!Validation.validateValue(this.addressLine2, true, "^(\\P{M}\\p{M}*){0,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AddressLine2", "Address"));
        }

        if (!Validation.validateValue(this.city, false, "^(\\P{M}\\p{M}*){1,255}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("City", "Address"));
        }

        if (!Validation.validateValue(this.state, false, "^(\\P{M}\\p{M}*){1,21}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("State", "Address"));
        }

        if (!Validation.validateValue(this.zip, false, "((\\d){5})(\\-)?((\\d){4})?$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("Zip", "Address"));
        }
    }

}
