package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/requests/QueryCompanyEventsRequestDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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
