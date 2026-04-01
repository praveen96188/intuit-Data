package com.intuit.sbd.payroll.psp.domain.util;

import com.intuit.sbd.payroll.psp.processes.DataLoadServices;
import org.junit.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Aug 15, 2007
 * Time: 10:51:50 AM
 *  Contains the unit tests for the <CODE>CalendarUtils</CODE> class.
 */

public class CalendarUtilsTests {
    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
    } 

    @Test
    public void validateClearTime() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.January, 31, 23, 34, 22, 2);
        CalendarUtils.clearTime(calendar);
        assertTrue((calendar.getYear()==2007) &&
                   (calendar.getMonth()==SpcfCalendar.January) &&
                   (calendar.getDay()== 31) &&
                   (calendar.getHour()==0) &&
                   (calendar.getMinute()==0) &&
                   (calendar.getSecond()==0) &&
                   (calendar.getMillisecond()==0));
    }

    @Test
    public void validateSaturdayIsWeekend() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.August, 4, SpcfTimeZone.getLocalTimeZone());
        assertTrue(CalendarUtils.isWeekend(calendar));
    }

//    @Test
//    public void validateNextDepositDay() {
//        SpcfCalendar calendar = SpcfCalendar.createInstance(2008,SpcfCalendar.March, 16, SpcfTimeZone.getLocalTimeZone());
//        CalendarUtils.getNextValidPaycheckDepositDate(calendar,2);
//        assertFalse(CalendarUtils.isWeekend(calendar));
//    }

    @Test
    public void validateSundayIsWeekend() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.August, 5, SpcfTimeZone.getLocalTimeZone());
        assertTrue(CalendarUtils.isWeekend(calendar));
    }

    @Test
    public void validateIsNotWeekend() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.August, 6, SpcfTimeZone.getLocalTimeZone());
        assertFalse(CalendarUtils.isWeekend(calendar));
    }

    @Test
    public void validateIsHoliday() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 4, SpcfTimeZone.getLocalTimeZone());
        assertTrue(CalendarUtils.isWeekendOrHoliday(calendar));
    }

    @Test
    public void validateIsNotHoliday() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 5, SpcfTimeZone.getLocalTimeZone());
        assertFalse(CalendarUtils.isWeekendOrHoliday(calendar));
    }

    @Test
    public void validateGetValidDateSaturday() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 7, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.getValidDate(calendar,1);
        assertTrue(calendar.equals(SpcfCalendar.createInstance(2007,SpcfCalendar.July,9, SpcfTimeZone.getLocalTimeZone())));
    }

    @Test
    public void validateGetValidDateSunday() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 8, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.getValidDate(calendar,1);
        assertTrue(calendar.equals(SpcfCalendar.createInstance(2007,SpcfCalendar.July,9, SpcfTimeZone.getLocalTimeZone())));
    }

    @Test
    public void validateGetValidDateHoliday() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 4, SpcfTimeZone.getLocalTimeZone());
        CalendarUtils.getValidDate(calendar,1);
        assertTrue(calendar.equals(SpcfCalendar.createInstance(2007,SpcfCalendar.July,5, SpcfTimeZone.getLocalTimeZone())));

    }

    @Test
    public void validateGetValidDateWeekday() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 5, SpcfTimeZone.getLocalTimeZone());
        assertTrue(calendar.equals(SpcfCalendar.createInstance(2007,SpcfCalendar.July,5, SpcfTimeZone.getLocalTimeZone())));
        assertFalse(CalendarUtils.isWeekendOrHoliday(calendar));
    }

    @Test
    public void validateGetDifferenceInDays() {
        // First date is before second date - difference is negative
        SpcfCalendar calendar1 = SpcfCalendar.createInstance(2007,SpcfCalendar.January, 5, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar calendar2 = SpcfCalendar.createInstance(2007,SpcfCalendar.January, 21, SpcfTimeZone.getLocalTimeZone());
        assertTrue(CalendarUtils.getDifferenceInDays(calendar1, calendar2) == -16);
        calendar2.addDays(322);
        assertTrue(CalendarUtils.getDifferenceInDays(calendar1, calendar2) == -338);
        // First date is after second date, difference is positive
        calendar1.setValues(2007, SpcfCalendar.January, 21);
        calendar2.setValues(2007, SpcfCalendar.January, 5);
        assertTrue(CalendarUtils.getDifferenceInDays(calendar1, calendar2) == 16);
        calendar1.addDays(322);
        assertTrue(CalendarUtils.getDifferenceInDays(calendar1, calendar2) == 338);

    }

    @Test
    public void validateGetDifferenceInDaysAbs() {
        // First date is before second date - difference is negative
        SpcfCalendar calendar1 = SpcfCalendar.createInstance(2007,SpcfCalendar.January, 5, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar calendar2 = SpcfCalendar.createInstance(2007,SpcfCalendar.January, 21, SpcfTimeZone.getLocalTimeZone());
        assertTrue(CalendarUtils.getDifferenceInDaysAbs(calendar1, calendar2) == 16);
        calendar2.addDays(322);
        assertTrue(CalendarUtils.getDifferenceInDaysAbs(calendar1, calendar2) == 338);
        // First date is after second date, difference is positive
        calendar1.setValues(2007, SpcfCalendar.January, 21);
        calendar2.setValues(2007, SpcfCalendar.January, 5);
        assertTrue(CalendarUtils.getDifferenceInDaysAbs(calendar1, calendar2) == 16);
        calendar1.addDays(322);
        assertTrue(CalendarUtils.getDifferenceInDaysAbs(calendar1, calendar2) == 338);

    }

    @Test
    public void validateBusinessDaysFromDateToDate() {
        // From Date < To Date
        SpcfCalendar calendar1 = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 1, SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar calendar2 = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 21, SpcfTimeZone.getLocalTimeZone());
        assertTrue(CalendarUtils.businessDaysFromDateToDate(calendar1, calendar2) == 14);

        // From Date > To Date
        calendar1.setValues(2007,SpcfCalendar.July, 21);
        calendar2.setValues(2007,SpcfCalendar.July, 1);
        assertTrue(CalendarUtils.businessDaysFromDateToDate(calendar1, calendar2) == -14);
    }

    @Test
    public void validateBusinessDaysToCurrentYearEnd() {
       SpcfCalendar calendar1 =
               SpcfCalendar.createInstance(SpcfCalendar.getNow().getYear(),
                       SpcfCalendar.July,
                       1,
                       SpcfTimeZone.getLocalTimeZone());
       int businessDays = CalendarUtils.businessDaysToCurrentYearEnd(calendar1);
       SpcfCalendar yearEnd = SpcfCalendar.createInstance(SpcfCalendar.getNow().getYear(),SpcfCalendar.December,31,23,59,59,59);
       assertTrue(CalendarUtils.businessDaysFromDateToDate(calendar1,yearEnd)== businessDays);
    }

    @Test
    public void validateAddBusinessDays() {
       // business days > 0
       SpcfCalendar calendar1 = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 1, SpcfTimeZone.getLocalTimeZone());
       SpcfCalendar calendar2 = SpcfCalendar.createInstance(2007,SpcfCalendar.July, 20, SpcfTimeZone.getLocalTimeZone());
       CalendarUtils.addBusinessDays(calendar1,14);
       assertTrue(calendar1.equals(calendar2));

       // business days = 0
        calendar1.setValues(2007, SpcfCalendar.July,1);
        calendar2.setValues(2007, SpcfCalendar.July,1);
        CalendarUtils.addBusinessDays(calendar1,0);
        assertTrue(calendar1.equals(calendar2));

         // business days < 0
        calendar1.setValues(2007, SpcfCalendar.July,20);
        calendar2.setValues(2007, SpcfCalendar.June, 29);
        CalendarUtils.addBusinessDays(calendar1,-14);
        assertTrue(calendar1.equals(calendar2));
    }

    @Test
    public void validateConvertToSpcfCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2007,Calendar.SEPTEMBER, 20);
        SpcfCalendar spcfCalendar = CalendarUtils.convertToSpcfCalendar(calendar);
        assertTrue(spcfCalendar.getYear() == 2007);
        assertTrue(spcfCalendar.getMonth() == SpcfCalendar.September);
        assertTrue(spcfCalendar.getDay() == 20);
    }

    @Test
    public void testGetPSPDateFromDB() {
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime("20061010121212");
        PayrollServices.commitUnitOfWork();
        PayrollServices.beginUnitOfWork();
        SpcfCalendar spcfCalendar = CalendarUtils.getPSPDateFromDB();
        PayrollServices.commitUnitOfWork();
        assertTrue(spcfCalendar.getYear() == 2006);
        assertTrue(spcfCalendar.getMonth() == 10);
        assertTrue(spcfCalendar.getDay() == 10);
        assertTrue(spcfCalendar.getHour() == 12);
    }

    @Test
    public void testGetLastDayOfQuarter() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.August, 5, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Third quarter", CalendarUtils.getLastDayOfQuarter(calendar), SpcfCalendar.createInstance(2007, SpcfCalendar.September, 30, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.January, 1, SpcfTimeZone.getLocalTimeZone());
        assertEquals("First quarter", CalendarUtils.getLastDayOfQuarter(calendar), SpcfCalendar.createInstance(2007, SpcfCalendar.March, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.March, 31, SpcfTimeZone.getLocalTimeZone());
        assertEquals("First quarter", CalendarUtils.getLastDayOfQuarter(calendar), SpcfCalendar.createInstance(2007, SpcfCalendar.March, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.April, 1, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Second quarter", CalendarUtils.getLastDayOfQuarter(calendar), SpcfCalendar.createInstance(2007, SpcfCalendar.June, 30, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.October, 1, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Last quarter", CalendarUtils.getLastDayOfQuarter(calendar), SpcfCalendar.createInstance(2007, SpcfCalendar.December, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
        calendar = SpcfCalendar.createInstance(2007,SpcfCalendar.December, 31, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Last quarter", CalendarUtils.getLastDayOfQuarter(calendar), SpcfCalendar.createInstance(2007, SpcfCalendar.December, 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone()));
    }

    @Test
    public void testConvertCalendarToXmlStringNoMilliSeconds() {
        SpcfCalendar calendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        calendar.setValues(2013, 2, 11, 23, 59, 1, 0);
        assertEquals("timestamp string does not match", "2013-02-11T23:59:01-08:00", CalendarUtils.convertCalendarToXmlStringNoMilliSeconds(calendar));
        calendar.setValues(2013, 4, 11, 23, 59, 1, 0);
        assertEquals("timestamp string does not match", "2013-04-11T23:59:01-07:00", CalendarUtils.convertCalendarToXmlStringNoMilliSeconds(calendar));
    }
}
