package com.intuit.sbd.payroll.psp.util;

import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.sbd.payroll.psp.Application;
import com.intuit.sbd.payroll.psp.DomainEntitySet;
import com.intuit.sbd.payroll.psp.PSPDate;
import com.intuit.sbd.payroll.psp.domain.BankHoliday;
import com.intuit.sbd.payroll.psp.query.Query;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portability.util.SpcfTimeZone;

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * Created by IntelliJ IDEA.
 * User: mvillani
 * Date: Aug 13, 2007
 * Time: 4:29:22 PM
 * To change this template use File | Settings | File Templates.
 */
public class CalendarUtils {
    public static SpcfCalendar createInstanceFromDate(String dateYYYYMMDD) {
        if (dateYYYYMMDD == null || dateYYYYMMDD.length() != 8) {
            throw new RuntimeException("PSP time to set " + dateYYYYMMDD + " not in expected format: YYYYMMDD");
        }
        return createInstanceFromDateTime(dateYYYYMMDD + "000000");
    }

    public static SpcfCalendar createInstanceFromDateTime(String dateYYYYMMDDHHMMSS) {
        if (dateYYYYMMDDHHMMSS == null || dateYYYYMMDDHHMMSS.length() != 14) {
            throw new RuntimeException("PSP time to set " + dateYYYYMMDDHHMMSS + " not in expected format: YYYYMMDDHHMMSS");
        }

        SpcfCalendar newDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        int year = Integer.parseInt(dateYYYYMMDDHHMMSS.substring(0, 4));
        int month = Integer.parseInt(dateYYYYMMDDHHMMSS.substring(4, 6));
        int day = Integer.parseInt(dateYYYYMMDDHHMMSS.substring(6, 8));
        int hour = Integer.parseInt(dateYYYYMMDDHHMMSS.substring(8, 10));
        int minute = Integer.parseInt(dateYYYYMMDDHHMMSS.substring(10, 12));
        int second = Integer.parseInt(dateYYYYMMDDHHMMSS.substring(12, 14));
        newDate.setValues(year, month, day, hour, minute, second, 0);

        return newDate;
    }


    public static SpcfCalendar createInstanceFromTimestampString(String pTimestamp) {
//        if (pTimestamp == null || pTimestamp.length() != 23) {
        //          throw new RuntimeException("PSP time to set " + pTimestamp + " not in expected format: YYYY/MM/DD/HH:MM:SS");
        //      }

        SpcfCalendar newDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        int year = Integer.parseInt(pTimestamp.substring(0, 4));
        int month = Integer.parseInt(pTimestamp.substring(5, 7));
        int day = Integer.parseInt(pTimestamp.substring(8, 10));
        int hour = Integer.parseInt(pTimestamp.substring(11, 13));
        int minute = Integer.parseInt(pTimestamp.substring(14, 16));
        int second = Integer.parseInt(pTimestamp.substring(17, 19));
        newDate.setValues(year, month, day, hour, minute, second, 0);

        return newDate;
    }

    public static SpcfCalendar createInstanceFromXMLGregorianCalendar(XMLGregorianCalendar pCalendar) {

        SpcfCalendar newDate = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        int year = pCalendar.getYear();
        int month = pCalendar.getMonth();
        int day = pCalendar.getDay();
        int hour = pCalendar.getHour();
        int minute = pCalendar.getMinute();
        int second = pCalendar.getSecond();

        if (hour == DatatypeConstants.FIELD_UNDEFINED) {
            hour = 0;
        }
        if (minute == DatatypeConstants.FIELD_UNDEFINED) {
            minute = 0;
        }
        if (second == DatatypeConstants.FIELD_UNDEFINED) {
            second = 0;
        }

        newDate.setValues(year, month, day, hour, minute, second, 0);

        return newDate;
    }

    /**
     * Clears the time fields from a SpcfCalendar, enabling comparisons of dates using equals.
     *
     * @param cal
     */
    public static void clearTime(SpcfCalendar cal) {
        cal.setValues(cal.getYear(), cal.getMonth(), cal.getDay(), 0, 0, 0, 0);
    }

    public static SpcfCalendar setTime12AM(SpcfCalendar cal) {
        return SpcfCalendar.createInstance(cal.getYear(), cal.getMonth(), cal.getDay());
    }

    public static SpcfCalendar endOfDay(SpcfCalendar cal) {
        cal.setValues(cal.getYear(), cal.getMonth(), cal.getDay(), 23, 59, 59, 999);
        return cal;
    }

    /**
     * Utility method used to find and set a valid non weekend or holiday date. The util will increment the calendars days
     * by the increment amount and check against each day until a valid day is found.
     *
     * @param date
     * @param increment
     */
    public static void getValidDate(SpcfCalendar date, int increment) {
        while (CalendarUtils.isWeekendOrHoliday(date)) {
            date.addDays(increment);
        }
    }
    /**
     * Helper method to determine if a given date is first day of month
     *
     * @param date date to check against
     * @return true if the given day is first day of month
     */
    public static boolean isFirstDayOfMonth(SpcfCalendar date) {
        SpcfCalendar currentDate = date.toLocal().copy();
        CalendarUtils.clearTime(currentDate);
        SpcfCalendar firstDayOfCurrMonth = CalendarUtils.getFirstDayOfMonth(date) ;
        CalendarUtils.clearTime(firstDayOfCurrMonth);
        return currentDate.equals(firstDayOfCurrMonth) ;
    }
    /**
     * Helper method to determine if a given date is last day of month
     *
     * @param date date to check against
     * @return true if the given day is last day of month
     */
    public static boolean isLastDayOfMonth(SpcfCalendar date) {
        SpcfCalendar currentDate = date.toLocal().copy();
        CalendarUtils.clearTime(currentDate);
        SpcfCalendar lastDayOfTheMonth = CalendarUtils.getLastDayOfMonth(date) ;
        CalendarUtils.clearTime(lastDayOfTheMonth);
        return currentDate.equals(lastDayOfTheMonth) ;
    }



    /**
     * Helper method to determine if a given date falls on a weekend or a holiday.
     *
     * @param day day to check against
     * @return true if the given day is a weekend or a holiday
     */
    public static boolean isWeekendOrHoliday(SpcfCalendar day) {
        //
        // The caching below is to avoid going to the database multiple times and to avoid the creation of instances
        // of spcfCalendar for a day we already know is a weekend/holiday
        //
        Object isWeekendOrHoliday = Application.getSessionCache().getNonHibernateObject(day.getTimeInMilliseconds());
        if (isWeekendOrHoliday == null) {
            SpcfCalendar compareDay = day.copy();
            compareDay = setTime12AM(compareDay);
            isWeekendOrHoliday = CalendarUtils.isWeekend(compareDay) || isHoliday(compareDay);
            Application.getSessionCache().addNonHibernateObject(day.getTimeInMilliseconds(), isWeekendOrHoliday);
        }
        return (Boolean) isWeekendOrHoliday;
    }

    /**
     * Helper method to determine if a given date falls on a holiday
     */
    public static boolean isHoliday(SpcfCalendar day) {
        Object bankHolidayMap = Application.getSessionCache().getNonHibernateObject("BankHolidayMap");
        if (bankHolidayMap == null) {
            // cache all holidays
            DomainEntitySet<BankHoliday> bankHolidays = Application.find(BankHoliday.class, new Query<BankHoliday>());
            HashMap<Long, BankHoliday> holidayMap = new HashMap<Long, BankHoliday>(bankHolidays.size());
            for (BankHoliday bankHoliday : bankHolidays) {
                holidayMap.put(new Long(bankHoliday.getBankHolidayDate().getTimeInMilliseconds()), bankHoliday);
            }
            Application.getSessionCache().addNonHibernateObject("BankHolidayMap", holidayMap);
            bankHolidayMap = holidayMap;
        }

        SpcfCalendar compareDay = day.copy();
        compareDay = setTime12AM(compareDay);

        return ((HashMap<Long, BankHoliday>) bankHolidayMap).containsKey(new Long(compareDay.getTimeInMilliseconds()));
    }

    /**
     * Utility method which can test a day of the year to see if it is a weekend day.
     *
     * @param day day to test
     * @return true if the day falls on a weekend
     */
    public static boolean isWeekend(SpcfCalendar day) {
        int dayOfWeek = day.getDayOfWeek();

        return dayOfWeek == SpcfCalendar.Saturday || dayOfWeek == SpcfCalendar.Sunday;
    }

    /**
     * Utility method which will return the difference between two dates as an int.
     *
     * @param cal1 first date
     * @param cal2 second date
     * @return difference between the two dates
     */
    public static Long getDifferenceInHours(SpcfCalendar cal1, SpcfCalendar cal2) {
        long milis1;
        long milis2;

        if (cal1.before(cal2)) {
            milis1 = cal1.getTimeInMilliseconds();
            milis2 = cal2.getTimeInMilliseconds();
        } else {
            milis1 = cal2.getTimeInMilliseconds();
            milis2 = cal1.getTimeInMilliseconds();
        }

        long diff = milis2 - milis1;
        return diff / (60 * 60 * 1000);
    }

    /**
     * Utility method which will return the difference between two dates as an int.
     *
     * @param cal1 first date
     * @param cal2 second date
     * @return difference between the two dates
     */
    public static Long getDifferenceInSeconds(SpcfCalendar cal1, SpcfCalendar cal2) {
        long milis1;
        long milis2;

        if (cal1.before(cal2)) {
            milis1 = cal1.getTimeInMilliseconds();
            milis2 = cal2.getTimeInMilliseconds();
        } else {
            milis1 = cal2.getTimeInMilliseconds();
            milis2 = cal1.getTimeInMilliseconds();
        }

        long diff = milis2 - milis1;
        return diff / (1000);
    }

    /**
     * Utility method which will return the difference between two dates as an int.
     *
     * @param cal1 first date
     * @param cal2 second date
     * @return difference between the two dates
     */
    public static int getDifferenceInDays(SpcfCalendar cal1, SpcfCalendar cal2) {

        int cal1Year = cal1.getYear();
        int cal2Year = cal2.getYear();
        int cal1DayOfYear = cal1.getDayOfYear();
        int cal2DayOfYear = cal2.getDayOfYear();
        int daysDifference = 365 * (cal2Year - cal1Year) + cal2DayOfYear - cal1DayOfYear;
        return -daysDifference;
    }

    /**
     * Utility method which will return the difference between two dates as an int.
     *
     * @param cal1 first date
     * @param cal2 second date
     * @return difference between the two dates
     */
    public static int getDifferenceInDaysAbs(SpcfCalendar cal1, SpcfCalendar cal2) {
        return Math.abs(CalendarUtils.getDifferenceInDays(cal1, cal2));
    }

    /**
     * Returns number of business banking days between the specified
     * date and the year end
     *
     * @param date
     * @return Returns number of business banking days between the specified
     *         date and the current year end.  The return will be negative if the date
     *         is after the current year end.
     */
    public static int businessDaysToCurrentYearEnd(SpcfCalendar date) {
        if (date == null) {
            throw new IllegalArgumentException("Date to compare is null");
        }

        SpcfCalendar yearEnd = SpcfCalendar.createInstance(SpcfCalendar.getNow().getYear(), SpcfCalendar.December, 31, 23, 59, 59, 59);
        int dif = CalendarUtils.businessDaysFromDateToDate(date, yearEnd);

        return dif;
    }

    public static SpcfCalendar getLastDayOfQuarter(SpcfCalendar pDate) {
        SpcfCalendar quarterEnd = null;

        if (pDate != null) {
            int quarter = ((pDate.getMonth() - 1) / 3) + 1;

            if (quarter == 2 || quarter == 3) {
                quarterEnd = SpcfCalendar.createInstance(pDate.getYear(), (quarter * 3), 30, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
            } else {
                quarterEnd = SpcfCalendar.createInstance(pDate.getYear(), (quarter * 3), 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
            }
        }

        return quarterEnd;
    }

    public static SpcfCalendar getFirstDayOfQuarter(SpcfCalendar pDate) {
        SpcfCalendar quarterBegin = null;

        if (pDate != null) {
            int quarter = ((pDate.getMonth() - 1) / 3) + 1;
            quarterBegin = SpcfCalendar.createInstance(pDate.getYear(), ((quarter * 3) - 2), 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        }
        return quarterBegin;
    }

    public static SpcfCalendar getLastDayOfQuarter(int pYear, int pQtr) {
        SpcfCalendar quarterEnd = null;
        if (pQtr > 0 && pQtr < 5) {
            if (pQtr == 2 || pQtr == 3) {
                quarterEnd = SpcfCalendar.createInstance(pYear, (pQtr * 3), 30, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
            } else {
                // should be either 1 0r 4
                quarterEnd = SpcfCalendar.createInstance(pYear, (pQtr * 3), 31, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
            }
        }
        return quarterEnd;
    }


    public static SpcfCalendar getFirstDayOfQuarter(int pYear, int pQtr) {
        SpcfCalendar quarterBegin = null;
        if (pQtr > 0 && pQtr < 5) {
            quarterBegin = SpcfCalendar.createInstance(pYear, ((pQtr * 3) - 2), 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
        }
        return quarterBegin;
    }

    public static int getQuarterAsInt(SpcfCalendar pDate) {
        int retval = 0;
        if (pDate != null) {
            retval = ((pDate.getMonth() - 1) / 3) + 1;
        }
        return retval;
    }

    public static SpcfCalendar getFirstDayOfPreviousQuarter(SpcfCalendar pDate) {
        if (pDate != null) {
            int year = pDate.getYear();
            int previousQuarter;
            int currentQuarter = getQuarterAsInt(pDate);
            if (currentQuarter == 1) {
                previousQuarter = 4;
                year = year - 1;
            } else {
                previousQuarter = currentQuarter - 1;
            }
            return getFirstDayOfQuarter(year, previousQuarter);
        }
        return null;
    }

    public static SpcfCalendar getFirstDayOfNextQuarter(SpcfCalendar pDate) {
        if (pDate != null) {
            int year = pDate.getYear();
            int NextQuarter;
            int currentQuarter = getQuarterAsInt(pDate);
            if (currentQuarter == 4) {
                NextQuarter = 1;
                year = year + 1;
            } else {
                NextQuarter = currentQuarter + 1;
            }
            return getFirstDayOfQuarter(year, NextQuarter);
        }
        return null;
    }

    public static SpcfCalendar getLastDayOfPreviousQuarter(SpcfCalendar pDate) {
        if (pDate != null) {
            int year = pDate.getYear();
            int previousQuarter;
            int currentQuarter = getQuarterAsInt(pDate);
            if (currentQuarter == 1) {
                previousQuarter = 4;
                year = year - 1;
            } else {
                previousQuarter = currentQuarter - 1;
            }
            return getLastDayOfQuarter(year, previousQuarter);
        }

        return null;
    }

    public static int getPreviousQuarter(SpcfCalendar pDate) {
        int retval = 0;
        if (pDate != null) {
            retval = ((pDate.getMonth() - 1) / 3);
            if (retval == 0) retval = 4;
        }
        return retval;
    }

    public static int getYearAndQuarterAsInt(SpcfCalendar pDate) {
        int retval = 0;
        if (pDate != null) {
            retval = pDate.getYear() * 10 + ((pDate.getMonth() - 1) / 3) + 1;
        }
        return retval;
    }

    @Deprecated
    /**
     * @deprecated most methods on this class are local time, but this one isn't.  would like to delete it for consistency.
     * @see #getFirstDayOfTheYearLocal(SpcfCalendar)
     */
    public static SpcfCalendar getFirstDayOfTheYear(SpcfCalendar cal) {
        return SpcfCalendar.createInstance(cal.getYear(), 1, 1);
    }

    public static SpcfCalendar getFirstDayOfTheYearLocal(SpcfCalendar cal) {
        return SpcfCalendar.createInstance(cal.getYear(), 1, 1, SpcfTimeZone.getLocalTimeZone());
    }

    public static SpcfCalendar getFirstDayOfMonth(SpcfCalendar pDate) {
        return SpcfCalendar.createInstance(pDate.getYear(), pDate.getMonth(), 1, 0, 0, 0, 0, SpcfTimeZone.getLocalTimeZone());
    }

    public static SpcfCalendar getLastDayOfMonth(SpcfCalendar pDate) {
        return SpcfCalendar.createInstance(pDate.getYear(), pDate.getMonth(), pDate.getDaysInMonth(), 23, 59, 59, 999, SpcfTimeZone.getLocalTimeZone());
    }

    public static SpcfCalendar getFirstDayOfPrevMonth(SpcfCalendar pDate) {
        SpcfCalendar aDate = pDate.copy();
        aDate.addMonths(-1);
        return getFirstDayOfMonth(aDate);
    }
    
    /**
     * Utility method which will return the first day of requested previous X Quarters
     *
     * @param pDate non NULL valid date
     * @param xQuarters int value >= 0
     * @return First day of requested previous X Quarters as SpcfCalendar
     */
    public static SpcfCalendar getFirstDayOfPrevXQuarter(SpcfCalendar pDate, int xQuarters) {
        SpcfCalendar xDate = null;
        if (pDate != null && xQuarters >= 0) {
            SpcfCalendar tmpDate = SpcfCalendar.createInstance(pDate.getTimeInMilliseconds());
            tmpDate.addMonths(-(xQuarters * 3));
            xDate = CalendarUtils.getFirstDayOfQuarter(tmpDate);
        }

        return xDate;
    }

    public static boolean isPriorQuarter(SpcfCalendar cal) {
        SpcfCalendar quarterStartDate = getFirstDayOfQuarter(PSPDate.getPSPTime());
        return quarterStartDate.after(cal);
    }

    public static boolean isCurrentQuarter(SpcfCalendar cal) {
        SpcfCalendar now = PSPDate.getPSPTime();
        SpcfCalendar quarterStartDate = getFirstDayOfQuarter(now);
        SpcfCalendar quarterEndDate = getLastDayOfQuarter(now);
        return (quarterStartDate.before(cal) || quarterStartDate.equals(cal)) &&
                (cal.before(quarterEndDate) || cal.equals(quarterEndDate));
    }


    /**
     * Returns number of business days (Mon - Fri and not holidays) as intger.
     * if fromDate is after toDate, the result returned is negative
     *
     * @param fromDate
     * @param toDate
     * @return Returns number of business days (Mon - Fri and not holidays) as intger.
     *         if fromDate is after toDate, the result returned is negative
     */
    public static int businessDaysFromDateToDate(SpcfCalendar fromDate, SpcfCalendar toDate) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException("Dates to compare are null");
        }

        // work on a copy
        SpcfCalendar start = SpcfCalendar.createInstance();
        SpcfCalendar end = null;
        int increment;
        if (toDate.after(fromDate)) {
            start.setValues(fromDate.getYear(), fromDate.getMonth(), fromDate.getDay());
            end = toDate;
            increment = 1;
        } else {
            start.setValues(toDate.getYear(), toDate.getMonth(), toDate.getDay());
            end = fromDate;
            increment = -1;
        }

        int ret = 0;
        start.addDays(1);
        for (; end.after(start); start.addDays(1)) {
            if (!CalendarUtils.isWeekendOrHoliday(start)) {
                ret += increment;
            }
        }

        return ret;
    }

    /**
     * Adds a number of business days to a SpcfCalendar
     *
     * @param calendar
     * @param businessDays
     */
    public static void addBusinessDays(SpcfCalendar calendar, int businessDays) {
        if (businessDays == 0) {
            return;
        }
        int increment = (businessDays < 0) ? -1 : ((businessDays > 0) ? 1 : 0);

        int counter = 0;
        for (calendar.addDays(increment); ; calendar.addDays(increment)) {
            if (!CalendarUtils.isWeekendOrHoliday(calendar)) {
                if ((counter += increment) == businessDays) {
                    break;
                }
            }
        }
    }

    /**
     * Converts a standard Calendar to SpcfCalendar
     *
     * @param pCalendar
     * @return SpcfCalendar
     */
    public static SpcfCalendar convertToSpcfCalendar(Calendar pCalendar) {
        return SpcfCalendar.createInstance(pCalendar.getTimeInMillis(), SpcfTimeZone.getLocalTimeZone());
    }

    /**
     * Converts an SpcfCalendar to a standard Calendar
     *
     * @param spcfCalendar
     * @return java.util.Calendar version of the SpcfCalendar
     */
    public static Calendar convertToCalendar(SpcfCalendar spcfCalendar) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(spcfCalendar.getTimeInMilliseconds());
        return calendar;
    }

    /**
     * Converts a rules Calendar to SpcfCalendar
     *
     * @param pRulesCalendar
     * @return SpcfCalendar
     */
    public static SpcfCalendar convertToSpcfCalendar(RulesCalendar pRulesCalendar) {
        return SpcfCalendar.createInstance(pRulesCalendar.getYear(), pRulesCalendar.getMonth(), pRulesCalendar.getDay(), SpcfTimeZone.getLocalTimeZone());
    }

    /**
     * Converts an SpcfCalendar to a Rules Calendar
     *
     * @param spcfCalendar
     * @return RulesCalendar
     */
    public static RulesCalendar convertToRulesCalendar(SpcfCalendar spcfCalendar) {
        RulesCalendar rulesCalendar = RulesCalendar.createCalendar(spcfCalendar.getYear(), spcfCalendar.getMonth(), spcfCalendar.getDay());
        return rulesCalendar;
    }

    /**
     * Converts an SpcfCalendar to a date
     *
     * @param spcfCalendar
     * @return java.util.Date version of the SpcfCalendar
     */
    public static Date convertToDate(SpcfCalendar spcfCalendar) {
        return new Date(spcfCalendar.getTimeInMilliseconds());
    }

    /**
     * Converts Date to SpcfCalendar
     *
     * @param date
     * @return
     */

    public static SpcfCalendar convertToSpcfCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return convertToSpcfCalendar(calendar);
    }

    /**
     * Gets the database timestamp
     *
     * @return SpcfCalendar
     */
    public static SpcfCalendar getPSPDateFromDB() {
        Timestamp executionTimestamp = (Timestamp) Application.executeNamedQuery("getPSPDateFromDB", null, null).get(0);

        SpcfCalendar dbTimestamp = SpcfCalendar.createInstance(executionTimestamp.getTime(), SpcfTimeZone.getLocalTimeZone());

        return dbTimestamp;
    }

    public static Date getDateWithoutSeconds(SpcfCalendar pSpcfCalendar) {
        pSpcfCalendar.setValues(pSpcfCalendar.getYear(), pSpcfCalendar.getMonth(), pSpcfCalendar.getDay(), pSpcfCalendar.getHour(), pSpcfCalendar.getMinute(), 0, 0);
        return new Date(pSpcfCalendar.getTimeInMilliseconds());
    }

    public static SpcfCalendar getCalendarForDateAndTime(SpcfCalendar pDate, String pTime) {
        SpcfCalendar cutoffCalendar = SpcfCalendar.createInstance(SpcfTimeZone.getLocalTimeZone());

        String[] time = pTime.split(":");
        Integer hours = Integer.parseInt(time[0]);
        Integer minutes = Integer.parseInt(time[1]);
        Integer seconds = Integer.parseInt(time[2]);
        cutoffCalendar.setValues(pDate.getYear(), pDate.getMonth(), pDate.getDay(), hours,
                                 minutes, seconds, 0);

        return cutoffCalendar;
    }

    public static String toAuditDateFormat(SpcfCalendar pDate) {
        SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
        dateFormat.setPattern("dd-MMM-yy hh.mm.ss.S a");
        return dateFormat.format(pDate);

    }

    public static boolean isTimeClear(SpcfCalendar settlementDate) {
        return settlementDate.getMinute() == 0 &&
                settlementDate.getSecond() == 0 &&
                settlementDate.getMillisecond() == 0 &&
                (settlementDate.getHour() == 0 && settlementDate.isLocal() ||
                        settlementDate.getHour() >= 7 && settlementDate.getHour() <= 8 && settlementDate.isUTC());
    }

    // the time is cleared, timezone is reset to local timezone
    public static SpcfCalendar dayOfMonthBeforeOrEqualTo(SpcfCalendar pDate, int pDayOfMonth) {
        if (pDayOfMonth == pDate.getDay()) {
            SpcfTimeZone timeZone = SpcfTimeZone.getLocalTimeZone();
            SpcfCalendar date = SpcfCalendar.createInstance(pDate.getYear(), pDate.getMonth(), pDate.getDay(), timeZone);
            return date;
        } else {
            return dayOfMonthBefore(pDate, pDayOfMonth);
        }
    }

    // the time is cleared, timezone is reset to local timezone
    public static SpcfCalendar dayOfMonthBefore(SpcfCalendar pDate, int pDayOfMonth) {
        SpcfTimeZone timeZone = SpcfTimeZone.getLocalTimeZone();
        SpcfCalendar date = SpcfCalendar.createInstance(pDate.getYear(), pDate.getMonth(), pDate.getDay(), timeZone);

        if (pDayOfMonth >= date.getDay()) {
            date.addMonths(-1);
        }
        if (pDayOfMonth > date.getDaysInMonth()) {
            date.setValues(date.getYear(), date.getMonth(), date.getDaysInMonth());
        } else {
            date.setValues(date.getYear(), date.getMonth(), pDayOfMonth);
        }

        return date;
    }

    // the time is cleared, timezone is reset to local timezone
    public static SpcfCalendar endDateOfMonthlyPeriod(SpcfCalendar pStartDate) {
        SpcfTimeZone timeZone = SpcfTimeZone.getLocalTimeZone();
        SpcfCalendar date = SpcfCalendar.createInstance(pStartDate.getYear(), pStartDate.getMonth(), pStartDate.getDay(), timeZone);

        date.addMonths(1);
        date.addDays(-1);

        return date;
    }

    // the time is cleared, timezone is reset to local timezone
    public static SpcfCalendar dayOfMonthAfterOrEqualTo(SpcfCalendar pDate, int pDayOfMonth) {

        if (pDayOfMonth == pDate.getDay()) {
            SpcfTimeZone timeZone = SpcfTimeZone.getLocalTimeZone();
            SpcfCalendar date = SpcfCalendar.createInstance(pDate.getYear(), pDate.getMonth(), pDate.getDay(), timeZone);
            return date;
        } else {
            return dayOfMonthAfter(pDate, pDayOfMonth);
        }

    }

    // the time is cleared, timezone is reset to local timezone
    public static SpcfCalendar dayOfMonthAfter(SpcfCalendar pDate, int pDayOfMonth) {
        SpcfTimeZone timeZone = SpcfTimeZone.getLocalTimeZone();
        SpcfCalendar date = SpcfCalendar.createInstance(pDate.getYear(), pDate.getMonth(), pDate.getDay(), timeZone);

        if (pDayOfMonth <= date.getDay() || isLastDayOfMonth(pDate)) {
            date.addMonths(1);
        }
        if (pDayOfMonth > date.getDaysInMonth()) {
            date.setValues(date.getYear(), date.getMonth(), date.getDaysInMonth());
        } else {
            date.setValues(date.getYear(), date.getMonth(), pDayOfMonth);
        }

        return date;
    }

    // some Intuit web services doesn't like the Milli part of XML calendar
    // Spcf only supports UTC and local timezone
    public static String convertCalendarToXmlStringNoMilliSeconds(SpcfCalendar pCalendar) {
        StringBuffer timezoneStr = new StringBuffer();
        if (pCalendar.isUTC()) {
            timezoneStr.append("Z");
        } else {
            GregorianCalendar aCalendar = new GregorianCalendar(pCalendar.getYear(), pCalendar.getMonth()-1, pCalendar.getDay(),
                                                                pCalendar.getHour(), pCalendar.getMinute(), pCalendar.getSecond());
            long millis = aCalendar.getTimeZone().getOffset(1, aCalendar.get(Calendar.YEAR), aCalendar.get(Calendar.MONTH), aCalendar.get(Calendar.DAY_OF_MONTH), aCalendar.get(Calendar.DAY_OF_WEEK), aCalendar.get(Calendar.MILLISECOND));
            if (millis > 0) {
                timezoneStr.append("+");
            } else {
                timezoneStr.append("-");
                millis = -millis;
            }
            long hours = TimeUnit.MILLISECONDS.toHours(millis);

            timezoneStr.append(String.format("%02d:%02d", hours, TimeUnit.MILLISECONDS.toMinutes(millis - TimeUnit.HOURS.toMillis(hours))));
        }

        return pCalendar.format("yyyy-MM-dd'T'HH:mm:ss") + timezoneStr;
    }

    public static Date convertLocalTimestamp(long millis) {
        TimeZone tz = TimeZone.getDefault();
        Calendar c = Calendar.getInstance(tz);
        long localMillis = millis;
        int offset, time;

        c.set(1970, Calendar.JANUARY, 1, 0, 0, 0);

        // Add milliseconds
        while (localMillis > Integer.MAX_VALUE)
        {
            c.add(Calendar.MILLISECOND, Integer.MAX_VALUE);
            localMillis -= Integer.MAX_VALUE;
        }
        c.add(Calendar.MILLISECOND, (int)localMillis);

        // Stupidly, the Calendar will give us the wrong result if we use getTime() directly.
        // Instead, we calculate the offset and do the math ourselves.
        time = c.get(Calendar.MILLISECOND);
        time += c.get(Calendar.SECOND) * 1000;
        time += c.get(Calendar.MINUTE) * 60 * 1000;
        time += c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        offset = tz.getOffset(c.get(Calendar.ERA), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_WEEK), time);

        return new Date(millis - offset);
    }

    public static int getWeekOfYear(SpcfCalendar pCalendar) {

        GregorianCalendar calendar = new GregorianCalendar(pCalendar.getYear(), pCalendar.getMonth() - 1, pCalendar.getDay());

        return calendar.get(Calendar.WEEK_OF_YEAR);
    }


    /**
     * <p>This Compares only date i.e day,month and year</p>
     * <p>Compares two SpcfCalendar, returning <code>true</code> if they are equal </p>
     *
     * <p><code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered equal.</p>
     *
     * <pre>
     * CalendarUtils.compareSpcfCalendar(null, null)   = true
     * CalendarUtils.compareSpcfCalendar(null, spcfCalendar)  = false
     * CalendarUtils.compareSpcfCalendar(spcfCalendar, null)  = false
     * CalendarUtils.compareSpcfCalendar(spcfCalendar, spcfCalendar) = true
     * CalendarUtils.compareSpcfCalendar(spcfCalendar1, spcfCalendar2) = false
     * </pre>
     *
     * @param pSpcfCalendar1  the first SpcfCalendar, may be null
     * @param pSpcfCalendar2  the second SpcfCalendar, may be null
     * @return <code>true</code> if the SpcfCalendars are equal, or
     *  both <code>null</code>
     *
     */



    public static boolean compareSpcfCalendarDate(SpcfCalendar pSpcfCalendar1, SpcfCalendar pSpcfCalendar2){

        return pSpcfCalendar1 == null ? pSpcfCalendar2 == null : ( pSpcfCalendar1.getDay() == pSpcfCalendar2.getDay() &&
                                                                   pSpcfCalendar1.getMonth() == pSpcfCalendar2.getMonth() &&
                                                                    pSpcfCalendar1.getYear() == pSpcfCalendar2.getYear());
    }

    public static SpcfCalendar getFirstBusinessDayOfMonth(SpcfCalendar pDate) {
        SpcfCalendar firstDayOfMonth = getFirstDayOfMonth(pDate);
        if(isWeekendOrHoliday(firstDayOfMonth)){
            addBusinessDays(firstDayOfMonth, 1);
        }
        return firstDayOfMonth;
    }
}
