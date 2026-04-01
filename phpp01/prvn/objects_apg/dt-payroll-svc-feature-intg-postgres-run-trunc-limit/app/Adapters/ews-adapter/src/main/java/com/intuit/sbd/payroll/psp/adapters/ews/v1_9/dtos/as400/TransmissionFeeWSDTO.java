/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_9.dtos.as400;

import javax.xml.bind.annotation.*;
import java.math.*;

/**
    @author Jeff Jones
 */

@XmlType(name = "TransmissionFeeType", propOrder = {"name", "amt"})
public class TransmissionFeeWSDTO implements Cloneable {

    private String name;
    private BigDecimal amt;   // 0.00 thru 999999999.99

    public TransmissionFeeWSDTO clone() throws CloneNotSupportedException {
        return (TransmissionFeeWSDTO) super.clone();        
    }

    @XmlAttribute(name = "Name", required = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "Amt", required = true)
    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt.setScale(2);
    }
}
