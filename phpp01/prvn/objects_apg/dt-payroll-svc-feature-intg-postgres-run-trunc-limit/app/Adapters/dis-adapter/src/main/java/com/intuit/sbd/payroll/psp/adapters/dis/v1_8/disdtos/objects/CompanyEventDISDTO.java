package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.domain.CompanyEventStatus;

import javax.xml.bind.annotation.*;
import java.util.Date;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Company Events DIS DTO that will be returned by the WS
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CompanyEvent", propOrder = {"statusCd","statusEffectiveDate","eventTimeStamp",
        "creatorId","eventTypeCode","name","eventDetails"})
public class CompanyEventDISDTO {

    @XmlElement(name = "Status")
    private CompanyEventStatus statusCd;

    public CompanyEventStatus getStatusCd() {
        return statusCd;
    }

    public void setStatusCd(CompanyEventStatus statusCd) {
        this.statusCd = statusCd;
    }

    @XmlElement(name = "StatusEffectiveDate")
    private Date statusEffectiveDate;

    public Date getStatusEffectiveDate() {
        return statusEffectiveDate;
    }

    public void setStatusEffectiveDate(Date statusEffectiveDate) {
        this.statusEffectiveDate = statusEffectiveDate;
    }

    @XmlElement(name = "EventTimeStamp")
    private Date eventTimeStamp;

    public Date getEventTimeStamp() {
        return eventTimeStamp;
    }

    public void setEventTimeStamp(Date eventTimeStamp) {
        this.eventTimeStamp = eventTimeStamp;
    }

    @XmlElement(name = "CreatorId")
    private String creatorId;

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @XmlElement(name = "EventTypeCode")
    private String eventTypeCode;

    public String getEventTypeCode() {
        return eventTypeCode;
    }

    public void setEventTypeCode(String eventTypeCode) {
        this.eventTypeCode = eventTypeCode;
    }

    @XmlElement(name = "Name")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    @XmlElement(name = "EventDetails")
    private List<CompanyEventDetailDISDTO> eventDetails;

    public List<CompanyEventDetailDISDTO> getEventDetails() {
        return eventDetails;
    }

    public void setEventDetails(List<CompanyEventDetailDISDTO> pEventDetails) {
        eventDetails = pEventDetails;
    }
}
