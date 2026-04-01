/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPCompanyEvent.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.*;

import java.util.Date;
import java.util.ArrayList;

/**
 * SAPCompanyEvent - SAP DTO super class for all company events
 *
 * @author Joe Warmelink
 */
public class SAPCompanyEvent {
    private Date eventDate;
    private EventTypeCode eventTypeCd;
    private String eventTypeName;
    private String eventTypeDescription;
    private CompanyEventStatus statusCd;
    private Date statusEffectiveDate;
    private ArrayList<SAPCompanyEventDetail> companyEventDetails = new ArrayList<SAPCompanyEventDetail>();
    private ArrayList<SAPCompanyEventEmail> companyEventEmails = new ArrayList<SAPCompanyEventEmail>();
    private ArrayList<SAPEventAs400Sync> companyEventAs400Syncs = new ArrayList<SAPEventAs400Sync>();
    private Date lastNoteDate;
    private String creatorId;
    private EventGroup eventGroupCode;
    private String id;
    private String overrideMessage;
    private String transmissionId;

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    public EventTypeCode getEventTypeCd() {
        return eventTypeCd;
    }

    public void setEventTypeCd(EventTypeCode eventTypeCd) {
        this.eventTypeCd = eventTypeCd;
    }

    public String getEventTypeName() {
        return eventTypeName;
    }

    public void setEventTypeName(String eventTypeName) {
        this.eventTypeName = eventTypeName;
    }

    public String getEventTypeDescription() {
        return eventTypeDescription;
    }

    public void setEventTypeDescription(String eventTypeDescription) {
        this.eventTypeDescription = eventTypeDescription;
    }

    public CompanyEventStatus getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(CompanyEventStatus statusCd) {
        this.statusCd = statusCd;
    }

    public Date getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(Date statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }

    public ArrayList<SAPCompanyEventEmail> getCompanyEventEmails() {
        return companyEventEmails;
    }

    public void setCompanyEventEmails(ArrayList<SAPCompanyEventEmail> companyEventEmails) {
        this.companyEventEmails = companyEventEmails;
    }

    public ArrayList<SAPCompanyEventDetail> getCompanyEventDetails() {
        return companyEventDetails;
    }

    public void setCompanyEventDetails(ArrayList<SAPCompanyEventDetail> companyEventDetails) {
        this.companyEventDetails = companyEventDetails;
    }

    public SAPCompanyEventDetail getEventDetail(EventDetailTypeCode eventDetailTypeCd) {
        for (SAPCompanyEventDetail eventDetail: this.getCompanyEventDetails()) {
            if (eventDetail.getEventDetailTypeCd() == eventDetailTypeCd) {
                return eventDetail;
            }
        }
        return null;
    }

    public Date getLastNoteDate() {
        return lastNoteDate;
    }

    public void setLastNoteDate(Date lastNoteDate) {
        this.lastNoteDate = lastNoteDate;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public EventGroup getEventGroupCode() {
        return eventGroupCode;
    }

    public void setEventGroupCode(EventGroup eventGroupCode) {
        this.eventGroupCode = eventGroupCode;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOverrideMessage() {
        return overrideMessage;
    }

    public void setOverrideMessage(String pOverrideMessage) {
        overrideMessage = pOverrideMessage;
    }

    public String getTransmissionId() {
        return transmissionId;
    }

    public void setTransmissionId(String pTransmissionId) {
        transmissionId = pTransmissionId;
    }


    public ArrayList<SAPEventAs400Sync> getCompanyEventAs400Syncs() {
        return companyEventAs400Syncs;
    }

    public void setCompanyEventAs400Syncs(ArrayList<SAPEventAs400Sync> companyEventAs400Syncs) {
        this.companyEventAs400Syncs = companyEventAs400Syncs;
    }
}
