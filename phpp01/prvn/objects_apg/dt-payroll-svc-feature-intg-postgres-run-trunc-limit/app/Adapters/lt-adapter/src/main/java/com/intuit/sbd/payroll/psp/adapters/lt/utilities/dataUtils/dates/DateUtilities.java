package com.intuit.sbd.payroll.psp.adapters.lt.utilities.dataUtils.dates;

import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.adapters.qbdtws.payroll.dtos.QBDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: msalayko
 * Date: Oct 5, 2010
 * Time: 9:09:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class DateUtilities {

    public static final String QBOE_EVENTSYNC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SZ";
    public static final String QBOE_TARGET_TXN_FORMAT = "yyyy-MM-dd";
    public static final String QBDT_DTCLIENT_FORMAT = "yyyyMMddHHmmss";
    public static final String QBDT_TXN_FORMAT = "yyyyMMdd";
    public static final String EWS_TIMESTAMP_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SZ";
    public static final String TESTWS_SHORT_FORMAT = "yyyyMMdd";
    public static final String PSP_DATE_FORMAT = "yyyyMMddHHmmss";

    public enum DateSpecification {
        PSP_DATE, RANDOM, SPECIFIC
    }

     public enum LtSourceSystemCode {
        TESTWS,

        EWS,

        QBOE, 

        QBDT,

        QBDTWS,

        ALL_WS
 }



    public static Date getToday(){
        return new Date(PSPDate.getPSPTime().getTimeInMilliseconds());
    }

    public static Date getRandomDay(){
        //How far in the furture can the payroll be dated
        int payrollWindow = 14;

        //Build the calendar and increment by a random number (up to "payrollWindow"
        SpcfCalendar cal = PSPDate.getPSPTime();
        Random randomGenerator = new Random();
        int r = randomGenerator.nextInt(payrollWindow)+1;
        cal.addDays(r);

        //Make sure it doesn't fall on a Weekend or Holiday
        CalendarUtils.getValidDate(cal, 1);

        return CalendarUtils.convertToDate(cal);
    }


    public static Date getRandomDay(Date pDate, int pWindow){
        //How far in the furture can the payroll be dated
        int payrollWindow = pWindow;

        //Build the calendar and increment by a random number (up to "payrollWindow"
        SpcfCalendar cal = CalendarUtils.convertToSpcfCalendar(pDate);
        Random randomGenerator = new Random();
        int r = randomGenerator.nextInt(payrollWindow)+1;
        cal.addDays(0-r);

        //Make sure it doesn't fall on a Weekend or Holiday
        CalendarUtils.getValidDate(cal, 1);

        return CalendarUtils.convertToDate(cal);
    }

    public static Date addDaysToDate(Date pDate, int pDaysToAdd){


        //Build the calendar and increment by a random number (up to "payrollWindow"
        SpcfCalendar cal = CalendarUtils.convertToSpcfCalendar(pDate);
        cal.addDays(pDaysToAdd);

        //Make sure it doesn't fall on a Weekend or Holiday
        CalendarUtils.getValidDate(cal, 1);

        return CalendarUtils.convertToDate(cal);
    }


}
