/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPPropertyAudit.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import java.util.Date;

/**
 * SAPPropertyAudit - SAP DTO for property audit information
 *
 * @author Joe Warmelink
 */
public class SAPPropertyAudit {
    private Date createdDate;
    private String userId;
    private String propertyName;
    private Date auditDate;
    private String oldPropertyValue;
    private String newPropertyValue;
    private String category;

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Date getAuditDate() {
        return auditDate;
    }

    public void setAuditDate(Date auditDate) {
        this.auditDate = auditDate;
    }

    public String getOldPropertyValue() {
        return oldPropertyValue;
    }

    public void setOldPropertyValue(String oldPropertyValue) {
        this.oldPropertyValue = oldPropertyValue;
    }

    public String getNewPropertyValue() {
        return newPropertyValue;
    }

    public void setNewPropertyValue(String newPropertyValue) {
        this.newPropertyValue = newPropertyValue;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
