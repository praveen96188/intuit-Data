package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/objects/SAPDisplayStatusDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SAPDisplayStatus", propOrder = {"displayStatus","displaySubStatus","displayDetails"})
public class SAPDisplayStatusDISDTO {
    @XmlElement(name = "DisplayStatus")
    private String displayStatus;

    public String getDisplayStatus() {
        return displayStatus;
    }

    public void setDisplayStatus(String displayStatus) {
        this.displayStatus = displayStatus;
    }

    @XmlElement(name = "DisplaySubStatus")
    private String displaySubStatus;

    public String getDisplayDetails() {
        return displayDetails;
    }

    public void setDisplayDetails(String displayDetails) {
        this.displayDetails = displayDetails;
    }

    @XmlElement(name = "DisplayDetails")
    private String displayDetails;

    public String getDisplaySubStatus() {
        return displaySubStatus;
    }

    public void setDisplaySubStatus(String displaySubStatus) {
        this.displaySubStatus = displaySubStatus;
    }
}
