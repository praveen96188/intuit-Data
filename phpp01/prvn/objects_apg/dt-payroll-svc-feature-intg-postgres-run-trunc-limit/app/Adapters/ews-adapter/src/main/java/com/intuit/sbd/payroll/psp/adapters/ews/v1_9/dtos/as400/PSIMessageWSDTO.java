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

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
    @author Jeff Jones
 */
@XmlRootElement(name = "PSIMessage")
@XmlType(name = "PSIMessage", propOrder = {"action", "respStatus", "company", "feature", "fee", "dateTimeStamp", "serverId",
        "serverPassword", "forceRandomDollar", "offer"})
public class PSIMessageWSDTO implements Cloneable {
    private ActionEnum action;
    private ResponseWSDTO respStatus;
    private CompanyWSDTO company;
    private FeatureWSDTO feature;
    private FeeWSDTO fee;
    private Calendar dateTimeStamp;
    private String serverId;
    private String serverPassword;
    private Boolean forceRandomDollar;
    private OfferWSDTO offer;

    public PSIMessageWSDTO() {
        this.action = null;
        this.respStatus = null;
        this.company = null;
        this.feature = null;
        this.fee = null;
        this.dateTimeStamp = Calendar.getInstance();
        this.serverId = "IDTest";
        this.serverPassword = "PasswordTest";
        this.forceRandomDollar = false;
        this.offer = null;
    }

    public PSIMessageWSDTO clone() throws CloneNotSupportedException {
        PSIMessageWSDTO clone = (PSIMessageWSDTO) super.clone();
        
        if (respStatus != null) {
            clone.setRespStatus(respStatus.clone());
        }
        if (feature != null) {
            clone.setFeature(feature.clone());
        }
        if (company != null) {
            clone.setCompany(company.clone());
        }
        if (fee != null) {
            clone.setFee(fee.clone());
        }
        if (offer != null) {
            clone.setOffer(offer.clone());
        }

        return clone;
    }

    @XmlElement(name = "Action", nillable = false, required = false)
    public ActionEnum getAction() {
        return action;
    }

    public void setAction(ActionEnum action) {
        this.action = action;
    }

    @XmlElement(name = "RespStatus", nillable = false, required = false)
    public ResponseWSDTO getRespStatus() {
        return respStatus;
    }

    public void setRespStatus(ResponseWSDTO respStatus) {
        this.respStatus = respStatus;
    }

    @XmlElement(name = "Company", nillable = false, required = false)
    public CompanyWSDTO getCompany() {
        return company;
    }

    public void setCompany(CompanyWSDTO company) {
        this.company = company;
    }

    @XmlElement(name = "Feature", nillable = false, required = false)
    public FeatureWSDTO getFeature() {
        return feature;
    }

    public void setFeature(FeatureWSDTO feature) {
        this.feature = feature;
    }

    @XmlElement(name = "Fee", nillable = false, required = false)
    public FeeWSDTO getFee() {
        return fee;
    }

    public void setFee(FeeWSDTO fee) {
        this.fee = fee;
    }

    @XmlElement(name = "DateTimeStamp", nillable = false, required = true)
    public Calendar getDateTimeStamp() {
        return dateTimeStamp;
    }

    public void setDateTimeStamp(Calendar dateTimeStamp) {
        this.dateTimeStamp = dateTimeStamp;
    }

    @XmlElement(name = "ServerId", nillable = false, required = false)
    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @XmlElement(name = "ServerPassword", nillable = false, required = false)
    public String getServerPassword() {
        return serverPassword;
    }

    public void setServerPassword(String serverPassword) {
        this.serverPassword = serverPassword;
    }

    @XmlElement(name = "ForceRandomDollar", nillable = false, required = false)
    public Boolean getForceRandomDollar() {
        return this.forceRandomDollar;
    }

    public void setForceRandomDollar(Boolean forceRandomDollar) {
        this.forceRandomDollar = forceRandomDollar;
    }

    @XmlElement(name = "Offer", nillable = false, required = false)
    public OfferWSDTO getOffer() {
        return offer;
    }

    public void setOffer(OfferWSDTO offer) {
        this.offer = offer;
    }

    public void validateAction(ActionEnum pActionEnum) throws Exception {
        if (this.action == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Action"));
        }
        if (!pActionEnum.equals(action)) {
            throw new EwsException(EwsMessages.invalidActionType());
        }
    }

    public void validateRespStatus() throws Exception {
        if (this.respStatus == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("RespStatus"));
        }
    }

    public void validateCompany() throws Exception {
        if (this.company == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Company"));
        }
    }

    public void validateFeature() throws Exception {
        if (this.feature == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Feature"));
        }
    }

    public void validateFee() throws Exception {
        if (this.fee == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Fee"));
        }
    }

    public void validateOffer() throws Exception {
        if (this.offer == null) {
            throw new EwsException(EwsMessages.objectCanNotBeNull("Offer"));
        }
    }

    public void validateDateTimeStamp() throws Exception {
        if (this.dateTimeStamp == null) {
            throw new EwsException(EwsMessages.fieldCanNotBeNullOrEmpty("DateTimeStamp", "PSIMessage"));
        }
    }

    public void validateServerId() throws Exception {
        if (!Validation.validateValue(this.serverId, false, "\\p{Graph}{1,50}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("ServerId", "PSIMessage"));
        }
    }

    public void validateServerPassword() throws Exception {
        if (!Validation.validateValue(this.serverPassword, false, "\\p{Graph}{1,50}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("ServerPassword", "PSIMessage"));
        }
    }

    public void validateForceRandomDollar() throws Exception {
        if (this.forceRandomDollar == null) {
            this.forceRandomDollar = false;
        }
    }
}
