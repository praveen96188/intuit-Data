package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects.AgencyRulesAgencyDISDTO;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/GetAgencyRulesResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
