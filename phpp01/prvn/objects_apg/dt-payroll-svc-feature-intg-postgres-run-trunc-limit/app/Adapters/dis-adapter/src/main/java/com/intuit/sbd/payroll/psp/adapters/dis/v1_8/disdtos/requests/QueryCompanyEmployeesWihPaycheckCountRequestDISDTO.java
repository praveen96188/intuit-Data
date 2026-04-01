package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.requests;

import com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.enums.SourceSystemEnum;

import javax.xml.bind.annotation.*;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "QueryCompanyEmployeesWihPaycheckCountRequestDISDTO")
@XmlType()
public class QueryCompanyEmployeesWihPaycheckCountRequestDISDTO {
    @XmlElement(name = "SourceSystem", nillable = false, required = true)
    private SourceSystemEnum sourceSystem;

    public SourceSystemEnum getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(SourceSystemEnum sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    @XmlElement(name = "SourceCompanyId", nillable = false, required = true)
    private String sourceCompanyId;

    public String getSourceCompanyId() {
        return sourceCompanyId;
    }

    public void setSourceCompanyId(String sourceCompanyId) {
        this.sourceCompanyId = sourceCompanyId;
    }

    @XmlElement(name = "Year", nillable = false, required = true)
    private int year;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
