package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.CompanyEventDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryCompanyEventsResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
 *
 * Response WS DTO for the query company events request
 *
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryCompanyEventsResponseDISDTO",propOrder = {"companyEvents"})
public class QueryCompanyEventsResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "CompanyEvent")
    private List<CompanyEventDISDTO> companyEvents;

    public List<CompanyEventDISDTO> getCompanyEvents() {
        return companyEvents;
    }

    public void setCompanyEvents(List<CompanyEventDISDTO> companyEvents) {
        this.companyEvents = companyEvents;
    }

    public void clearElements() {
        this.companyEvents = null;
    }


}
