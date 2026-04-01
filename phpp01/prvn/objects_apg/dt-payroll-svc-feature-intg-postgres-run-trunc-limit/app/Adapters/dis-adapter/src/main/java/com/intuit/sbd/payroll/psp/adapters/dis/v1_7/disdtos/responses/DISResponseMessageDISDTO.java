package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.DISMessage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/DISResponseMessageDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
