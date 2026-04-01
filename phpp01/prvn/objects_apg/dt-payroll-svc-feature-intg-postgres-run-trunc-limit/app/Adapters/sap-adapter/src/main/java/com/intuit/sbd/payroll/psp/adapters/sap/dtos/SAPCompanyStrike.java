/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPCompanyStrike.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.StrikeReason;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * SAPCompany -- DTO to represent a company for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
public class SAPCompanyStrike extends SAPCompanyEvent {
    private StrikeReason strikeReason;
    private String manualDescription;
    private Date strikeDate;
    private String createdByUserId;
    private String spcfUniqueId;
    private boolean newStrike = false;
    private boolean cancelled = false;
    private Date statusEffectiveDate;
    private String cancelledByUserId;
    private String financialTransactionId;

    public String getFinancialTransactionId() {
        return financialTransactionId;
    }

    public void setFinancialTransactionId(String financialTransactionId) {
        this.financialTransactionId = financialTransactionId;
    }

    public StrikeReason getStrikeReason() {
        return strikeReason;
    }

    public void setStrikeReason(StrikeReason strikeReason) {
        this.strikeReason = strikeReason;
    }

    public Date getStrikeDate() {
        return strikeDate;
    }

    public void setStrikeDate(Date strikeDate) {
        this.strikeDate = strikeDate;
    }

    public boolean isNewStrike() {
        return newStrike;
    }

    public void setNewStrike(boolean newStrike) {
        this.newStrike = newStrike;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getSpcfUniqueId() {
        return spcfUniqueId;
    }

    public void setSpcfUniqueId(String spcfUniqueId) {
        this.spcfUniqueId = spcfUniqueId;
    }

    public String getManualDescription() {
        return manualDescription;
    }

    public void setManualDescription(String manualDescription) {
        this.manualDescription = manualDescription;
    }

    public Date getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(Date statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }

    public String getCancelledByUserId() {
        return cancelledByUserId;
    }

    public void setCancelledByUserId(String cancelledByUserId) {
        this.cancelledByUserId = cancelledByUserId;
    }

    public String getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(String createdByUserId) {
        this.createdByUserId = createdByUserId;
    }
}
