package com.intuit.sbd.payroll.psp.adapters.dis.v1_8.disdtos.responses;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
 * $Author$
 * $File$
 * $Revision$
 * $DateTime$
 * $Author$
 */
@XmlRootElement()
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QueryCompanyLatestPayrollDatesResponseDISDTO",propOrder = {"latestPayrollRunDate","latestPayrollCheckDate","firstPayrollRunDate","firstPayrollCheckDate"})
public class QueryCompanyLatestPayrollDatesResponseDISDTO extends ResponseDISDTO {

    @XmlElement(name = "LatestPayrollRunDate")
    private Calendar latestPayrollRunDate;

    @XmlElement(name = "LatestPayrollCheckDate")
    private Calendar latestPayrollCheckDate;

    @XmlElement(name = "FirstPayrollRunDate")
    private Calendar firstPayrollRunDate;

    @XmlElement(name = "FirstPayrollCheckDate")
    private Calendar firstPayrollCheckDate;

    public Calendar getLatestPayrollRunDate() {
        return latestPayrollRunDate;
    }

    public void setLatestPayrollRunDate(Calendar latestPayrollRunDate) {
        this.latestPayrollRunDate = latestPayrollRunDate;
    }

    public Calendar getLatestPayrollCheckDate() {
        return latestPayrollCheckDate;
    }

    public void setLatestPayrollCheckDate(Calendar latestPayrollCheckDate) {
        this.latestPayrollCheckDate = latestPayrollCheckDate;
    }

    public Calendar getFirstPayrollRunDate() {
        return firstPayrollRunDate;
    }

    public void setFirstPayrollRunDate(Calendar pFirstPayrollRunDate) {
        firstPayrollRunDate = pFirstPayrollRunDate;
    }

    public Calendar getFirstPayrollCheckDate() {
        return firstPayrollCheckDate;
    }

    public void setFirstPayrollCheckDate(Calendar pFirstPayrollCheckDate) {
        firstPayrollCheckDate = pFirstPayrollCheckDate;
    }

    @Override
    public void clearElements() {
        //@TODO Implement
    }

}