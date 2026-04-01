package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.objects;

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
@XmlType(name = "AgencyRulesJurisdictionDISDTO")
public class AgencyRulesJurisdictionDISDTO {
    @XmlElement(name = "JurisdictionId")
    private String jurisdictionId;

    @XmlElement(name = "UIString")
    private String uiString;

    @XmlElement(name = "StateId")
    private String stateId;

    @XmlElement(name = "Agency")
    private List<AgencyRulesAgencyDISDTO> agencies;

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

    public String getUiString() {
        return uiString;
    }

    public void setUiString(String uiString) {
        this.uiString = uiString;
    }

    public String getStateId() {
        return stateId;
    }

    public void setStateId(String stateId) {
        this.stateId = stateId;
    }

    public List<AgencyRulesAgencyDISDTO> getAgencies() {
        return agencies;
    }

    public void setAgencies(List<AgencyRulesAgencyDISDTO> agencies) {
        this.agencies = agencies;
    }
}
