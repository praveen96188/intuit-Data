package com.intuit.sbd.payroll.psp.adapters.lt.webservices.wsdto.Dates;

import com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates.DateUtilities;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QBDT_Dates", propOrder = {
        "dtClient",
        "dtPaychecks",
        "dtTransactions",
        "dtPayPeriodBegin",
        "dtPayPeriodEnd"
})

public class QBDTDateDTO implements LtDateDTO{

    @XmlElement(name = "QBDT_DTClient")
    public String dtClient;

    @XmlElement(name = "QBDT_DTPaychecks")
    public String dtPaychecks;

    @XmlElement(name = "QBDT_DTTransactions")
    public String dtTransactions;

    @XmlElement(name = "QBDT_DTPayPeriodBegin")
    public String dtPayPeriodBegin;

    @XmlElement(name = "QBDT_DTPayPeriodEnd")
    public String dtPayPeriodEnd;

    public QBDTDateDTO() {

    }

    public QBDTDateDTO(Date date) {
        this.setDate(date);
    }

    public String getDtClient() {
        return dtClient;
    }

    public void setDtClient(String dtClient) {
        this.dtClient = dtClient;
    }

    public String getDtPaychecks() {
        return dtPaychecks;
    }

    public void setDtPaychecks(String dtPaychecks) {
        this.dtPaychecks = dtPaychecks;
    }

    public String getDtTransactions() {
        return dtTransactions;
    }

    public void setDtTransactions(String dtTransactions) {
        this.dtTransactions = dtTransactions;
    }

    public String getDtPayPeriodBegin() {
        return dtPayPeriodBegin;
    }

    public void setDtPayPeriodBegin(String dtPayPeriodBegin) {
        this.dtPayPeriodBegin = dtPayPeriodBegin;
    }

    public String getDtPayPeriodEnd() {
        return dtPayPeriodEnd;
    }

    public void setDtPayPeriodEnd(String dtPayPeriodEnd) {
        this.dtPayPeriodEnd = dtPayPeriodEnd;
    }

    public void setDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat();

        //format I.DTCLIENT date
        sdf.applyPattern(DateUtilities.QBDT_DTCLIENT_FORMAT);
        this.dtClient = sdf.format(date);

        //format I.DTPAYCHECKS date
        sdf.applyPattern(DateUtilities.QBDT_TXN_FORMAT);
        this.dtPaychecks = sdf.format(date);

        //format I.DTTX date
        sdf.applyPattern(DateUtilities.QBDT_TXN_FORMAT);
        SpcfCalendar cal = SpcfCalendar.createInstance(date.getTime());
        cal.addDays(3);
        //Make sure it doesn't fall on a Weekend or Holiday
        CalendarUtils.getValidDate(cal, 1);
        this.dtTransactions = sdf.format(CalendarUtils.convertToDate(cal));

        //format I.DTPAYPERIODBEGIN date
        sdf.applyPattern(DateUtilities.QBDT_TXN_FORMAT);
        this.dtPayPeriodBegin = sdf.format(DateUtilities.getRandomDay(date, 30));

        //format I.DTPAYPERIODEND date
        sdf.applyPattern(DateUtilities.QBDT_TXN_FORMAT);
        this.dtPayPeriodEnd = sdf.format(DateUtilities.addDaysToDate(date, -1));

    }
}
