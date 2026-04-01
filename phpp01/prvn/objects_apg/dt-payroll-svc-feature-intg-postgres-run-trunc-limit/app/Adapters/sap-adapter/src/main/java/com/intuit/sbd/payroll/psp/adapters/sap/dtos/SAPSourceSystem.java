/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPSourceSystem.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * SAPSourceSystem -- DTO to represent company source system for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
public class SAPSourceSystem {
    private String description;
    private String name;
    private String sourceSystemCd;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSourceSystemCd() {
        return sourceSystemCd;
    }

    public void setSourceSystemCd(String sourceSystemCd) {
        this.sourceSystemCd = sourceSystemCd;
    }
}
