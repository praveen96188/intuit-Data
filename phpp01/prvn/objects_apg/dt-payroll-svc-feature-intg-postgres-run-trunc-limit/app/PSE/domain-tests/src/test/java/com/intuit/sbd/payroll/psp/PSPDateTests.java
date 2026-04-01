/*
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.api.PayrollServices;
import com.intuit.sbd.payroll.psp.api.PayrollServicesTest;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import junit.framework.TestCase;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class PSPDateTests {


    @Before
    public void runBeforeEachTest() {
        PayrollServicesTest.beforeEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    @After
    public void runAfterEachTest() {
        PayrollServicesTest.afterEachTest();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
    }

    private String padWithZeroes(int pValueToPad) {
        if (pValueToPad < 10) {
            return "0" + Integer.toString(pValueToPad);
        } else {
            return Integer.toString(pValueToPad);
        }

    }

    @Test
    public void addDaysToPSPDate() {
        /**
         * PSPDate.getPSPTime() takes the current SpcfCalendar time and adds a millisecond offset to it.
         * PSPDate.addDaysToPSPTime() adjusts the offset by that many days' worth of milliseconds.
         * When comparing the adjusted PSPTime, don't use SpcfCalendar.addDays() -- it adds calendar days to the date
         * without taking into account the transition to/from Daylight Savings Time, which would change the hour.
         * Instead, add milliseconds to the current time, like PSPDate.getPSPTime() does.
         */
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar eightyDaysFromNow = SpcfCalendar.createInstance(now.getTimeInMilliseconds() + 80*86400L*1000, SpcfTimeZone.getLocalTimeZone());
        
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(80);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(eightyDaysFromNow, PSPDate.getPSPTime());
    }

    @Test
    public void testResetPSPDate() {
        addDaysToPSPDate();
        PayrollServices.beginUnitOfWork();
        PSPDate.resetPSPTime();
        PayrollServices.commitUnitOfWork();
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        assertYearMonthDayHourMinuteEquals(now, PSPDate.getPSPTime());
    }

    @Test
    public void subtractDaysFromPSPDate() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar fiveHundredDaysAgo = SpcfCalendar.createInstance(now.getTimeInMilliseconds() - 500*86400L*1000, SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.addDaysToPSPTime(-500);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(fiveHundredDaysAgo, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDate_Tomorrow() {
        SpcfCalendar tomorrow = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        tomorrow.addDays(1);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(tomorrow);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(tomorrow, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDate_Yesterday() {
        SpcfCalendar yesterday = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        yesterday.addDays(-1);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(yesterday);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(yesterday, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDate_Now() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(now);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayHourMinuteEquals(now, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDate_ThreeHoursFromNow() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        now.addHours(3);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(now);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayHourMinuteEquals(now, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDate_FiveFifteenToday() {
        SpcfCalendar fiveFifteenToday = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        fiveFifteenToday.setValues(fiveFifteenToday.getYear(), fiveFifteenToday.getMonth(), fiveFifteenToday.getDay(),
                17, 15, 0, 0);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(fiveFifteenToday);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayHourMinuteEquals(fiveFifteenToday, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDate_500DaysAway() {
        SpcfCalendar fiveHundredDaysAway = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        fiveHundredDaysAway.addDays(500);
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(fiveHundredDaysAway);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(fiveHundredDaysAway, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDate_Null() {
        SpcfCalendar nullCalendar = null;
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(nullCalendar);
            PayrollServices.commitUnitOfWork();
            TestCase.fail(
                    "Did not catch excepted exception: PSP time to set cannot be null");
        } catch (Throwable t) {
            PayrollServices.rollbackUnitOfWork();
            assertEquals("PSP time to set cannot be null",
                    t.getMessage());
        }
    }

    /**
     * String tests *
     */
    @Test
    public void testSetPSPDateString_Null() {
        String nullStr = null;
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(nullStr);
            PayrollServices.commitUnitOfWork();
            TestCase.fail(
                    "Did not catch excepted exception: PSP time to set null not in expected format: YYYYMMDDHHMMSS");
        } catch (Throwable t) {
            PayrollServices.rollbackUnitOfWork();
            assertEquals("PSP time to set null not in expected format: YYYYMMDDHHMMSS",
                    t.getMessage());
        }
    }

    @Test
    public void testSetPSPDateString_ZeroLength() {
        String nullStr = "";
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(nullStr);
            PayrollServices.commitUnitOfWork();
            TestCase.fail(
                    "Did not catch excepted exception: PSP time to set  not in expected format: YYYYMMDDHHMMSS");
        } catch (Throwable t) {
            PayrollServices.rollbackUnitOfWork();
            assertEquals("PSP time to set  not in expected format: YYYYMMDDHHMMSS",
                    t.getMessage());
        }
    }

    @Test
    public void testSetPSPDateString_Length15() {
        String nullStr = "200706260434212";
        try {
            PayrollServices.beginUnitOfWork();
            PSPDate.setPSPTime(nullStr);
            PayrollServices.commitUnitOfWork();
            TestCase.fail(
                    "Did not catch excepted exception PSP time to set 200706260434212 not in expected format: YYYYMMDDHHMMSS");
        } catch (Throwable t) {
            PayrollServices.rollbackUnitOfWork();
            assertEquals("PSP time to set 200706260434212 not in expected format: YYYYMMDDHHMMSS",
                    t.getMessage());
        }
    }

    @Test
    public void testSetPSPDateString_Tomorrow() {
        SpcfCalendar tomorrow = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        String strTomorrow = Integer.toString(tomorrow.getYear()) + padWithZeroes(tomorrow.getMonth()) + padWithZeroes(
                tomorrow.getDay()) + padWithZeroes(tomorrow.getHour()) + padWithZeroes(
                tomorrow.getMinute()) + padWithZeroes(tomorrow.getSecond());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(strTomorrow);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(tomorrow, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDateString_Yesterday() {
        SpcfCalendar yesterday = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        yesterday.addDays(-1);
        String strYesterday = Integer.toString(yesterday.getYear()) + padWithZeroes(
                yesterday.getMonth()) + padWithZeroes(
                yesterday.getDay()) + padWithZeroes(yesterday.getHour()) + padWithZeroes(
                yesterday.getMinute()) + padWithZeroes(yesterday.getSecond());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(strYesterday);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(yesterday, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDateString_Now() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        String strNow = Integer.toString(now.getYear()) + padWithZeroes(now.getMonth()) + padWithZeroes(
                now.getDay()) + padWithZeroes(now.getHour()) + padWithZeroes(
                now.getMinute()) + padWithZeroes(now.getSecond());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(strNow);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayHourMinuteEquals(now, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDateString_ThreeHoursFromNow() {
        SpcfCalendar now = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        now.addHours(3);
        String strNow = Integer.toString(now.getYear()) + padWithZeroes(now.getMonth()) + padWithZeroes(
                now.getDay()) + padWithZeroes(now.getHour()) + padWithZeroes(
                now.getMinute()) + padWithZeroes(now.getSecond());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(strNow);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayHourMinuteEquals(now, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDateString_FiveFifteenToday() {
        SpcfCalendar fiveFifteenToday = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        fiveFifteenToday.setValues(fiveFifteenToday.getYear(), fiveFifteenToday.getMonth(), fiveFifteenToday.getDay(),
                17, 15, 0, 0);
        String strFiveFifteen = Integer.toString(fiveFifteenToday.getYear()) + padWithZeroes(
                fiveFifteenToday.getMonth()) + padWithZeroes(
                fiveFifteenToday.getDay()) + padWithZeroes(fiveFifteenToday.getHour()) + padWithZeroes(
                fiveFifteenToday.getMinute()) + padWithZeroes(fiveFifteenToday.getSecond());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(strFiveFifteen);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayHourMinuteEquals(fiveFifteenToday, PSPDate.getPSPTime());
    }

    @Test
    public void testSetPSPDateString_500DaysAway() {
        SpcfCalendar fiveHundredDaysAway = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        fiveHundredDaysAway.addDays(500);
        String strFiveFifteen = Integer.toString(fiveHundredDaysAway.getYear()) + padWithZeroes(
                fiveHundredDaysAway.getMonth()) + padWithZeroes(
                fiveHundredDaysAway.getDay()) + padWithZeroes(fiveHundredDaysAway.getHour()) + padWithZeroes(
                fiveHundredDaysAway.getMinute()) + padWithZeroes(fiveHundredDaysAway.getSecond());
        PayrollServices.beginUnitOfWork();
        PSPDate.setPSPTime(strFiveFifteen);
        PayrollServices.commitUnitOfWork();
        assertYearMonthDayEquals(fiveHundredDaysAway, PSPDate.getPSPTime());
    }

    @Test
    public void daylightToStandardTime() {
        // the transition from Daylight to Standard time is on 2008-11-02 at 02:00:00 (when it will become 01:00:00)

        // noon on the last day of Daylight time...
        SpcfCalendar spcfDaylight = SpcfCalendar.createInstance(2008, 11, 1, 12, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        assertEquals("SPCF Daylight hour", 12, spcfDaylight.getHour());

        // adding "1 day" yields noon on the first day of Standard time, which is 25 hours later
        SpcfCalendar spcfStandardByDays = spcfDaylight.copy();
        spcfStandardByDays.addDays(1);
        assertEquals("Day test: SPCF Standard hour", 12, spcfStandardByDays.getHour());
        assertEquals("Day test: millisecond offset", 25*60*60*1000L, spcfStandardByDays.subtract(spcfDaylight)); // 25 hours

        // adding "86,400,000" milliseconds yields 11 am on the first day of Standard time, which is 24 hours later
        SpcfCalendar spcfStandardByMillis = SpcfCalendar.createInstance(spcfDaylight.getTimeInMilliseconds() + 86400L * 1000, SpcfTimeZone.getLocalTimeZone());
        assertEquals("Millisecond test: SPCF Standard hour", 11, spcfStandardByMillis.getHour());
        assertEquals("Millisecond test: millisecond offset", 24*60*60*1000L, spcfStandardByMillis.subtract(spcfDaylight)); // 24 hours
    }

    private void assertYearMonthDayHourMinuteEquals(SpcfCalendar pTimeToCompare, SpcfCalendar pPSPTime) {
        // compare times in utc to remove dst component
        SpcfCalendar time1 = pTimeToCompare.toUtc();
        SpcfCalendar time2 = pPSPTime.toUtc();

        assertEquals(time1.getDay(), time2.getDay());
        assertEquals(time1.getMonth(), time2.getMonth());
        assertEquals(time1.getYear(), time2.getYear());
        assertEquals(time1.getHour(), time2.getHour());
        assertEquals(time1.getMinute(), time2.getMinute());
    }

    private void assertYearMonthDayEquals(SpcfCalendar pTimeToCompare, SpcfCalendar pPSPTime) {
        // compare times in utc to remove dst component
        SpcfCalendar time1 = pTimeToCompare.toUtc();
        SpcfCalendar time2 = pPSPTime.toUtc();

        assertEquals(time1.getDay(), time2.getDay());
        assertEquals(time1.getMonth(), time2.getMonth());
        assertEquals(time1.getYear(), time2.getYear());
    }

}
