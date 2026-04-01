package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPServiceStatusDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPServiceStatus", propOrder = {"serviceStatusCd","serviceStatusName","serviceStatusDescription","serviceSubStatusList"})
public class SAPServiceStatusDISDTO {
    // The SAP service status is not the same as the PSP service status.  The SAP service status is just a string.

    @XmlElement(name = "ServiceStatusCd")
    private String serviceStatusCd;

    public String getServiceStatusCd() {
        return serviceStatusCd;
    }

    public void setServiceStatusCd(String serviceStatusCd) {
        this.serviceStatusCd = serviceStatusCd;
    }

    @XmlElement(name = "ServiceStatusName")
    private String serviceStatusName;

    public String getServiceStatusName() {
        return serviceStatusName;
    }

    public void setServiceStatusName(String serviceStatusName) {
        this.serviceStatusName = serviceStatusName;
    }

    @XmlElement(name = "ServiceStatusDescription")
    private String serviceStatusDescription;

    public String getServiceStatusDescription() {
        return serviceStatusDescription;
    }

    public void setServiceStatusDescription(String serviceStatusDescription) {
        this.serviceStatusDescription = serviceStatusDescription;
    }

    @XmlElement(name = "ServiceSubStatusList")
    private ArrayList<SAPServiceSubStatusDISDTO> serviceSubStatusList;

    public ArrayList<SAPServiceSubStatusDISDTO> getServiceSubStatusList() {
        return serviceSubStatusList;
    }

    public void setServiceSubStatusList(ArrayList<SAPServiceSubStatusDISDTO> serviceSubStatusList) {
        this.serviceSubStatusList = serviceSubStatusList;
    }
}
