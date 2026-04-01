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
@XmlType(name = "LegalType", propOrder = {"legalName", "legalAddress1", "legalAddress2", "legalCity", "legalState",
        "legalZip"})
public class LegalWSDTO implements Cloneable {
    private String legalName;
    private String legalAddress1;
    private String legalAddress2;
    private String legalCity;
    private String legalState;
    private String legalZip;

    public LegalWSDTO() {
        this.legalName = null;
        this.legalAddress1 = null;
        this.legalAddress2 = null;
        this.legalCity = null;
        this.legalState = null;
        this.legalZip = null;
    }

    public LegalWSDTO clone() throws CloneNotSupportedException {
        return (LegalWSDTO) super.clone();
    }

    @XmlElement(name = "LegalName", nillable = false, required = false)
    public String getLegalName() {
        return legalName;
    }

    public void setLegalName(String legalName) {
        this.legalName = legalName;
    }

    @XmlElement(name = "LegalAddress1", nillable = false, required = false)
    public String getLegalAddress1() {
        return legalAddress1;
    }

    public void setLegalAddress1(String legalAddress1) {
        this.legalAddress1 = legalAddress1;
    }

    @XmlElement(name = "LegalAddress2", nillable = false, required = false)
    public String getLegalAddress2() {
        return legalAddress2;
    }

    public void setLegalAddress2(String legalAddress2) {
        this.legalAddress2 = legalAddress2;
    }

    @XmlElement(name = "LegalCity", nillable = false, required = false)
    public String getLegalCity() {
        return legalCity;
    }

    public void setLegalCity(String legalCity) {
        this.legalCity = legalCity;
    }

    @XmlElement(name = "LegalState", nillable = false, required = false)
    public String getLegalState() {
        return legalState;
    }

    public void setLegalState(String legalState) {
        this.legalState = legalState == null ? null: legalState.toUpperCase();
    }

    @XmlElement(name = "LegalZip", nillable = false, required = false)
    public String getLegalZip() {
        return legalZip;
    }

    public void setLegalZip(String legalZip) {
        this.legalZip = legalZip;
    }

    public void validateLegalName() throws Exception {
        if (!Validation.validateValue(this.legalName, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LegalName", "Legal"));
        }
    }

    public void validateLegalAddress1() throws Exception {
        if (!Validation.validateValue(this.legalAddress1, false, "^(\\P{M}\\p{M}*){1,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LegalAddress1", "Legal"));
        }
    }

    public void validateLegalAddress2() throws Exception {
        if (!Validation.validateValue(this.legalAddress2, true, "^(\\P{M}\\p{M}*){0,80}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LegalAddress2", "Legal"));
        }
    }

    public void validateLegalCity() throws Exception {
        if (!Validation.validateValue(this.legalCity, false, "^(\\P{M}\\p{M}*){1,255}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LegalCity", "Legal"));
        }
    }

    public void validateLegalState() throws Exception {
        if (!Validation.validateValue(this.legalState, false, "^(\\P{M}\\p{M}*){1,21}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LegalState", "Legal"));
        }
    }

    public void validateLegalZip() throws Exception {
        if (!Validation.validateValue(this.legalZip, false, "((\\d){5})(\\-)?((\\d){4})?$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("LegalZip", "Legal"));
        }
    }

}
