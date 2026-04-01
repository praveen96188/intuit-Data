/**
 * Copyright (c) 2008 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp.adapters.ews.v1_10.dtos.as400;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
    @author Jeff Jones
 */
@XmlType(name = "ResponseType", propOrder = {"errorCode", "errorText"})
public class ResponseWSDTO implements Cloneable {
    private Integer errorCode;
    private String errorText;

    public ResponseWSDTO() {
        this.errorCode = null;
        this.errorText = null;
    }

    public ResponseWSDTO clone() throws CloneNotSupportedException {
        return (ResponseWSDTO) super.clone();
    }

    @XmlElement(name = "ErrorCode")
    public Integer getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(Integer errorCode) {
        this.errorCode = errorCode;
    }

    @XmlElement(name = "ErrorText")
    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

}
