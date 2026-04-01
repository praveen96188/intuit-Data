/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_10.utils.Validation;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */
@XmlType(name = "OfferType", propOrder = {"offerCode", "description"})
public class OfferWSDTO implements Cloneable {
    private String offerCode;
    private String description;

    public OfferWSDTO() {
        this.offerCode = null;
        this.description = null;
    }

    public OfferWSDTO clone() throws CloneNotSupportedException {
        return (OfferWSDTO) super.clone();
    }

    @XmlElement(name = "OfferCode", nillable = false, required = false)
    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    @XmlElement(name = "Description", nillable = false, required = false)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void validateOfferCode() throws Exception {
        if (!Validation.validateValue(this.offerCode, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("OfferCode", "Offer"));
        }
    }

}
