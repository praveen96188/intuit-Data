/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.EwsMessages;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.exceptions.EwsException;
import com.intuit.sbd.payroll.psp.adapters.ews.v1_9.utils.Validation;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;

/**
    @author Jeff Jones
 */
@XmlType(name = "FeeType", propOrder = "monthlyFee")
public class FeeWSDTO implements Cloneable {
    private String monthlyFee;

    public FeeWSDTO() {
        this.monthlyFee = null;
    }

    public FeeWSDTO clone() throws CloneNotSupportedException {
        return (FeeWSDTO) super.clone();
    }

    @XmlElement(name = "MonthlyFee", nillable = false, required = false)
    public String getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(String monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public void validateMonthlyFee() throws Exception {
        if (!Validation.validateValue(this.monthlyFee, true, "\\p{Graph}{0,100}")) {
            throw new EwsException(EwsMessages.fieldDataNotValid("MonthlyFee", "Fee"));
        }
    }

}
