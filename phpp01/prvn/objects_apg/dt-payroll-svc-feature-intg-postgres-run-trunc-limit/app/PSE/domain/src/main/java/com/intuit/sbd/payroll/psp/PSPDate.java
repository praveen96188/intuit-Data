/*
 * $Id: //psp/dev/PSE/Domain/src/com/intuit/sbd/payroll/psp/PSPDate.java#1 $
 *
 * Copyright (c) 2007 Intuit, Inc. All Rights Reserved.
 *
 * Unauthorized reproduction is a violation of applicable law.
 * This material contains certain confidential or proprietary
 * information and trade secrets of Intuit Inc.
 */

package com.intuit.sbd.payroll.psp;

import com.intuit.sbd.payroll.psp.cache.DirtyCheckProcessCache;
import com.intuit.sbd.payroll.psp.domain.SystemParameter;
import com.intuit.sbd.payroll.psp.processes.TransactionThread;
import com.intuit.sbd.payroll.psp.util.CalendarUtils;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;
import com.intuit.spc.foundations.primary.logging.SpcfLogManager;
import com.intuit.spc.foundations.primary.logging.SpcfLogger;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class PSPDate {
    private static long MILLISECONDS_PER_DAY = 86400000;
    private static SpcfLogger logger = SpcfLogManager.getLogger(PSPDate.class);
    private static final String PSP_DATE_OFFSET = "PSP_DATE_OFFSET";
    private static final String PSP_DATE_TIMEZONE_OFFSET = "PSP_DATE_TIMEZONE_OFFSET";

    /**
     * Returns a date that has its time portion set in a way that the date itself is the same
     * regardless of if we look at the date as GMT/UTC or PDT
     */
    public static SpcfCalendar getTimeZoneIndependentDate(SpcfCalendar inDate) {
        SpcfCalendar newDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        newDate.setValues(inDate.getYear(), inDate.getMonth(), inDate.getDay(), 5, 0, 0, 0);
        return newDate;
    }

    /**
     * Returns the PSP-wide time, taking into account any offset previously set by calling
     * either setPSPTime or addDaysToPSPTime
     *
     * @return The PSP-wide time
     */
    public static SpcfCalendar getPSPTime() {
        // See if PSPDate was set in the session cache. If it was, return it
        SpcfCalendar pspDate =  Application.getSessionCache().getNonHibernateObject("PSPDate");
        if (pspDate != null) return pspDate.copy();

        // Get offset from database and return correct date
        Calendar currDate = Calendar.getInstance();
        long currTime = currDate.getTimeInMillis();

        long offsetInMilliseconds = getCurrentOffset();

        return SpcfCalendar.createInstance(currTime + offsetInMilliseconds, SpcfTimeZone.getLocalTimeZone());
    }

    /**
     * String format is: YYYYMMDDHHMMSS.  For example, June 26, 1982 at 4:43PM is: 19820626164300
     *
     * @param pPSPTime
     */
    public static void setPSPTime(String pPSPTime) {
        if (pPSPTime == null || pPSPTime.length() != 14) {
            throw new RuntimeException("PSP time to set " + pPSPTime + " not in expected format: YYYYMMDDHHMMSS");
        }

        SpcfCalendar systemDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        SpcfCalendar newDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        int year = Integer.parseInt(pPSPTime.substring(0, 4));
        int month = Integer.parseInt(pPSPTime.substring(4, 6));
        int day = Integer.parseInt(pPSPTime.substring(6, 8));
        int hour = Integer.parseInt(pPSPTime.substring(8, 10));
        int minute = Integer.parseInt(pPSPTime.substring(10, 12));
        int second = Integer.parseInt(pPSPTime.substring(12, 14));
        newDate.setValues(year, month, day, hour, minute, second, 0);

        setPSPTime(newDate);
    }

    /**
     * Set the PSP time to be the given time.  Should only be called during testing
     *
     * @param pCurrentTime The time to set the PSP-wide time to
     */
    public static void setPSPTime(SpcfCalendar pCurrentTime) {
        if (pCurrentTime == null) {
            throw new RuntimeException("PSP time to set cannot be null");
        }
        SpcfCalendar systemDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());
        Long lOffsetInMilliseconds;
        lOffsetInMilliseconds = pCurrentTime.subtract(systemDate);
        setDBTimeZoneOffset(pCurrentTime);

        persistNewOffset(lOffsetInMilliseconds);
    }

    /**
     * This method is to fix the discrepancy between the java and oracle offsets when the PSPDate which we want to set
     * is in different timezone from the current system timezone.
     * The issue is due to Oracle database always consider the system's current timezone irrespective of the PSPDate's
     * timezone.
     * <p/>
     * ex: If we set the PSPDate in August which is PDT(-7 hours) timezone and if the current system date is in
     * November which is PST (-8 hours), Java calendar adds 7 hours to get UTC and Oracle adds 8 hours to get UTC.
     *
     * @param pPSPTime
     */
    private static void setDBTimeZoneOffset(SpcfCalendar pPSPTime) {
        TimeZone tz = TimeZone.getDefault();
        Date currentDate = Calendar.getInstance().getTime();
        String timeZoneOffset = "+00.00";
        if (tz.inDaylightTime(CalendarUtils.convertToDate(pPSPTime)) && !tz.inDaylightTime(currentDate)) {
            timeZoneOffset = "+01.00";
        }

        if (!tz.inDaylightTime(CalendarUtils.convertToDate(pPSPTime)) && tz.inDaylightTime(currentDate)) {
            timeZoneOffset = "-01.00";
        }

        // save the timezone offset into system parameter
        SystemParameter sysParamTimezone = SystemParameter.findSystemParameter(PSP_DATE_TIMEZONE_OFFSET);

        if (sysParamTimezone == null) {
            throw new RuntimeException("Query for system parameter code " + PSP_DATE_TIMEZONE_OFFSET + " did not return one result as expected");
        }

        Application.getHibernateSession().update(sysParamTimezone);
        sysParamTimezone.setSystemParameterValue(timeZoneOffset);
        Application.save(sysParamTimezone);

        // mark the process cache as dirty (next unit of work will force update)
        DirtyCheckProcessCache.updateDBCacheTokenValue();
    }


    /**
     * Adds the given number of days to the current PSP time. Should only be called during testing
     *
     * @param pDays The number of days to add to the current PSP-wide time
     */
    public static void addDaysToPSPTime(int pDays) {
        long lDayAddOffset = MILLISECONDS_PER_DAY * pDays;
        long newOffset = getCurrentOffset() + lDayAddOffset;

        Calendar currDate = Calendar.getInstance();
        long currTime = currDate.getTimeInMillis();
        setDBTimeZoneOffset(SpcfCalendar.createInstance(currTime + newOffset, SpcfTimeZone.getLocalTimeZone()));

        persistNewOffset(newOffset);
    }

    /**
     * Adds the given number of days to the current PSP time. Should only be called during testing
     *
     * @param pDays The number of days to add to the current PSP-wide time
     */
    public static void addBusinessDaysToPSPTime(int pDays) {
        SpcfCalendar currentDate = PSPDate.getPSPTime().copy();
        CalendarUtils.addBusinessDays(currentDate, pDays);
        setPSPTime(currentDate);
    }

    /**
     * Resets the PSP-wide time to the system time
     */
    public static void resetPSPTime() {
        setDBTimeZoneOffset(SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone()));
        persistNewOffset(0L);
    }

    private static synchronized void persistNewOffset(Long pOffsetInMilliseconds) {
        if (logger.isInfoEnabled()) {
            SpcfCalendar currDate = SpcfCalendar.createInstance(
                    Calendar.getInstance().getTimeInMillis() + pOffsetInMilliseconds, SpcfTimeZone.getLocalTimeZone());
            logger.info(currDate.toString() + ", " + currDate.format("EEEE"));
        }
        SystemParameter sysParamOffset = SystemParameter.findSystemParameter(PSP_DATE_OFFSET);

        if (sysParamOffset == null) {
            throw new RuntimeException("Query for system parameter code " + PSP_DATE_OFFSET + " did not return one result as expected");
        }

        Application.getHibernateSession().update(sysParamOffset);
        sysParamOffset.setSystemParameterValue(Long.toString(pOffsetInMilliseconds));
        Application.save(sysParamOffset);
        Application.getSessionCache().addNonHibernateObject(PSP_DATE_OFFSET, pOffsetInMilliseconds);

        // mark cache as dirty
      DirtyCheckProcessCache.updateDBCacheTokenValue();
    }

    /**
     * Returns current PSPDate offset in miliseconds
     *
     * @return
     */
    public static long getCurrentOffset() {
        if(Application.isProdEnvironment()) {
            return 0;
        }
        Long offsetInMilliseconds = Application.getSessionCache().getNonHibernateObject(PSP_DATE_OFFSET);
        if (offsetInMilliseconds == null) {
            SystemParameter sysParamOffset = SystemParameter.findSystemParameter(PSP_DATE_OFFSET);

            if (sysParamOffset == null) {
                throw new RuntimeException("Query for system parameter code " + PSP_DATE_OFFSET + " did not return one result as expected");
            }

            String strParamOffset = sysParamOffset.getSystemParameterValue();
            offsetInMilliseconds = Long.parseLong(strParamOffset);
            Application.getSessionCache().addNonHibernateObject(PSP_DATE_OFFSET, offsetInMilliseconds);
        }

        return offsetInMilliseconds;
    }

    /**
     * This is to be called from the hibernate interceptor to guarantee we are not going to the database
     *
     * @return
     */
    public static void ensureCurrentOffsetIsCached() {
        Long offsetInMilliseconds = Application.getSessionCache().getNonHibernateObject(PSP_DATE_OFFSET);
        if (offsetInMilliseconds == null) {
            //
            // We need to do this in a separate hibernate session because this method is called from
            // hibernate's interceptor (onFlushDirty event)
            //
           offsetInMilliseconds =
                    Application.<Long>executeTransactionThread(new TransactionThread() {
                        public Long transaction() {
                            DomainEntitySet<SystemParameter> parameters = Application.<SystemParameter>find(SystemParameter.class, SystemParameter.SystemParameterCd().equalTo(PSP_DATE_OFFSET));
                            if (parameters.size() > 0) {
                                return Long.parseLong(parameters.get(0).getSystemParameterValue());
                            }
                            else {
                                return 0L;
                            }
                        }
                    });

            Application.getSessionCache().addNonHibernateObject(PSP_DATE_OFFSET, offsetInMilliseconds);
        }
    }

}
