package com.intuit.sbd.payroll.psp.adapters.dis.v1_7.disdtos.responses;

import javax.xml.bind.annotation.*;
import java.util.Calendar;

/**
 * $Author: JChickanosky $
 * $File: //PSP/dev/Adapters/DIS/src/com/intuit/sbd/payroll/psp/adapters/dis/v1_7/disdtos/responses/QueryCompanyLatestPayrollDatesResponseDISDTO.java $
 * $Revision: #1 $
 * $DateTime: 2012/10/03 10:58:39 $
 * $Author: JChickanosky $
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