package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.DISMessage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Base response wrapper DIS DTO that will be returned by the WS
 *
 */
@XmlRootElement()
@XmlType(name = "DISResponseMessageDISDTO",propOrder = {"code", "message"})
public class DISResponseMessageDISDTO {

    private DISMessage disMessage;

    public DISResponseMessageDISDTO() {
    }

    public void setDISMessage(DISMessage pDISMessage) {
        this.disMessage=pDISMessage;
    }

    @XmlElement(name = "Code", nillable = false, required = true)
    public String getCode() {
        return "DIS-" + disMessage.getCode();
    }

    @XmlElement(name = "Message", nillable = false, required = true)
    public String getMessage() {
        return disMessage.getMessage();
    }

}
