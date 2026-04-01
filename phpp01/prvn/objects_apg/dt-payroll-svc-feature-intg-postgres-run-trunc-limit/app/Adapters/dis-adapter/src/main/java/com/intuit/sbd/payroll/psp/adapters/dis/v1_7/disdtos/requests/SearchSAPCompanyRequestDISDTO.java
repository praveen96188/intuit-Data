package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/requests/SearchSAPCompanyRequestDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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

}
