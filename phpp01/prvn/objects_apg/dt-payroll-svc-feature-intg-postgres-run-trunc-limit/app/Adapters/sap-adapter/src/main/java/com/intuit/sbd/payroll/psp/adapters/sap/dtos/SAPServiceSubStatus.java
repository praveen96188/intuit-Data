/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPServiceSubStatus.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * SAPFundingModel -- DTO to represent company funding model for SAP adapter.
 *
 * @author Joe Warmelink
 */
@XmlRootElement
public class SAPServiceSubStatus {
    private String subStatusName;
    private String subStatusDescription;
    private ServiceSubStatusCode subStatusCd;
    private String subStatusType;
    private boolean isManuallyUpdatable;

    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

    public String getSubStatusDescription() {
        return subStatusDescription;
    }

    public void setSubStatusDescription(String subStatusDescription) {
        this.subStatusDescription = subStatusDescription;
    }

    public ServiceSubStatusCode getSubStatusCd() {
        return subStatusCd;
    }

    public void setSubStatusCd(ServiceSubStatusCode subStatusCd) {
        this.subStatusCd = subStatusCd;
    }

    public String getSubStatusType() {
        return subStatusType;
    }

    public void setSubStatusType(String subStatusType) {
        this.subStatusType = subStatusType;
    }

    public boolean isManuallyUpdatable() {
        return isManuallyUpdatable;
    }

    public void setManuallyUpdatable(boolean manuallyUpdatable) {
        isManuallyUpdatable = manuallyUpdatable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SAPServiceSubStatus that = (SAPServiceSubStatus) o;

        if (subStatusCd != that.subStatusCd) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return subStatusCd.hashCode();
    }
}
