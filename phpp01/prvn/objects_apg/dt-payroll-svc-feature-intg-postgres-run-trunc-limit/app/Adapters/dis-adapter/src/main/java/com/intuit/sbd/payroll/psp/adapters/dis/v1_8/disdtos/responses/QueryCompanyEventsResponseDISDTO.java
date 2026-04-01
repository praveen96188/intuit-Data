package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.CompanyEventDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
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
