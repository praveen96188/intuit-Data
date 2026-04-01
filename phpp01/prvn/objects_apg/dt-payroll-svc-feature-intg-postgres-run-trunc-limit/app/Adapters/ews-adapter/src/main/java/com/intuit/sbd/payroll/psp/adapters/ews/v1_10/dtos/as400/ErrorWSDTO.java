/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */

@XmlType(name = "ErrorType", propOrder = {"code", "description"})
public class ErrorWSDTO implements Cloneable {

    private String code;
    private String description;

    public ErrorWSDTO clone() throws CloneNotSupportedException {
        return (ErrorWSDTO) super.clone();
    }

    public ErrorWSDTO() {
        this.code = null;
        this.description = null;
    }

    @XmlAttribute(name = "Code")
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @XmlAttribute(name = "Description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
