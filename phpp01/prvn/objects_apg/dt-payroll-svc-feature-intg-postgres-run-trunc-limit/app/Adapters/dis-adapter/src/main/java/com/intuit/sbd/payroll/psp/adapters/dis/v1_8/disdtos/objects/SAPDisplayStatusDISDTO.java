package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
