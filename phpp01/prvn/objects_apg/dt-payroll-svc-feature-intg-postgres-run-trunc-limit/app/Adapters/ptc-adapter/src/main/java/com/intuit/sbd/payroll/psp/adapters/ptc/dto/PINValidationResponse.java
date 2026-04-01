package com.intuit.sbd.payroll.psp.adapters.ptc.dto;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * User: dweinberg
 * Date: 8/13/12
 * Time: 3:55 PM
 */
@XmlRootElement()
@XmlType(name = "PINValidationResponse")
public class PINValidationResponse {
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String pStatus) {
        status = pStatus;
    }
}
