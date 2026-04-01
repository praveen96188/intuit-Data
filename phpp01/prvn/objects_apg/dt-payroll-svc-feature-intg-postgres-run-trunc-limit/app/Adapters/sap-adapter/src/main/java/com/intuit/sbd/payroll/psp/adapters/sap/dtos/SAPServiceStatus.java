/*
 * $Id: //psp/dev/Adapters/SAP/src/com/intuit/sbd/payroll/psp/adapters/sap/dtos/SAPServiceStatus.java#1 $
 *
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */
package com.intuit.sbd.payroll.psp.adapters.sap.dtos;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

/**
 * SAPServiceStatus -- DTO to represent company service status for SAP adapter.
 *
 * @author Wiktor Kozlik
 */
@XmlRootElement
public class SAPServiceStatus {

    private String serviceStatusCd;
    private String serviceStatusName;
    private String serviceStatusDescription;
    private ArrayList<SAPServiceSubStatus> serviceSubStatusList;

    public String getServiceStatusCd() {
        return serviceStatusCd;
    }

    public void setServiceStatusCd(String serviceStatusCd) {
        this.serviceStatusCd = serviceStatusCd;
    }

    public String getServiceStatusName() {
        return serviceStatusName;
    }

    public void setServiceStatusName(String serviceStatusName) {
        this.serviceStatusName = serviceStatusName;
    }

    public String getServiceStatusDescription() {
        return serviceStatusDescription;
    }

    public void setServiceStatusDescription(String serviceStatusDescription) {
        this.serviceStatusDescription = serviceStatusDescription;
    }

    public ArrayList<SAPServiceSubStatus> getServiceSubStatusList() {
        return serviceSubStatusList;
    }

    public void setServiceSubStatusList(ArrayList<SAPServiceSubStatus> serviceSubStatusList) {
        this.serviceSubStatusList = serviceSubStatusList;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SAPServiceStatus that = (SAPServiceStatus) o;

        if (serviceStatusCd != null ? !serviceStatusCd.equals(that.serviceStatusCd) : that.serviceStatusCd != null)
            return false;

        if (serviceSubStatusList == null && that.serviceSubStatusList != null)
            return false;

        // override default use of ArrayList.equals(..) to remove importance of ordering
        for (SAPServiceSubStatus subStatus : serviceSubStatusList) {
            int i = that.serviceSubStatusList.indexOf(subStatus);
            if (i == -1)
                return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return serviceStatusCd != null ? serviceStatusCd.hashCode() : 0;
    }


}
