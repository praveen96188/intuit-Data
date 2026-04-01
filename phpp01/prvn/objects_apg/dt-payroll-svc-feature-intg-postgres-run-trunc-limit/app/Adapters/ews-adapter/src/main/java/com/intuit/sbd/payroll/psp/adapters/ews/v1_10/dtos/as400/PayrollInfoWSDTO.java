/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */

@XmlType(name = "PayrollInfoType")
public class PayrollInfoWSDTO implements Cloneable {

    private PayrollStatusWSDTO payrollStatusWSDTO;

    public PayrollInfoWSDTO clone() throws CloneNotSupportedException {
        PayrollInfoWSDTO clone = (PayrollInfoWSDTO) super.clone();

        if (payrollStatusWSDTO != null) {
            clone.setPayrollStatusWSDTO(payrollStatusWSDTO.clone());
        }

        return clone;
    }

    public PayrollInfoWSDTO() {
        this.payrollStatusWSDTO = null;
    }

    @XmlElement(name = "PayrollStatus", required = true, nillable = false)
    public PayrollStatusWSDTO getPayrollStatusWSDTO() {
        return payrollStatusWSDTO;
    }

    public void setPayrollStatusWSDTO(PayrollStatusWSDTO payrollStatusWSDTO) {
        this.payrollStatusWSDTO = payrollStatusWSDTO;
    }
}
