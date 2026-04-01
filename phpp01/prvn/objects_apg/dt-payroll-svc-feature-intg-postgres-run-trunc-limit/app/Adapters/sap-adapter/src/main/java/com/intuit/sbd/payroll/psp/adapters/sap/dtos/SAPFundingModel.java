/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPFundingModel.java#1 $
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
 * SAPFundingModel -- DTO to represent company funding model for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
public class SAPFundingModel {
    private String name;
    private String description;
    private String fundingModelCd;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFundingModelCd() {
        return fundingModelCd;
    }

    public void setFundingModelCd(String fundingModelCd) {
        this.fundingModelCd = fundingModelCd;
    }
}
