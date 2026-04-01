package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

import com.intuit.sbd.payroll.psp.adapters.sap.dtos.SAPAgencyIdRequirement;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType
public class AgencyIdRequirementDISDTO {

    public AgencyIdRequirementDISDTO() {
    }

    public AgencyIdRequirementDISDTO(SAPAgencyIdRequirement sapAgencyIdRequirement) {
        this.setFulfilled(sapAgencyIdRequirement.getIsFulfilled());
        this.setRequirementString(sapAgencyIdRequirement.getRequirementString());
    }

    @XmlElement
    private boolean isFulfilled;

    public boolean isFulfilled() {
        return isFulfilled;
    }

    public void setFulfilled(boolean pFulfilled) {
        isFulfilled = pFulfilled;
    }

    @XmlElement
    private String requirementString;

    public String getRequirementString() {
        return requirementString;
    }

    public void setRequirementString(String pRequirementString) {
        requirementString = pRequirementString;
    }

}
