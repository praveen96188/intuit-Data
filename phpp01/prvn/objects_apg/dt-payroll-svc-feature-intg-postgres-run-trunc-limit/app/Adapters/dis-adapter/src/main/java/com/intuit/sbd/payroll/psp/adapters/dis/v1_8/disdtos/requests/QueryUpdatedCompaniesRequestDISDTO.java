package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import com.intuit.sbd.payroll.psp.domain.ServiceCode;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "QueryUpdatedCompaniesRequestDISDTO")
@XmlType()
public class QueryUpdatedCompaniesRequestDISDTO {
    @XmlElement(nillable = false, required = true)
    private Calendar startDate;

    @XmlElement
    private Calendar endDate;

    @XmlElement(nillable = false, required = true)
    private ServiceCode serviceCode;

    public Calendar getStartDate() {
        return startDate;
    }

    public void setStartDate(Calendar startDate) {
        this.startDate = startDate;
    }

    public Calendar getEndDate() {
        return endDate;
    }

    public void setEndDate(Calendar endDate) {
        this.endDate = endDate;
    }

    public ServiceCode getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(ServiceCode pServiceCode) {
        serviceCode = pServiceCode;
    }
}
