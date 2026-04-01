/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPCompanyNote.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * SAPCompanyNote -- DTO to represent company notes for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
public class SAPCompanyNote {
    private String id;
    private String notes;
    private String insertUserId;
    private Date createdDate;
    private boolean alert;
    private String eventId;

    public String getId() {
        return id;
    }

    public void setId(String pId) {
        id = pId;
    }


    public String getEventId() {
        return eventId;
    }

    public void setEventId(String pEventId) {
        eventId = pEventId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInsertUserId() {
        return insertUserId;
    }

    public void setInsertUserId(String insertUserId) {
        this.insertUserId = insertUserId;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public boolean getAlert() {
        return alert;
    }

    public void setAlert(boolean pAlert) {
        alert = pAlert;
    }
}
