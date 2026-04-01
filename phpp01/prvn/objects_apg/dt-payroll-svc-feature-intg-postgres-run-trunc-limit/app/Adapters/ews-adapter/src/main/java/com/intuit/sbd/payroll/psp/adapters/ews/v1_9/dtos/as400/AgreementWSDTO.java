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
@XmlType(name = "AgreementType", propOrder = {"agreementName", "serviceType", "subscriptionNum", "serviceKey",
        "crisRowId", "subType", "priceType", "addEIN"})
public class AgreementWSDTO implements Cloneable {

    private String agreementName;
    private String serviceType;
    private String subscriptionNum;
    private String serviceKey;
    private String crisRowId;
    private String subType;
    private String priceType;
    private Boolean addEIN;

    public AgreementWSDTO() {
        this.agreementName = "Basic";
        this.serviceType = null;
        this.subscriptionNum = null;
        this.serviceKey = null;
        this.crisRowId = null;
        this.subType = null;
        this.priceType = "Standard";
        this.addEIN = null;
    }

    public AgreementWSDTO clone() throws CloneNotSupportedException {
        return (AgreementWSDTO) super.clone();
    }

    @XmlElement(name = "AgreementName", nillable = false, required = false)
    public String getAgreementName() {
        return agreementName;
    }

    @XmlElement(name = "ServiceType", nillable = false, required = false)
    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @XmlElement(name = "SubscriptionNum", nillable = false, required = false)
    public String getSubscriptionNum() {
        return subscriptionNum;
    }

    public void setSubscriptionNum(String subscriptionNum) {
        this.subscriptionNum = subscriptionNum;
    }

    @XmlElement(name = "ServiceKey", nillable = false, required = false)
    public String getServiceKey() {
        return serviceKey;
    }

    public void setServiceKey(String serviceKey) {
        this.serviceKey = serviceKey;
    }

    @XmlElement(name = "CRISRowId", nillable = false, required = false)
    public String getCrisRowId() {
        return crisRowId;
    }

    public void setCrisRowId(String crisRowId) {
        this.crisRowId = crisRowId;
    }

    @XmlElement(name = "SubType", nillable = false, required = false)
    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @XmlElement(name = "PriceType", nillable = false, required = false)
    public String getPriceType() {
        return priceType;
    }

    public void setPriceType(String priceType) {
        this.priceType = priceType;
    }

    @XmlElement(name = "AddEIN", nillable = false, required = false)
    public Boolean getAddEIN() {
        return addEIN;
    }

    public void setAddEIN(Boolean addEIN) {
        this.addEIN = addEIN;
    }

    public void setAgreementName(String agreementName) {
        this.agreementName = agreementName;
    }

    public void validateAgreementName() throws Exception {
        if (!Validation.validateValue(this.agreementName, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("AgreementName", "Agreement"));
        }
    }

    public void validateServiceType() throws Exception {
        if (!Validation.validateValue(this.serviceType, true, "^(\\P{M}\\p{M}*){0,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("ServiceType", "Agreement"));
        }
    }

    public void validateSubscriptionNum() throws Exception {
        if (!Validation.validateValue(this.subscriptionNum, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("SubscriptionNum", "Agreement"));
        }
    }

    public void validateServiceKey() throws Exception {
        if (!Validation.validateValue(this.serviceKey, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("ServiceKey", "Agreement"));
        }
    }

    public void validateCRISRowId() throws Exception {
        if (!Validation.validateValue(this.crisRowId, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("CRISRowId", "Agreement"));
        }
    }

    public void validateSubType() throws Exception {
        if (!Validation.validateValue(this.subType, false, "^(\\P{M}\\p{M}*){1,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("SubType", "Agreement"));
        }
    }

}
