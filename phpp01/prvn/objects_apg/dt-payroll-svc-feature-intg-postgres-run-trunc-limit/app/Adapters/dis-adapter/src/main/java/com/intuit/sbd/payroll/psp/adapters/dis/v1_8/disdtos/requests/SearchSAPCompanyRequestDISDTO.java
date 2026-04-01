package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Received WS DTO for the query company request
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "QueryCompanyFullRequest")
@XmlType()
public class SearchSAPCompanyRequestDISDTO {

    @XmlElement(name = "EIN", nillable = false, required = false)
    private String ein;

    public String getEin() {
        return ein;
    }

    public void setEin(String pEIN) {
        this.ein = pEIN;
    }

    @XmlElement(name = "SourceSystem", nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    public SourceSystemEnum getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystemEnum sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    @XmlElement(name = "SourceCompanyId", nillable = false, required = false)
    private String sourceCompanyId;

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    @XmlElement(name = "RealmId", nillable = false, required = false)
    private String realmId;

    public String getRealmId() {
        return realmId;
    }

    public void setRealmId(String realmId) {
        this.realmId = realmId;
    }
}
