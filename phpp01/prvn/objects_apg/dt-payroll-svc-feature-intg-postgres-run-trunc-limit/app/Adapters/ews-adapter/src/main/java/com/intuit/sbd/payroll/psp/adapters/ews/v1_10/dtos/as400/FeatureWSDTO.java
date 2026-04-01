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
@XmlType(name = "FeatureType", propOrder = {"featureName", "bank", "offerCode", "sourceCode", "priceCode",
        "mostCurrentTaxYear", "agreement", "status", "onHold"})
public class FeatureWSDTO implements Cloneable {
    private FeatureEnum featureName;
    private BankWSDTO bank;
    private String offerCode;
    private String sourceCode;
    private String priceCode;
    private String mostCurrentTaxYear;
    private AgreementWSDTO agreement;
    private StatusEnum status;
    private Boolean onHold;

    public FeatureWSDTO() {
        this.featureName = FeatureEnum.Assisted;
        this.bank = null;
        this.offerCode = null;
        this.sourceCode = "S00001";
        this.priceCode = null;
        this.mostCurrentTaxYear = null;
        this.agreement = null;
        this.status = null;
        this.onHold = null;
    }

    public FeatureWSDTO clone() throws CloneNotSupportedException {
        FeatureWSDTO clone = (FeatureWSDTO) super.clone();

        if (bank != null) {
            clone.setBank(bank.clone());
        }
        if (agreement != null) {
            clone.setAgreement(agreement.clone());
        }

        return clone;
    }

    @XmlElement(name = "FeatureName", nillable = false, required = false)
    public FeatureEnum getFeatureName() {
        return featureName;
    }

    public void setFeatureName(FeatureEnum featureName) {
        this.featureName = featureName;
    }

    @XmlElement(name = "Bank", nillable = false, required = false)
    public BankWSDTO getBank() {
        return bank;
    }

    public void setBank(BankWSDTO bank) {
        this.bank = bank;
    }

    @XmlElement(name = "OfferCode", nillable = false, required = false)
    public String getOfferCode() {
        return offerCode;
    }

    public void setOfferCode(String offerCode) {
        this.offerCode = offerCode;
    }

    @XmlElement(name = "SourceCode", nillable = false, required = false)
    public String getSourceCode() {
        return sourceCode;
    }

    @XmlElement(name = "PriceCode", nillable = false, required = false)
    public String getPriceCode() {
        return priceCode;
    }

    @XmlElement(name = "MostCurrentTaxYear", nillable = false, required = false)
    public String getMostCurrentTaxYear() {
        return mostCurrentTaxYear;
    }

    public void setMostCurrentTaxYear(String mostCurrentTaxYear) {
        this.mostCurrentTaxYear = mostCurrentTaxYear;
    }

    @XmlElement(name = "Agreement", nillable = false, required = false)
    public AgreementWSDTO getAgreement() {
        return agreement;
    }

    public void setAgreement(AgreementWSDTO agreement) {
        this.agreement = agreement;
    }

    @XmlElement(name = "Status", nillable = false, required = false)
    public StatusEnum getStatus() {
        return status;
    }

    public void setStatus(StatusEnum status) {
        this.status = status;
    }

    @XmlElement(name = "OnHold", nillable = false, required = false)
    public Boolean getOnHold() {
        return onHold;
    }

    public void setOnHold(Boolean onHold) {
        this.onHold = onHold;
    }

    public void validateFeatureName() throws Exception {
        if (this.featureName == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("FeatureName", "Feature"));
        }
    }

    public void validateBank() throws Exception {
        if (this.bank == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Bank"));
        }
    }

    public void validateOfferCode() throws Exception {
        if (!Validation.validateValue(this.offerCode, true, "^(\\P{M}\\p{M}*){0,100}$")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("OfferCode", "Feature"));
        }
    }

    public void validateAgreement() throws Exception {
        if (this.agreement == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Agreement"));
        }
    }

    public void validateOnHold() throws Exception {
        if (this.onHold == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("OnHold", "Feature"));
        }
    }

    public void validateStatus() throws Exception {
        if (this.status == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("Status", "Feature"));
        }
    }

}
