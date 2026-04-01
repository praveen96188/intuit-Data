package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import com.intuit.sbd.payroll.psp.domain.ServiceSubStatusCode;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPServiceSubStatusDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPServiceSubStatus", propOrder = {"subStatusName","subStatusDescription","subStatusCd","subStatusType","isManuallyUpdatable"})
public class SAPServiceSubStatusDISDTO {
    @XmlElement(name = "SubStatusName")
    private String subStatusName;

    public String getSubStatusName() {
        return subStatusName;
    }

    public void setSubStatusName(String subStatusName) {
        this.subStatusName = subStatusName;
    }

    @XmlElement(name = "SubStatusDescription")
    private String subStatusDescription;

    public String getSubStatusDescription() {
        return subStatusDescription;
    }

    public void setSubStatusDescription(String subStatusDescription) {
        this.subStatusDescription = subStatusDescription;
    }

    @XmlElement(name = "SubStatusCd")
    private ServiceSubStatusCode subStatusCd;

    public ServiceSubStatusCode getSubStatusCd() {
        return subStatusCd;
    }

    public void setSubStatusCd(ServiceSubStatusCode subStatusCd) {
        this.subStatusCd = subStatusCd;
    }

    @XmlElement(name = "SubStatusType")
    private String subStatusType;

    public String getSubStatusType() {
        return subStatusType;
    }

    public void setSubStatusType(String subStatusType) {
        this.subStatusType = subStatusType;
    }

    @XmlElement(name = "IsManuallyUpdatable")
    private boolean isManuallyUpdatable;

    public boolean isManuallyUpdatable() {
        return isManuallyUpdatable;
    }

    public void setManuallyUpdatable(boolean manuallyUpdatable) {
        isManuallyUpdatable = manuallyUpdatable;
    }
}
