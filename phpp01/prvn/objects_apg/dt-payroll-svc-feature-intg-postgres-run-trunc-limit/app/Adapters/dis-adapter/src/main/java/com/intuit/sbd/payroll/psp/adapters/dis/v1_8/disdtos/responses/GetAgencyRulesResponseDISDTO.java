package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects.AgencyRulesAgencyDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetAgencyRulesResponseDISDTO",propOrder = {"agencies"})
public class GetAgencyRulesResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "Agencies")
    private List<AgencyRulesAgencyDISDTO> agencies;

    public List<AgencyRulesAgencyDISDTO> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<AgencyRulesAgencyDISDTO> agencies) {
        this.agencies = agencies;
    }

    @Override
    public void clearElements() {
        agencies = null;
    }
}
