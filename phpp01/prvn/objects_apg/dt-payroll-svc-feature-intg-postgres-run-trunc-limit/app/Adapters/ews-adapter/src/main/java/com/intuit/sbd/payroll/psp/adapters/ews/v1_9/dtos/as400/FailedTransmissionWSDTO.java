/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import javax.xml.bind.annotation.*;

/**
    @author Jeff Jones
 */

@XmlType(name = "FailedTransmissionType", propOrder = {"date", "time", "description"})
public class FailedTransmissionWSDTO implements Cloneable {

    private String date;                // mm/dd/yyyy
    private String time;                // hh:mm am/pm
    private String description;

    public FailedTransmissionWSDTO clone() throws CloneNotSupportedException {
        return (FailedTransmissionWSDTO) super.clone();
    }

    @XmlAttribute(name = "Date", required = true)
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @XmlAttribute(name = "Time", required = true)
    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @XmlAttribute(name = "Description", required = true)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
