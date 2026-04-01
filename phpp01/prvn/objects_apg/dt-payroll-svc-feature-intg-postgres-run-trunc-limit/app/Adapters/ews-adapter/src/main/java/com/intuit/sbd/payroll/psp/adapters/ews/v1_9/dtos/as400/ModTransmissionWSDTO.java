/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import javax.xml.bind.annotation.*;
import java.util.*;

/**
    @author Jeff Jones
 */

@XmlType(name = "ModTransmissionType", propOrder = {"date", "time", "offloadDate", "checkDate", "totalTaxes", "totalDD",
        "totalFees", "total", "fees"})
public class ModTransmissionWSDTO {

    private String date;                // mm/dd/yyyy
    private String time;                // hh:mm am/pm
    private String offloadDate;         // mm/dd/yyyy
    private String checkDate;           // mm/dd/yyyy
    private Double totalTaxes;          // 0.00 thru 999999999.99
    private Double totalDD;             // 0.00 thru 999999999.99
    private Double totalFees;           // 0.00 thru 999999999.99
    private Double total;               // 0.00 thru 999999999.99
    private List<TransmissionFeeWSDTO> fees;

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

    @XmlAttribute(name = "OffloadDate", required = true)
    public String getOffloadDate() {
        return offloadDate;
    }

    public void setOffloadDate(String offloadDate) {
        this.offloadDate = offloadDate;
    }

    @XmlAttribute(name = "CheckDate", required = true)
    public String getCheckDate() {
        return checkDate;
    }

    public void setCheckDate(String checkDate) {
        this.checkDate = checkDate;
    }

    @XmlAttribute(name = "TotalTaxes", required = true)
    public Double getTotalTaxes() {
        return totalTaxes;
    }

    public void setTotalTaxes(Double totalTaxes) {
        this.totalTaxes = totalTaxes;
    }

    @XmlAttribute(name = "TotalDD", required = true)
    public Double getTotalDD() {
        return totalDD;
    }

    public void setTotalDD(Double totalDD) {
        this.totalDD = totalDD;
    }

    @XmlAttribute(name = "TotalFees", required = true)
    public Double getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(Double totalFees) {
        this.totalFees = totalFees;
    }

    @XmlAttribute(name = "Total", required = true)
    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    @XmlElement(name = "Fee", required = true, nillable = false)
    public List<TransmissionFeeWSDTO> getFees() {
        return fees;
    }

    public void setFees(List<TransmissionFeeWSDTO> fees) {
        this.fees = fees;
    }

}
