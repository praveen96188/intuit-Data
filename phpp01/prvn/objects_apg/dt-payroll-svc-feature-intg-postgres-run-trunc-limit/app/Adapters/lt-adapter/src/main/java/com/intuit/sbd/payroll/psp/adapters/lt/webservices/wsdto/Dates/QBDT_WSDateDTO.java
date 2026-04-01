package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Date;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QBDTWS_Dates", propOrder = {
        "payDate",
        "periodStart",
        "periodEnd"
})
public class QBDT_WSDateDTO implements LtDateDTO{

    @XmlElement(name = "QBDTWS_PayDate")
    public LtQBDate payDate;

    @XmlElement(name = "QBDTWS_PeriodStart")
    public LtQBDate periodStart;

    @XmlElement(name = "QBDTWS_PeriodEnd")
    public LtQBDate periodEnd;

    public QBDT_WSDateDTO() {
        payDate = new LtQBDate();
        periodStart = new LtQBDate();
        periodEnd = new LtQBDate();
    }

    public QBDT_WSDateDTO(Date date) {
        payDate = new LtQBDate();
        periodStart = new LtQBDate();
        periodEnd = new LtQBDate();
        this.setDate(date);
    }

    public LtQBDate getPayDate() {
        return payDate;
    }

    public void setPayDate(LtQBDate payDate) {
        this.payDate = payDate;
    }

    public LtQBDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LtQBDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public LtQBDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LtQBDate periodStart) {
        this.periodStart = periodStart;
    }

    public void setDate(Date date) {

        //Set the PayDate
        SpcfCalendar payCal = CalendarUtils.convertToSpcfCalendar(date);
        payDate.setYear(payCal.getYear());
        payDate.setMonth(payCal.getMonth());
        payDate.setDay(payCal.getDay());

        //Set the PeriodStartDate
        SpcfCalendar ppStartCal = CalendarUtils.convertToSpcfCalendar(DateUtilities.addDaysToDate(date, -30));
        periodStart.setYear(ppStartCal.getYear());
        periodStart.setMonth(ppStartCal.getMonth());
        periodStart.setDay(ppStartCal.getDay());

        //Set the PeriodEndDate
        SpcfCalendar ppEndCal = CalendarUtils.convertToSpcfCalendar(DateUtilities.addDaysToDate(date, -1));
        periodEnd.setYear(ppEndCal.getYear());
        periodEnd.setMonth(ppEndCal.getMonth());
        periodEnd.setDay(ppEndCal.getDay());

    }
}
