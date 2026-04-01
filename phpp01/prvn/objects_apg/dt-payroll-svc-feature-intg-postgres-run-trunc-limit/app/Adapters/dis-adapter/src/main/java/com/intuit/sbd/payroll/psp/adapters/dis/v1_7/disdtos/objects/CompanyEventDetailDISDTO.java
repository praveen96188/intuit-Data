package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class CompanyEventDetailDISDTO {
    @XmlElement(name = "EventDetailTypeCode")
    private String eventDetailTypeCode;

    @XmlElement(name = "EventDetailValue")
    private String eventDetailValue;

    public String getEventDetailTypeCode() {
        return eventDetailTypeCode;
    }

    public void setEventDetailTypeCode(String pEventDetailTypeCode) {
        eventDetailTypeCode = pEventDetailTypeCode;
    }

    public String getEventDetailValue() {
        return eventDetailValue;
    }

    public void setEventDetailValue(String pEventDetailValue) {
        eventDetailValue = pEventDetailValue;
    }
}
