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
import java.math.*;

/**
    @author Jeff Jones
 */

@XmlType(name = "TransmissionType", propOrder = {"date", "time", "offloadDate", "checkDate", "payrollType", "totalTaxes", "totalDD",
        "totalFees", "total", "fees"})
public class TransmissionWSDTO implements Cloneable {

    private String date;                // mm/dd/yyyy
    private String time;                // hh:mm am/pm
    private String offloadDate;         // mm/dd/yyyy
    private String checkDate;           // mm/dd/yyyy
    private String payrollType;
    private BigDecimal totalTaxes;      // 0.00 thru 999999999.99
    private BigDecimal totalDD;         // 0.00 thru 999999999.99
    private BigDecimal totalFees;       // 0.00 thru 999999999.99
    private BigDecimal total;           // 0.00 thru 999999999.99
    private List<TransmissionFeeWSDTO> fees;

    public TransmissionWSDTO clone() throws CloneNotSupportedException {
        TransmissionWSDTO clone = (TransmissionWSDTO) super.clone();

        if (fees != null) {
            clone.fees = new ArrayList<TransmissionFeeWSDTO>();
            for (TransmissionFeeWSDTO fee: fees) {
                clone.fees.add(fee.clone());
            }
        }

        return clone;
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

    @XmlAttribute(name = "PayrollType", required = true)
    public String getPayrollType() {
        return payrollType;
    }

    public void setPayrollType(String payrollType) {
        this.payrollType = payrollType;
    }

    @XmlAttribute(name = "TotalTaxes", required = true)
    public BigDecimal getTotalTaxes() {
        return totalTaxes;
    }

    public void setTotalTaxes(BigDecimal totalTaxes) {
        this.totalTaxes = totalTaxes.setScale(2);
    }

    @XmlAttribute(name = "TotalDD", required = true)
    public BigDecimal getTotalDD() {
        return totalDD;
    }

    public void setTotalDD(BigDecimal totalDD) {
        this.totalDD = totalDD.setScale(2);
    }

    @XmlAttribute(name = "TotalFees", required = true)
    public BigDecimal getTotalFees() {
        return totalFees;
    }

    public void setTotalFees(BigDecimal totalFees) {
        this.totalFees = totalFees.setScale(2);
    }

    @XmlAttribute(name = "Total", required = true)
    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total.setScale(2);
    }

    @XmlElement(name = "Fee", required = true, nillable = false)
    public List<TransmissionFeeWSDTO> getFees() {
        return fees;
    }

    public void setFees(List<TransmissionFeeWSDTO> fees) {
        this.fees = fees;
    }
}
