package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class AgencyIdPaymentMethodRequirementsDISDTO {

    public AgencyIdPaymentMethodRequirementsDISDTO() {
    }

    @XmlElement
    private String requirementsDescription;

    public String getRequirementsDescription() {
        return requirementsDescription;
    }

    public void setRequirementsDescription(String pRequirementsDescription) {
        requirementsDescription = pRequirementsDescription;
    }

    @XmlElement
    private List<AgencyIdRequirementDISDTO> agencyIdRequirements;

    public List<AgencyIdRequirementDISDTO> getAgencyIdRequirements() {
        return agencyIdRequirements;
    }

    public void setAgencyIdRequirements(List<AgencyIdRequirementDISDTO> pAgencyIdRequirements) {
        agencyIdRequirements = pAgencyIdRequirements;
    }


}
