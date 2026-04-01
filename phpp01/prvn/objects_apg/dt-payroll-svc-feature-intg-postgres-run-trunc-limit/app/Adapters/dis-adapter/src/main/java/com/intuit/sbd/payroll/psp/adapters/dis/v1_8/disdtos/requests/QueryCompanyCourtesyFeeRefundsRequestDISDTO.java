package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 *
 * Received WS DTO for the query company event request
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType()
@XmlRootElement(name = "QueryCompanyEventsRequest")
public class QueryCompanyCourtesyFeeRefundsRequestDISDTO {

    @XmlElement(nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    @XmlElement(nillable = false, required = true)
    private String sourceCompanyId;

    @XmlElement(nillable = false)
    private Calendar fromDate;

    public SourceSystemEnum getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystemEnum sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    public Calendar getFromDate() {
        return fromDate;
    }

    public void setFromDate(Calendar pFromDate) {
        fromDate = pFromDate;
    }
}
