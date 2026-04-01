/**
 * Helper.java
 *
 * Copyright (c) 1999-2000 PayCycle, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * PayCycle, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with PayCycle.
 *
 * PAYCYCLE MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. PAYCYCLE SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * CopyrightVersion 1.0
 */
package com.paycycle.util;


//import com.paycycle.biz.Address;
//import com.paycycle.biz.Partner;
//import com.paycycle.data.CompanyTaxInfo;
//import com.paycycle.data.TaxPaymentInfo;

import com.paycycle.model.DateModel;
import com.paycycle.model.ModelHelper;
import com.paycycle.user.UserException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;


//import java.util.TimeZone;

/**
 * Collection of date related utility functions
 */
public class DateUtil {
    //	// set some constants that allow pretty accurate estimates over
    //	//  about 2,000 years before or after the present.  These are
    //	//  slight overestimates since that's what we want (and need!)
    //	public static final double DAY_MILLIS = 1000*60*60 * 24.0015;
    //	public static final double WEEK_MILLIS = DAY_MILLIS * 7;
    //	public static final double MONTH_MILLIS = DAY_MILLIS * 30.43675;
    //	public static final double YEAR_MILLIS = WEEK_MILLIS * 52.2;
    //
    //
    //	private static Map businessDayMap =
    //			new HashMap(AppMgr.getBusinessDayMapSize());
    //	private static Map prevBusinessDayMap =
    //			new HashMap(AppMgr.getBusinessDayMapSize());
    //
    //	public static Map dayOfWeekDifferenceLookupTable = null;
    //

    /**
     * Parse a date string.  This method is the default behavior
     */
    public static Date parseDate(String d) throws ParseException {
        DateFormat df = new SimpleDateFormat("MM/dd/yy");
        d = convertToDefaultDateStringFormat(d);

        return parseDate(d, df);
    }

    /**
     * Parse a date string.  This method is the specialized behavior
     * You are required to preprocess the date string before calling this method so that it conforms to your specialized date format
     */
    public static Date parseDate(String d, DateFormat df) throws ParseException {
        // Do not be lenient
        df.setLenient(false);

        Date parsed = df.parse(d);

        // Make sure year is within DB limit
        int year = getYear(parsed);

        if ((year < 1900) || (year > 2079)) {
            throw new ParseException("Year in " + d + " should be between 1900 and 2079.", 0);
        }

        return parsed;
    }

    //
    //	public static Date parseDate(String date, Date def) {
    //		if( date!=null ) {
    //			try {
    //				return parseDate(date);
    //			} catch (ParseException e) {
    //
    //			}
    //		}
    //		return def;
    //	}
    //

    /**
     * Convert all formats to mm/dd/yy??
     * Check for 2 or 4 digits year (mm/dd/yy or mm/dd/yyyy)
     */
    private static String convertToDefaultDateStringFormat(String d) throws ParseException {
        StringTokenizer date = new StringTokenizer(d, "/-. ");
        String month = date.hasMoreTokens() ? date.nextToken() : "";
        String day = date.hasMoreTokens() ? date.nextToken() : "";
        String year = date.hasMoreTokens() ? date.nextToken() : "";

        if ((year.length() != 2) && (year.length() != 4)) {
            throw new ParseException("Can't understand date format " + d + ". Please use mm/dd/yyyy.", 0);
        }

        return month + "/" + day + "/" + year;
    }

    //
    //	/**
    //	 * Check for quarter start date
    //	 */
    //	public static Date checkQuarterStartDate(String d) throws ParseException
    //	{
    //		// must be a date:
    //		Date date = parseDate(d);
    //		return checkQuarterStartDate (date);
    //	}
    //
    //	/**
    //	 * Check for quarter start date
    //	 */
    //	public static Date checkQuarterStartDate(Date date) throws ParseException
    //	{
    //		Calendar cal=new GregorianCalendar();
    //		cal.setTime(date);
    //
    //		int day = cal.get(Calendar.DAY_OF_MONTH);
    //		int month = cal.get(Calendar.MONTH);
    //
    //		// Day must be 1 and month (which is 0 based) must be 0, 3, 6 or 9.
    //		if (!(day == 1 && (month == 0 || month == 3 || month == 6 || month == 9)))
    //			throw new ParseException(date + " is not a quarter date. ", 0);
    //
    //		return date;
    //	}
    //

    /**
     * Format a date according to the passed format string
     *
     * @return the formatted date string
     */
    public static String dateFormat(Object theDate, String format) {
        if ((theDate == null) || (theDate instanceof String && (((String) theDate).length() <= 0))) {
            return "";
        }

        SimpleDateFormat formatter = new SimpleDateFormat(format);

        if (theDate instanceof DateModel) {
            return formatter.format(ModelHelper.toDate((DateModel) theDate));
        }

        return formatter.format(theDate);
    }

    //
    //	/**
    //	 * Format date based on whether the time factor is present in the date. If the time factor is present then only hour and minutes will be
    //	 * added to the date part. 'Pacific will be appended at the end of date time. The format will be \
    //	 * 'MM/dd/yyyy hh:mm AM/PM Pacific' or just 'MM/dd/yyyy'.
    //	 */
    //	public static String dateTimeFormat(Object d)
    //	{
    //		if(d == null || d instanceof String && ((String)d).length() < 1)
    //			return "";
    //
    //		String formatStr = null;
    //		Calendar cal = new GregorianCalendar();
    //		cal.setTime((Date)d);
    //
    //		if(cal.get(Calendar.HOUR_OF_DAY) > 0)
    //			formatStr  = "MM/dd/yyyy h:mm aa 'Pacific'";
    //		else
    //			formatStr  = "MM/dd/yyyy";
    //
    //		SimpleDateFormat formatter = new SimpleDateFormat(formatStr);
    //
    //		return formatter.format((Date)d);
    //	}
    //
    //
    //	/**
    //	 * Format date based in ISO format 'yyyy-MM-ddThh:mm:ssZ'
    //	 */
    //	public static String dateTimeISOFormat(Object d)
    //	{
    //		return dateFormat((Date)d, "yyyy-MM-dd'T'hh:mm:ss'Z'");
    //	}
    //
    //
    //	/**
    //	 * Trim of the hours/minutes/seconds from a date.
    //	 * @return a date with time parts cleared.
    //	 */
    //	public static Date dateTruncate(Date d)
    //	{
    //		if (d == null) return null;
    //		Calendar cal=new GregorianCalendar();
    //		cal.setTime(d);
    //		Calendar cal2 = new GregorianCalendar(cal.get(Calendar.YEAR),
    //											  cal.get(Calendar.MONTH),
    //											  cal.get(Calendar.DAY_OF_MONTH));
    //		return cal2.getTime();
    //	}
    //
    //	/**
    //	 * Today's date, with a time of 00:00:00.  This can be useful for date only arithmatic.
    //	 */
    //	public static Date today()
    //	{
    //		return dateTruncate(now());
    //	}
    //

    public static Date now() {
        return new Date();
    }

    /**
     * Calendar day arithmatic
     */
    public static Date addDay(Date src, int numDays) {
        // DO NOT TRY TO SIMPLIFY THIS by returning src if numDays == 0, this causes bugs with date comparisons in A LOT OF PLACES
        // That simplifying caused at least 3+ bugs because date comparison stopped working.  We want to convert the date using
        // GregorianCalendar first and return it.
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(src);
        cal.add(Calendar.DATE, numDays);

        return cal.getTime();
    }

    //
    //	/**
    //	 * Business day arithmatic.  Similar to getBusinessDay in its purpose, but
    //	 * this version handles negative numbers.  Admitedly its lame that I didn't
    //	 * update the other function, but I'm in a hurry and don't understand it.
    //	 * -Eric
    //	 */
    //	private static Date subtractBusinessDay (Date src, int numDays)
    //	{
    //		numDays = -numDays;
    //		if ( (numDays>10 || numDays<-10) && !AppMgr.isProduction() ) {
    //			// This routine is badly written for large numbers.  If you really want to go more
    //			// 2 weeks, please upgrade the performance of this routine first. -Eric S.
    //			throw new UserException("unable to addBusinessDay for more than 2 weeks.");
    //		}
    //		while( numDays>0 ) {
    //			src = getBusinessDay(addDay(src,1));
    //			numDays = numDays-1;
    //		}
    //		while( numDays<0 ) {
    //			// This function actually handles both adding and subtracting
    //			src = addDay(src,-1);
    //			while(!isBusinessDay(src)) src = addDay(src,-1);
    //			numDays = numDays+1;
    //		}
    //		return src;
    //	}
    //
    //	public static int getEstDiff( int calUnit, Date d1, Date d2 ) {
    //    	long diff = d2.getTime() - d1.getTime();
    //      	switch (calUnit) {
    //      		case Calendar.DAY_OF_WEEK_IN_MONTH :
    //      		case Calendar.DAY_OF_MONTH :
    //         		return (int) (diff / DAY_MILLIS + .5);
    //      		case Calendar.WEEK_OF_YEAR :
    //         		return (int) (diff / WEEK_MILLIS + .5);
    //      		case Calendar.MONTH :
    //         		return (int) (diff / MONTH_MILLIS + .5);
    //      		case Calendar.YEAR :
    //         		return (int) (diff / YEAR_MILLIS + .5);
    //      		default:
    //         		return 0;
    //      	} /* endswitch */
    //   	}
    //
    //	public static int getEstDiffRoundDown( int calUnit, Date d1, Date d2 ) {
    //    	long diff = d2.getTime() - d1.getTime();
    //      	switch (calUnit) {
    //      		case Calendar.DAY_OF_WEEK_IN_MONTH :
    //      		case Calendar.DAY_OF_MONTH :
    //         		return (int) (diff / DAY_MILLIS);
    //      		case Calendar.WEEK_OF_YEAR :
    //         		return (int) (diff / WEEK_MILLIS);
    //      		case Calendar.MONTH :
    //         		return (int) (diff / MONTH_MILLIS);
    //      		case Calendar.YEAR :
    //         		return (int) (diff / YEAR_MILLIS);
    //      		default:
    //         		return 0;
    //      	} /* endswitch */
    //   	}
    //
    //	public static int getAbsoluteDiff(int calUnit, Date d1, Date d2){
    //		GregorianCalendar cal1 = new GregorianCalendar();
    //		cal1.setTime(d1);
    //		GregorianCalendar cal2 = new GregorianCalendar();
    //		cal2.setTime(d2);
    //		int yearDiff = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
    //		int monthDiff = cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
    //		int dayDiff = cal2.get(Calendar.DAY_OF_MONTH) - cal1.get(Calendar.DAY_OF_MONTH);
    //		return 0;
    //	}
    //
    //
    //   	/**
    //	 * Returns the date difference in number of days between two dates
    //	 * ie (d2-d1).
    //	 */
    //	public static int dateDiff (Date d1, Date d2 )
    //	{
    //		return dateDiff( Calendar.DAY_OF_YEAR,d1,d2);
    //	}
    //
    //	/**
    //	 * Determines if the given search is lies between the given start and end dates (boundaries included)
    //	 * @param searchDate
    //	 * @param startDate
    //	 * @param endDate
    //	 * @return
    //	 */
    //	public static boolean isBetween(Date searchDate, Date startDate, Date endDate){
    //		if(!searchDate.before(startDate) && !endDate.before(searchDate))
    //			return true;
    //		return false;
    //	}
    //
    //   	/**
    //	 * Returns the date difference based on the Calendar unit (ie. Calendar.DAY_OF_YEAR)
    //	 * Returns the value of d2 - d1 in calUnits
    //	 */
    //	public static int dateDiff (int calUnit, Date d1, Date d2 )
    //	{
    //    	// swap if d1 later than d2
    //      	boolean neg = false;
    //      	if( d1.after(d2) ) {
    //         	Date temp = d1;
    //         	d1 = d2;
    //         	d2 = temp;
    //         	neg = true;
    //      	}
    //
    //      	// estimate the diff.  d1 is now guaranteed <= d2
    //      	int estimate = getEstDiff( calUnit, d1, d2 );
    //
    //      	// convert the Dates to GregorianCalendars
    //      	GregorianCalendar c1 = new GregorianCalendar();
    //      	c1.setTime(d1);
    //      	GregorianCalendar c2 = new GregorianCalendar();
    //      	c2.setTime(d2);
    //
    //      	// add 2 units less than the estimate to 1st date,
    //      	//  then serially add units till we exceed 2nd date
    //      	c1.add( calUnit, estimate - 2 );
    //      	for( int i=estimate-1; ; i++ ) {
    //         	c1.add( calUnit, 1 );
    //         	if( c1.after(c2) )
    //            	return neg ? 1-i : i-1;
    //      	}
    //	}
    //
    //	/**
    //	 * Test if the given date is a business day.
    //	 */
    //	public static boolean isBusinessDay (Date src)
    //	{
    //		// Business days are weekdays that are non-bank holidays
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime(src);
    //		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
    //		return dayOfWeek != Calendar.SATURDAY
    //					&& dayOfWeek != Calendar.SUNDAY
    //					&& !isBankingHoliday(src);
    //	}
    //
    //	/**
    //	 * Test if the given date is a partner business day.
    //	 */
    //	public static boolean isPartnerBusinessDay(Date src)
    //	{
    //		return isPartnerBusinessDay(src, Partner.PAYCYCLE);
    //	}
    //
    //	/**
    //	 * Test if the given date is a partner business day.
    //	 */
    //	public static boolean isPartnerBusinessDay(Date src, long partnerID)
    //	{
    //		//Partner Business days are weekdays that are non-partner holidays
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime(src);
    //		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
    //		return dayOfWeek != Calendar.SATURDAY
    //					&& dayOfWeek != Calendar.SUNDAY
    //					&& !isPartnerHoliday(src, partnerID);
    //	}
    //
    //	/**
    //	 * Returns the given date if it is a business day,
    //	 * otherwise the next business day is returned
    //	 */
    //	public static Date getBusinessDay (Date src)
    //	{
    //		return getBusinessDay(src, 0);
    //	}
    //
    //	/**
    //	 * Returns a number of business day after the given day.
    //	 * @param src reference date
    //	 * @param count number of business days to advance
    //	 * @return the result
    //	 */
    //	public static Date getBusinessDay (Date src, int count)
    //	{
    //		if( count<0 ) {
    //			return subtractBusinessDay(src,-count);
    //		}
    //		BusinessDateKey bdk = new BusinessDateKey(src, count);
    //
    //		Date d = null;
    //
    //		d = (Date)businessDayMap.get(bdk);
    //
    //		if(d == null) {
    //			d = (Date) Helper.executeQuery(QueryID.NEXTBUSINESSDAY,
    //							new Object[] {src, new Integer(count)});
    //
    //			//AppMgr.getLogger().info("Getting BusinessDay from database: " +
    //			//				businessDayMap.size() + ", " + src + "," + count);
    //		} else {
    //			//AppMgr.getLogger().info("Found BusinessDay in Map: " +
    //			//				businessDayMap.size() + ", " + src + "," + count);
    //		}
    //
    //		addBusinessDayToMap(bdk, d);
    //		Calendar cal1 = Calendar.getInstance();
    //		Calendar cal2 = Calendar.getInstance();
    //
    //		cal1.setTimeInMillis(d.getTime());
    //
    //		cal2.setTime(src);
    //
    //		cal1.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
    //		cal1.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
    //		cal1.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
    //		cal1.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
    //
    //		return cal1.getTime();
    //	}
    //
    //
    //	private static synchronized void addBusinessDayToMap(
    //					BusinessDateKey bdk, Date d) {
    //		long ts1 = System.currentTimeMillis();
    //
    //		int maxSize = AppMgr.getBusinessDayMapSize();
    //		if(businessDayMap.size() == maxSize) {
    //			int sizeToTakeOut = AppMgr.getBusinessDayRemoveSize();
    //
    //			List lst = new ArrayList(businessDayMap.keySet());
    //
    //			Collections.sort(
    //				lst, new BusinessDateKey.BusinessDateKeyComparator());
    //
    //			for(int i = 0; i < sizeToTakeOut; i++) {
    //				Object obj = lst.get(i);
    //
    //				businessDayMap.remove(obj);
    //			}
    //		}
    //
    //		bdk.setAccessTimeStamp((new Date()).getTime());
    //
    //		businessDayMap.remove(bdk);
    //
    //		businessDayMap.put(bdk, d);
    //
    //		long ts2 = System.currentTimeMillis();
    //
    //	}
    //
    //
    //	/**
    //	 * returns the given date if it is a business day,
    //	 * otherwise the previous business day is returned
    //	 */
    //	public static Date getPrevBusinessDay (Date src)
    //	{
    //		return getPrevBusinessDay(src, 0);
    //	}
    //
    //	/**
    //	 * Returns a number of business day before the given day.
    //	 * @param src reference date
    //	 * @param count number of business days to move backward
    //	 * @return the result
    //	 */
    //	public static Date getPrevBusinessDay (Date src, int count)
    //	{
    //		Date d = (Date) Helper.executeQuery(QueryID.PREVBUSINESSDAY, new Object[] {src, new Integer(count)});
    //		return new Date(d.getTime());
    //	}
    //
    //
    //	/**
    //	 *
    //	 * For 2005 R1.1, implement a cached version of prev business day to
    //	 * reduce the amount of testing
    //	 *
    //	 * returns the given date if it is a business day,
    //	 * otherwise the previous business day is returned
    //	 */
    //	public static Date getCachedPrevBusinessDay (Date src)
    //	{
    //		return getCachedPrevBusinessDay(src, 0);
    //	}
    //
    //	/**
    //	 *
    //	 * For 2005 R1.1, implement a cached version of prev business day to
    //	 * reduce the amount of testing
    //	 *
    //	 * Returns a number of business day before the given day.
    //	 * @param src reference date
    //	 * @param count number of business days to move backward
    //	 * @return the result
    //	 */
    //	public static Date getCachedPrevBusinessDay (Date src, int count)
    //	{
    //		BusinessDateKey bdk = new BusinessDateKey(src, count);
    //
    //		Date d = null;
    //
    //		d = (Date)prevBusinessDayMap.get(bdk);
    //
    //		if(d == null) {
    //			d = (Date) Helper.executeQuery(QueryID.PREVBUSINESSDAY,
    //							new Object[] {src, new Integer(count)});
    //
    //			//AppMgr.getLogger().info("Getting prevBusinessDay from database: " +
    //			//				prevBusinessDayMap.size() + ", " + src + "," + count);
    //		} else {
    //		    	//AppMgr.getLogger().info("Found prevBusinessDay in Map: " +
    //		    	//				prevBusinessDayMap.size() + ", " + src + "," + count);
    //		}
    //
    //		addPrevBusinessDayToMap(bdk, d);
    //
    //		Calendar cal1 = Calendar.getInstance();
    //		Calendar cal2 = Calendar.getInstance();
    //
    //		cal1.setTimeInMillis(d.getTime());
    //
    //		cal2.setTime(src);
    //
    //		cal1.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
    //		cal1.set(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
    //		cal1.set(Calendar.SECOND, cal2.get(Calendar.SECOND));
    //		cal1.set(Calendar.MILLISECOND, cal2.get(Calendar.MILLISECOND));
    //
    //		return cal1.getTime();
    //	}
    //
    //
    //	private static synchronized void addPrevBusinessDayToMap(
    //					BusinessDateKey bdk, Date d) {
    //	    	//long ts1 = System.currentTimeMillis();
    //
    //		int maxSize = AppMgr.getBusinessDayMapSize();
    //		if(prevBusinessDayMap.size() == maxSize) {
    //			int sizeToTakeOut = AppMgr.getBusinessDayRemoveSize();
    //
    //			List lst = new ArrayList(prevBusinessDayMap.keySet());
    //
    //			Collections.sort(
    //				lst, new BusinessDateKey.BusinessDateKeyComparator());
    //
    //			for(int i = 0; i < sizeToTakeOut; i++) {
    //				Object obj = lst.get(i);
    //
    //				prevBusinessDayMap.remove(obj);
    //			}
    //		}
    //
    //		bdk.setAccessTimeStamp(System.currentTimeMillis());
    //
    //		prevBusinessDayMap.remove(bdk);
    //
    //		prevBusinessDayMap.put(bdk, d);
    //
    //		//long ts2 = System.currentTimeMillis();
    //
    //		//if(AppMgr.getLogger().isLoggable(Level.FINE)) {
    //		//	AppMgr.getLogger().fine("Time taken to add prevBusiness day date: "  +
    //		//				(ts2 - ts1));
    //		//}
    //
    //	}
    //
    //	/**
    //	 * Returns the given date if it is a business day,
    //	 * otherwise the next business day is returned
    //	 */
    //	public static Date calcBusinessDay (Date src)
    //	{
    //		if (isBusinessDay(src))
    //			return src;
    //		else
    //		{
    //			Date nextDay = addDay(src, 1);
    //			return calcBusinessDay(nextDay);
    //		}
    //	}
    //
    //	/**
    //	 * Returns a number of business day after the given day.
    //	 * @param src reference date
    //	 * @param count number of business days to advance
    //	 * @return the result
    //	 */
    //	public static Date calcBusinessDay (Date src, int count)
    //	{
    //		Date result = src;
    //		if (count <= 0)
    //			return calcBusinessDay(src);
    //
    //		for (int i = 0; i < count; i++)
    //			result = calcBusinessDay(DateUtil.addDay(result, 1));
    //
    //		return result;
    //	}
    //
    //	/**
    //	 * returns the given date if it is a business day,
    //	 * otherwise the previous business day is returned
    //	 */
    //	public static Date calcPrevBusinessDay (Date src)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime(src);
    //
    //		/* if the src date is not a bank holiday, or
    //		   on the weekend, return it; otherwise, add
    //		   a day and check if that's a business day
    //		 */
    //		if (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
    //			cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
    //			!isBankingHoliday(src))
    //		{
    //			return src;
    //		} else
    //		{
    //			Date prevDay = addDay(src, -1);
    //			return calcPrevBusinessDay(prevDay);
    //		}
    //	}
    //
    //	/**
    //	 * Returns a number of business day before the given day.
    //	 * @param src reference date
    //	 * @param count number of business days to move backward
    //	 * @return the result
    //	 */
    //	public static Date calcPrevBusinessDay (Date src, int count)
    //	{
    //		Date result = src;
    //		if (count <= 0)
    //			return calcPrevBusinessDay(src);
    //
    //		for (int i = 0; i < count; i++)
    //			result = calcPrevBusinessDay(DateUtil.addDay(result, -1));
    //
    //		return result;
    //	}
    //
    //    /**
    //	 * Calculates the number of hours for the given start and end date.
    //	 * @param startDate The start date and time
    //	 * @param endDate The end date and time
    //	 * @return Returns the number of hours between the given times.
    //	 */
    //	public static double calculateHour(Date startDate, Date endDate) {
    //
    //        if(Helper.isEmpty(startDate) || Helper.isEmpty(endDate)) {
    //            return 0.0;
    //        }
    //		double diff = endDate.getTime() - startDate.getTime();
    //        // if the difference is 0, then assume a 24 hour shift
    //        if(diff == 0) {
    //            return 24;
    //        }
    //		long diffHours = 3600000;
    //        double calcHour = diff/diffHours;
    //
    //		BigDecimal roundedHours = (new BigDecimal(calcHour)).setScale(4, BigDecimal.ROUND_HALF_UP);
    //        return roundedHours.doubleValue();
    //	}
    //
    //	/**
    //	 * Initialize (load) all bank holidays
    //	 */
    //	public static void loadBankHolidays()
    //	{
    //		Helper.readAllObjects(com.paycycle.biz.BankHoliday.class, (Expression)null);
    //	}
    //
    //	/**
    //	 * Determines if the given date is a bank holiday.  We assume the BankHolidays cache has
    //	 * been fully initialized (loaded) - so that no DB hits will occur.
    //	 */
    //	public static boolean isBankingHoliday (Date d)
    //	{
    //		Expression exp = new ExpressionBuilder().get("m_date").equal(dateFormat(d, "yyyy-MM-dd"));
    //		ReadObjectQuery qry = new ReadObjectQuery();
    //		qry.setReferenceClass(com.paycycle.biz.BankHoliday.class);
    //		qry.setSelectionCriteria(exp);
    //		qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheOnly);
    //		return Helper.executeQuery(qry) != null;
    //	}
    //
    //	/**
    //	 * Determines if the given date is a partner holiday.  We assume the PartnerHolidays cache has
    //	 * been fully initialized (loaded) - so that no DB hits will occur.
    //	 */
    //	public static boolean isPartnerHoliday(Date d)
    //	{
    //		return isPartnerHoliday(d, Partner.PAYCYCLE);
    //	}
    //
    //	/**
    //	 * Determines if the given date is a partner holiday.  We assume the PartnerHolidays cache has
    //	 * been fully initialized (loaded) - so that no DB hits will occur.
    //	 */
    //	public static boolean isPartnerHoliday(Date d, long partnerID)
    //	{
    //		//Verify if we have holiday schedule for the partner,
    //		//if not default to use PayCycle holiday schedule
    //		if (partnerID != Partner.PAYCYCLE) {
    //			Expression exp = new ExpressionBuilder().get("m_partnerID").equal(partnerID);
    //			ReadObjectQuery qry = new ReadObjectQuery();
    //			qry.setReferenceClass(com.paycycle.biz.PartnerHoliday.class);
    //			qry.setSelectionCriteria(exp);
    //			qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheOnly);
    //			partnerID = (Helper.executeQuery(qry) != null) ? partnerID : Partner.PAYCYCLE;
    //		}
    //
    //		Expression exp1 = new ExpressionBuilder().get("m_date").equal(dateFormat(d, "yyyy-MM-dd"));
    //		Expression exp2 = new ExpressionBuilder().get("m_partnerID").equal(partnerID);
    //		ReadObjectQuery qry = new ReadObjectQuery();
    //		qry.setReferenceClass(com.paycycle.biz.PartnerHoliday.class);
    //		qry.setSelectionCriteria(exp1.and(exp2));
    //		qry.setCacheUsage(ObjectLevelReadQuery.CheckCacheOnly);
    //		return Helper.executeQuery(qry) != null;
    //	}
    //
    //	public static Date getDateFromDayAndMonthInQuarter(int dayOfMonthOrdinal, int monthOfQuarterOrdinal, Date anyDateInQuarter)
    //	{
    //		return createDate(getYear(anyDateInQuarter), getMonth(getFirstDayOfQuarter(anyDateInQuarter)) + (monthOfQuarterOrdinal - 1), dayOfMonthOrdinal);
    //	}
    //

    /**
     * Given the specified date, return the date part.
     */
    public static int getDatePart(Date d, int part) {
        if (d == null) {
            return -1;
        }

        Calendar cal = new GregorianCalendar();
        cal.setTime(d);

        return cal.get(part);
    }

    //
    //	/**
    //	 * Given the specified date, return the quarter the date is in.
    //	 */
    //	public static int getQuarter (Date d)
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		cal.setTime(d);
    //		switch (cal.get(Calendar.MONTH))
    //		{
    //			case Calendar.JANUARY:
    //			case Calendar.FEBRUARY:
    //			case Calendar.MARCH:
    //				return 1;
    //
    //			case Calendar.APRIL:
    //			case Calendar.MAY:
    //			case Calendar.JUNE:
    //				return 2;
    //
    //			case Calendar.JULY:
    //			case Calendar.AUGUST:
    //			case Calendar.SEPTEMBER:
    //				return 3;
    //
    //			case Calendar.OCTOBER:
    //			case Calendar.NOVEMBER:
    //			case Calendar.DECEMBER:
    //				return 4;
    //		}
    //		throw new RuntimeException ("Unknown month in date");
    //	}
    //
    //	/**
    //	 * Given the specified date, return the which half the date lies in.
    //	 */
    //	public static int getHalf(Date d)
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		cal.setTime(d);
    //		switch (cal.get(Calendar.MONTH))
    //		{
    //			case Calendar.JANUARY:
    //			case Calendar.FEBRUARY:
    //			case Calendar.MARCH:
    //			case Calendar.APRIL:
    //			case Calendar.MAY:
    //			case Calendar.JUNE:
    //				return 1;
    //
    //			case Calendar.JULY:
    //			case Calendar.AUGUST:
    //			case Calendar.SEPTEMBER:
    //			case Calendar.OCTOBER:
    //			case Calendar.NOVEMBER:
    //			case Calendar.DECEMBER:
    //				return 2;
    //		}
    //		throw new RuntimeException ("Unknown month in date");
    //	}
    //
    //	/**
    //	 * Given the specified date, return the first day of the quarter.
    //	 */
    //	public static Date getFirstDayOfQuarter (Date d)
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		cal.setTime(d);
    //		switch (cal.get(Calendar.MONTH))
    //		{
    //			case Calendar.JANUARY:
    //			case Calendar.FEBRUARY:
    //			case Calendar.MARCH:
    //				return createDate(getYear(d), Calendar.JANUARY, 1);
    //
    //			case Calendar.APRIL:
    //			case Calendar.MAY:
    //			case Calendar.JUNE:
    //				return createDate(getYear(d), Calendar.APRIL, 1);
    //
    //			case Calendar.JULY:
    //			case Calendar.AUGUST:
    //			case Calendar.SEPTEMBER:
    //				return createDate(getYear(d), Calendar.JULY, 1);
    //
    //			case Calendar.OCTOBER:
    //			case Calendar.NOVEMBER:
    //			case Calendar.DECEMBER:
    //				return createDate(getYear(d), Calendar.OCTOBER, 1);
    //		}
    //		throw new RuntimeException ("Unknown month in date");
    //	}
    //
    //	/**
    //	 * Given the specified quarter number, return the first day of the quarter
    //	 *  in the given year.
    //	 */
    //	public static Date getFirstDayOfQuarter (int quarter, int year)
    //	{
    //		switch (quarter)
    //		{
    //			case 1:
    //				return createDate(year, Calendar.JANUARY, 1);
    //
    //			case 2:
    //				return createDate(year, Calendar.APRIL, 1);
    //
    //			case 3:
    //				return createDate(year, Calendar.JULY, 1);
    //
    //			case 4:
    //				return createDate(year, Calendar.OCTOBER, 1);
    //		}
    //		throw new RuntimeException ("Unknown quarter value");
    //	}

    /**
     * Given the specified date, return the last day of the quarter.
     */
    public static Date getLastDayOfQuarter (Date d)
    {
        Calendar cal = new GregorianCalendar();
        cal.setTime(d);
        switch (cal.get(Calendar.MONTH))
        {
            case Calendar.JANUARY:
            case Calendar.FEBRUARY:
            case Calendar.MARCH:
                return getLastDayOfMonth(getYear(d), Calendar.MARCH);

            case Calendar.APRIL:
            case Calendar.MAY:
            case Calendar.JUNE:
                return getLastDayOfMonth(getYear(d), Calendar.JUNE);

            case Calendar.JULY:
            case Calendar.AUGUST:
            case Calendar.SEPTEMBER:
                return getLastDayOfMonth(getYear(d), Calendar.SEPTEMBER);

            case Calendar.OCTOBER:
            case Calendar.NOVEMBER:
            case Calendar.DECEMBER:
                return getLastDayOfMonth(getYear(d), Calendar.DECEMBER);
        }
        throw new RuntimeException ("Unknown month in date");
    }

    //	/**
    //	 * Given the specified quarter number, return the last day of the quarter
    //	 *  in the given year.
    //	 */
    //	public static Date getLastDayOfQuarter (int quarter, int year)
    //	{
    //		switch (quarter)
    //		{
    //			case 1:
    //				return getLastDayOfMonth(year, Calendar.MARCH);
    //
    //			case 2:
    //				return getLastDayOfMonth(year, Calendar.JUNE);
    //
    //			case 3:
    //				return getLastDayOfMonth(year, Calendar.SEPTEMBER);
    //
    //			case 4:
    //				return getLastDayOfMonth(year, Calendar.DECEMBER);
    //		}
    //		throw new RuntimeException ("Unknown quarter value");
    //	}
    //
    //	/**
    //	 * Given a quarter, returns the last month of that quarter
    //	 */
    //	public static int getLastMonthOfQuarter(int quarter){
    //		switch(quarter){
    //			case 1:	return Calendar.MARCH;
    //			case 2: return Calendar.JUNE;
    //			case 3: return Calendar.SEPTEMBER;
    //			case 4: return Calendar.DECEMBER;
    //		}
    //
    //		return 0;
    //	}
    //
    //	/**
    //	 * Given a quarter, returns the first month of that quarter
    //	 */
    //	public static int getFirstMonthOfQuarter(int quarter){
    //		switch(quarter){
    //			case 1:	return Calendar.JANUARY;
    //			case 2: return Calendar.APRIL;
    //			case 3: return Calendar.JULY;
    //			case 4: return Calendar.OCTOBER;
    //		}
    //		return 0;
    //	}
    //
    //	/**
    //	 * Return the current filing quarter
    //	 */
    //	public static int getCurrentFilingQuarter()
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		int qtr = getQuarter(cal.getTime());
    //
    //		if (cal.get(Calendar.MONTH) == Calendar.JANUARY ||
    //			cal.get(Calendar.MONTH) == Calendar.APRIL ||
    //			cal.get(Calendar.MONTH) == Calendar.JULY ||
    //			cal.get(Calendar.MONTH) == Calendar.OCTOBER)
    //				--qtr;
    //		return qtr;
    //	}
    //
    //	/**
    //	 * Return the current filing quarter start date
    //	 */
    //	public static Date getCurrentFilingQuarterStart()
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		switch(getCurrentFilingQuarter())
    //		{
    //			case 0:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR) - 1, Calendar.OCTOBER, 1)).getTime();
    //			case 1:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.JANUARY, 1)).getTime();
    //			case 2:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.APRIL, 1)).getTime();
    //			case 3:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.JULY, 1)).getTime();
    //			case 4:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.OCTOBER, 1)).getTime();
    //		}
    //		throw new RuntimeException ("Unknown quarter");
    //	}
    //
    //	/**
    //	 * Return the current filing quarter end date
    //	 */
    //	public static Date getCurrentFilingQuarterEnd()
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		switch(getCurrentFilingQuarter())
    //		{
    //			case 0:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31)).getTime();
    //			case 1:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.MARCH, 31)).getTime();
    //			case 2:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.JUNE, 30)).getTime();
    //			case 3:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.SEPTEMBER, 30)).getTime();
    //			case 4:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.DECEMBER, 31)).getTime();
    //		}
    //		throw new RuntimeException ("Unknown quarter");
    //	}
    //
    //	/**
    //	 * Return the current filing quarter due date
    //	 */
    //	public static Date getCurrentFilingQuarterDue()
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		switch(getCurrentFilingQuarter())
    //		{
    //			case 0:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.JANUARY, 1)).getTime();
    //			case 1:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.APRIL, 1)).getTime();
    //			case 2:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.JULY, 1)).getTime();
    //			case 3:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.OCTOBER, 1)).getTime();
    //			case 4:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR) + 1, Calendar.JANUARY, 1)).getTime();
    //		}
    //		throw new RuntimeException ("Unknown quarter");
    //	}
    //
    //	/**
    //	 * Return the current filing quarter penalty date
    //	 */
    //	public static Date getCurrentFilingQuarterPenalty()
    //	{
    //		Calendar cal = new GregorianCalendar();
    //		switch(getCurrentFilingQuarter())
    //		{
    //			case 0:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.JANUARY, 31)).getTime();
    //			case 1:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.APRIL, 30)).getTime();
    //			case 2:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.JULY, 31)).getTime();
    //			case 3:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR), Calendar.OCTOBER, 31)).getTime();
    //			case 4:
    //				return (new GregorianCalendar(cal.get(Calendar.YEAR) + 1, Calendar.JANUARY, 31)).getTime();
    //		}
    //		throw new RuntimeException ("Unknown quarter");
    //	}
    //
    //	/**
    //	 * Return the list of periods for given period start and end date
    //	 * If splitDay = Calendar.SUNDAY then the period is splited on each Sunday.
    //	 */
    //	public static PeriodList splitPeriods (int splitDay, Date periodStart, Date periodEnd)
    //	{
    //		PeriodList pl = new PeriodList();
    //		pl.add(periodStart, periodEnd);
    //		for (Date tempDate = periodStart; tempDate.compareTo(periodEnd) <= 0; tempDate = addDay(tempDate, 1))
    //		{
    //			Calendar cal = new GregorianCalendar();
    //			cal.setTime(tempDate);
    //			if (cal.get(Calendar.DAY_OF_WEEK) == splitDay)
    //				pl.split(tempDate);
    //		}
    //		return pl;
    //	}
    //
    //	// Assumptions:
    //	//    periodStart is the first of a month
    //	//    periodEnd is the end of a month greater than or equal to the month of periodStart
    //	public static PeriodList splitPeriodIntoMonths(Date periodStart, Date periodEnd)
    //	{
    //		PeriodList months = new PeriodList();
    //
    //		Date monthStart = periodStart;
    //		Date monthEnd = DateUtil.getLastDayOfMonth(monthStart);
    //		months.add(new Period(monthStart, monthEnd));
    //
    //		while (monthEnd.compareTo(periodEnd) < 0)
    //		{
    //			monthStart = DateUtil.addDay(monthEnd, 1);
    //			monthEnd = DateUtil.getLastDayOfMonth(monthStart);
    //			months.add(new Period(monthStart, monthEnd));
    //		}
    //
    //		return months;
    //	}
    //
    //	/**
    //	 * Split the given period into quarters; remove the last period if it is not a full quarter
    //	 * @param	periodStart date
    //	 * @param	periodEnd date
    //	 * @return	PeriodList object
    //	 */
    //	public static PeriodList splitQuarters (Date periodStart, Date periodEnd)
    //	{
    //		PeriodList pl = new PeriodList();
    //		pl.add(periodStart, periodEnd);
    //		for (Date tempDate = periodStart; tempDate.compareTo(periodEnd) <= 0; tempDate = addDay(tempDate, 90))
    //		{
    //			tempDate = getLastDayOfQuarter(tempDate);
    //			if (tempDate.before(periodEnd))
    //				pl.split(addDay(tempDate, 1));
    //		}
    //
    //		// remove the last period if it is not a full quarter
    //		Period lastQuarter = pl.get(pl.size()-1);
    //		Date endDate = dateTruncate(lastQuarter.getEndDate());
    //		if (endDate.before(dateTruncate(getLastDayOfQuarter(endDate))))
    //			pl.remove(pl.size()-1);
    //
    //		// if startDate of 1st period is not 1st day of the quarter then set it
    //		if ((pl.size() > 0) && (pl.get(0).getStartDate().after(getFirstDayOfQuarter(pl.get(0).getStartDate()))))
    //		{
    //			Period firstQuarter = pl.get(0);
    //			firstQuarter.setStartDate(getFirstDayOfQuarter(firstQuarter.getStartDate()));
    //		}
    //
    //		return pl;
    //	}
    //
    //	/**
    //	 * Split the given period into quarters without modifying the period start and end dates; this will allow for incomplete quarters
    //	 * @param	periodStart date
    //	 * @param	periodEnd date
    //	 * @return	PeriodList object
    //	 */
    //	public static PeriodList splitQuartersWithoutModification (Date periodStart, Date periodEnd)
    //	{
    //		PeriodList pl = new PeriodList();
    //		pl.add(periodStart, periodEnd);
    //		for (Date tempDate = periodStart; tempDate.compareTo(periodEnd) <= 0; tempDate = addDay(tempDate, 90))
    //		{
    //			tempDate = getLastDayOfQuarter(tempDate);
    //			if (tempDate.before(periodEnd))
    //				pl.split(addDay(tempDate, 1));
    //		}
    //
    //		return pl;
    //	}
    //
    //	/**
    //	 * Split the given period into years; remove the last period if it is not a full year
    //	 * @param	periodStart date
    //	 * @param	periodEnd date
    //	 * @return	PeriodList object
    //	 */
    //	public static PeriodList splitYears (Date periodStart, Date periodEnd)
    //	{
    //		PeriodList pl = new PeriodList();
    //		pl.add(periodStart, periodEnd);
    //		for (Date tempDate = periodStart; tempDate.compareTo(periodEnd) <= 0; tempDate = addDay(tempDate, 365))
    //		{
    //			tempDate = getLastDayOfMonth(getDatePart(tempDate, Calendar.YEAR), Calendar.DECEMBER);
    //			if (tempDate.before(periodEnd))
    //				pl.split(addDay(tempDate, 1));
    //		}
    //
    //		// remove the last period if it is not a full year
    //		Period lastYear = pl.get(pl.size()-1);
    //		Date endDate = dateTruncate(lastYear.getEndDate());
    //		Date yearEndDate = getLastDayOfMonth(getDatePart(endDate, Calendar.YEAR), Calendar.DECEMBER);
    //		if (endDate.before(dateTruncate(yearEndDate)))
    //			pl.remove(pl.size()-1);
    //
    //		// if startDate of 1st period is not 1st day of the year then set it
    //		if ((pl.size() > 0) && (getDayOfYear(pl.get(0).getStartDate()) > 1))
    //		{
    //			Period firstYear = pl.get(0);
    //			firstYear.setStartDate(createDate(getYear(firstYear.getStartDate()), Calendar.JANUARY, 1));
    //		}
    //
    //		return pl;
    //	}
    //

    public static Date createDate(int year, int month, int day) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(year, month, day);

        return cal.getTime();
    }

    //
    //	public static Date createDateTime(int year, int month, int day, int hour, int minute, int second)
    //	{
    //		GregorianCalendar calendar = new GregorianCalendar();
    //		calendar.set(year,month,day,hour,minute,second);
    //		return calendar.getTime();
    //	}
    //

    public static Date createDate(String date) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

            return sdf.parse(date);
        } catch (ParseException exp) {
            throw new UserException("Date format must be mm/dd/yyyy.");
        }
    }

    //
    //	public static Date createDateTime(String date) {
    //		return createDate(date, "MM/dd/yyyy HH:mm:ss");
    //	}
    //
    //	public static Date createDate(String date, String formatString)
    //	{
    //		try {
    //			SimpleDateFormat sdf = new SimpleDateFormat(formatString);
    //			return sdf.parse(date);
    //		} catch (ParseException exp) {
    //			throw new UserException ("Date format must be " + formatString + ".");
    //		}
    //	}
    //
    //	public static int getDaysInMonth(Date d)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime(d);
    //		return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    //	}
    //
    //	public static Date addMonth(Date src, int numMonths)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime (src);
    //		cal.add (Calendar.MONTH, numMonths);
    //		return cal.getTime ();
    //	}
    //
    //	public static Date addYear(Date src, int years)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime (src);
    //		cal.add (Calendar.YEAR, years);
    //		return cal.getTime ();
    //	}
    //
    //	public static Date addHours(Date src, int numOfHours)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime (src);
    //		cal.add (Calendar.HOUR_OF_DAY, numOfHours);
    //		return cal.getTime ();
    //	}
    //
    //
    //	public static Date addTime(Date src, int numOfHours, int numOfMinutes, int numOfSeconds){
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.setTime (src);
    //		cal.add (Calendar.HOUR_OF_DAY, numOfHours);
    //		cal.add (Calendar.MINUTE, numOfMinutes);
    //		cal.add (Calendar.SECOND, numOfSeconds);
    //		return cal.getTime ();
    //	}
    //
    //
    //	public static int getMonth(Date d)
    //	{
    //		return getDatePart(d, Calendar.MONTH);
    //	}
    //
    //	public static int getMonth1Base(Date d)
    //	{
    //		return getDatePart(d, Calendar.MONTH)+1;
    //	}
    //
    //	public static String getMonthName(Date d)
    //	{
    //		int month = getMonth(d);
    //		switch (month) {
    //			case Calendar.JANUARY: return "January";
    //			case Calendar.FEBRUARY: return "February";
    //			case Calendar.MARCH: return "March";
    //			case Calendar.APRIL: return "April";
    //			case Calendar.MAY: return "May";
    //			case Calendar.JUNE: return "June";
    //			case Calendar.JULY: return "July";
    //			case Calendar.AUGUST: return "August";
    //			case Calendar.SEPTEMBER: return "September";
    //			case Calendar.OCTOBER: return "October";
    //			case Calendar.NOVEMBER: return "November";
    //			case Calendar.DECEMBER: return "December";
    //		}
    //		throw new RuntimeException ("Unknown month");
    //	}
    //
    //	public static String getMonth3(Date endDate)
    //	{
    //		return getMonth1Base(endDate) == 12 ? "12" : "0" + TypeUtil.toInteger(getMonth1Base(endDate)).toString();
    //	}
    //	public static String getMonth3Year(Date endDate)
    //	{
    //		String month3year = getMonth1Base(endDate) == 12 ? "12" : "0" + TypeUtil.toInteger(getMonth1Base(endDate)).toString();
    //		month3year += TypeUtil.toInteger(getYear(endDate)).toString();
    //		return month3year;
    //	}
    //
    //	public static String getDayOfWeekName(Date d) {
    //		return dateFormat(d,"E");
    //	}
    //

    public static int getYear(Date d) {
        return getDatePart(d, Calendar.YEAR);
    }

    //
    //	public static int getDay(Date d)
    //	{
    //		return getDatePart(d, Calendar.DATE);
    //	}
    //
    //	public static int getDayOfYear(Date d)
    //	{
    //		return getDatePart(d, Calendar.DAY_OF_YEAR);
    //	}

    /**
     * Return a new date which is the last day of the month
     * specified by the given year/month.
     */
    public static Date getLastDayOfMonth(int year, int month)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(year, month, 1);
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        return cal.getTime();
    }

    public static Date getLastDayOfMonth(Integer year, Integer month)
    {
        return getLastDayOfMonth(year.intValue(),month.intValue());
    }

    //	public static Date getFirstDayOfMonth(Integer year, Integer month)
    //	{
    //		return createDate(year.intValue(), month.intValue(), 1);
    //	}
    //
    //	public static Date getLastDayOfMonth(Date d)
    //	{
    //		return getLastDayOfMonth(getYear(d), getMonth(d));
    //	}

    public static Date getLastDayOfYear(Date d)
    {
        return getLastDayOfYear(getYear(d));
    }

    public static Date getLastDayOfYear(int year)
    {
        GregorianCalendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(year, 11, 31);
        return cal.getTime();
    }

    //	public static Date getFirstDayOfMonth(Date d)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.clear();
    //		cal.set(getYear(d), getMonth(d), 1);
    //		return cal.getTime();
    //	}
    //
    //	public static Date getFirstDayOfYear(Date d)
    //	{
    //		return getFirstDayOfYear(getYear(d));
    //	}
    //
    //	public static Date getFirstDayOfYear(int year)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.clear();
    //		cal.set(year, 0, 1);
    //		return cal.getTime();
    //	}
    //
    //	public static Date getNthDayOfMonth(Date date, int day)
    //	{
    //		GregorianCalendar cal = new GregorianCalendar();
    //		cal.clear();
    //		cal.set(getYear(date), getMonth(date), day);
    //		return cal.getTime();
    //	}
    //
    //	/**
    //	 * given a date, return a string that represents the quarter in
    //	 * which the date falls.
    //	 */
    //	public static String getQuarterText (Date d)
    //	{
    //		switch (getQuarter(d))
    //		{
    //		case 1:
    //			return ("1st quarter " + getYear(d));
    //		case 2:
    //			return ("2nd quarter " + getYear(d));
    //		case 3:
    //			return ("3rd quarter " + getYear(d));
    //		case 4:
    //			return ("4th quarter " + getYear(d));
    //		default:
    //			return "";
    //		}
    //	}
    //
    //	/**
    //	 * given a date, return a string that represents the quarter and the
    //	 * months within the quarter
    //	 */
    //	public static String getQuarterTextDetail (Date d)
    //	{
    //		switch (getQuarter(d))
    //		{
    //		case 1:
    //			return ("01 - January, February, March");
    //		case 2:
    //			return ("02 - April, May, June");
    //		case 3:
    //			return ("03 - July, August, September");
    //		case 4:
    //			return ("04 - October, November, December");
    //		default:
    //			return "";
    //		}
    //	}
    //
    //	/**
    //	 * given a date, return a string that represents the quarter and the
    //	 * months within the quarter
    //	 */
    //	public static String getQuarterTextDetailAbbreviated (Date d)
    //	{
    //		switch (getQuarter(d))
    //		{
    //		case 1:
    //			return ("01 - Jan, Feb, Mar");
    //		case 2:
    //			return ("02 - Apr, May, Jun");
    //		case 3:
    //			return ("03 - Jul, Aug, Sep");
    //		case 4:
    //			return ("04 - Oct, Nov, Dec");
    //		default:
    //			return "";
    //		}
    //	}
    //
    //	/**
    //	 * given a date, return a string that represents the quarter and year
    //	 * as YYQQ, e.g. 0301.
    //	 */
    //	public static String getQuarterAndYearText (Date d)
    //	{
    //		switch (getQuarter(d))
    //		{
    //		case 1:
    //			return (dateFormat(d, "yy") + "01");
    //		case 2:
    //			return (dateFormat(d, "yy") + "02");
    //		case 3:
    //			return (dateFormat(d, "yy") + "03");
    //		case 4:
    //			return (dateFormat(d, "yy") + "04");
    //		default:
    //			return "";
    //		}
    //	}
    //
    //
    //	/**
    //	 * given a date, return a string that represents the quarter and year
    //	 * The year will be in a given format representation
    //	 */
    //	public static String getQuarterAndYear (Date d, String format)
    //	{
    //		switch (getQuarter(d))
    //		{
    //		case 1:
    //			return ("1" + dateFormat(d, format));
    //		case 2:
    //			return ("2" + dateFormat(d, format));
    //		case 3:
    //			return ("3" + dateFormat(d, format));
    //		case 4:
    //			return ("4" + dateFormat(d, format));
    //		default:
    //			return "";
    //		}
    //	}
    //
    //	/**
    //	 * @author ed
    //	 * SUN - MON = -1
    //	 * The whole point of this method is not to assume the values of the Calendar day of week constants
    //	 * They probably won't change, but why take the chance
    //	 */
    //	public static Map getDayOfWeekDifferenceLookupTable() {
    //		if (dayOfWeekDifferenceLookupTable == null) {
    //			dayOfWeekDifferenceLookupTable = new HashMap(7,1.0f);
    //			Map sun = new HashMap(7,1.0f); dayOfWeekDifferenceLookupTable.put(new Integer(Calendar.SUNDAY), sun);
    //			Map mon = new HashMap(7,1.0f); dayOfWeekDifferenceLookupTable.put(new Integer(Calendar.MONDAY), mon);
    //			Map tue = new HashMap(7,1.0f); dayOfWeekDifferenceLookupTable.put(new Integer(Calendar.TUESDAY), tue);
    //			Map wed = new HashMap(7,1.0f); dayOfWeekDifferenceLookupTable.put(new Integer(Calendar.WEDNESDAY), wed);
    //			Map thu = new HashMap(7,1.0f); dayOfWeekDifferenceLookupTable.put(new Integer(Calendar.THURSDAY), thu);
    //			Map fri = new HashMap(7,1.0f); dayOfWeekDifferenceLookupTable.put(new Integer(Calendar.FRIDAY), fri);
    //			Map sat = new HashMap(7,1.0f); dayOfWeekDifferenceLookupTable.put(new Integer(Calendar.SATURDAY), sat);
    //			sun.put(new Integer(Calendar.SUNDAY), new Integer(0));
    //			sun.put(new Integer(Calendar.MONDAY), new Integer(-1));
    //			sun.put(new Integer(Calendar.TUESDAY), new Integer(-2));
    //			sun.put(new Integer(Calendar.WEDNESDAY), new Integer(-3));
    //			sun.put(new Integer(Calendar.THURSDAY), new Integer(-4));
    //			sun.put(new Integer(Calendar.FRIDAY), new Integer(-5));
    //			sun.put(new Integer(Calendar.SATURDAY), new Integer(-6));
    //			mon.put(new Integer(Calendar.SUNDAY), new Integer(1));
    //			mon.put(new Integer(Calendar.MONDAY), new Integer(0));
    //			mon.put(new Integer(Calendar.TUESDAY), new Integer(-1));
    //			mon.put(new Integer(Calendar.WEDNESDAY), new Integer(-2));
    //			mon.put(new Integer(Calendar.THURSDAY), new Integer(-3));
    //			mon.put(new Integer(Calendar.FRIDAY), new Integer(-4));
    //			mon.put(new Integer(Calendar.SATURDAY), new Integer(-5));
    //			tue.put(new Integer(Calendar.SUNDAY), new Integer(2));
    //			tue.put(new Integer(Calendar.MONDAY), new Integer(1));
    //			tue.put(new Integer(Calendar.TUESDAY), new Integer(0));
    //			tue.put(new Integer(Calendar.WEDNESDAY), new Integer(-1));
    //			tue.put(new Integer(Calendar.THURSDAY), new Integer(-2));
    //			tue.put(new Integer(Calendar.FRIDAY), new Integer(-3));
    //			tue.put(new Integer(Calendar.SATURDAY), new Integer(-4));
    //			wed.put(new Integer(Calendar.SUNDAY), new Integer(3));
    //			wed.put(new Integer(Calendar.MONDAY), new Integer(2));
    //			wed.put(new Integer(Calendar.TUESDAY), new Integer(1));
    //			wed.put(new Integer(Calendar.WEDNESDAY), new Integer(0));
    //			wed.put(new Integer(Calendar.THURSDAY), new Integer(-1));
    //			wed.put(new Integer(Calendar.FRIDAY), new Integer(-2));
    //			wed.put(new Integer(Calendar.SATURDAY), new Integer(-3));
    //			thu.put(new Integer(Calendar.SUNDAY), new Integer(4));
    //			thu.put(new Integer(Calendar.MONDAY), new Integer(3));
    //			thu.put(new Integer(Calendar.TUESDAY), new Integer(2));
    //			thu.put(new Integer(Calendar.WEDNESDAY), new Integer(1));
    //			thu.put(new Integer(Calendar.THURSDAY), new Integer(0));
    //			thu.put(new Integer(Calendar.FRIDAY), new Integer(-1));
    //			thu.put(new Integer(Calendar.SATURDAY), new Integer(-2));
    //			fri.put(new Integer(Calendar.SUNDAY), new Integer(5));
    //			fri.put(new Integer(Calendar.MONDAY), new Integer(4));
    //			fri.put(new Integer(Calendar.TUESDAY), new Integer(3));
    //			fri.put(new Integer(Calendar.WEDNESDAY), new Integer(2));
    //			fri.put(new Integer(Calendar.THURSDAY), new Integer(1));
    //			fri.put(new Integer(Calendar.FRIDAY), new Integer(0));
    //			fri.put(new Integer(Calendar.SATURDAY), new Integer(-1));
    //			sat.put(new Integer(Calendar.SUNDAY), new Integer(6));
    //			sat.put(new Integer(Calendar.MONDAY), new Integer(5));
    //			sat.put(new Integer(Calendar.TUESDAY), new Integer(4));
    //			sat.put(new Integer(Calendar.WEDNESDAY), new Integer(3));
    //			sat.put(new Integer(Calendar.THURSDAY), new Integer(2));
    //			sat.put(new Integer(Calendar.FRIDAY), new Integer(1));
    //			sat.put(new Integer(Calendar.SATURDAY), new Integer(0));
    //		}
    //		return dayOfWeekDifferenceLookupTable;
    //	}
    //	/**
    //	 * @author ed
    //	 * SUN - MON = -1
    //	 */
    //	public static int getDayOfWeekDifference(int dayOfWeek1, int dayOfWeek2) {
    //		return ((Integer)((Map)getDayOfWeekDifferenceLookupTable().get(new Integer(dayOfWeek1))).get(new Integer(dayOfWeek2))).intValue();
    //	}
    //
    //        /**
    //         * @author Krishna Meduri
    //         * For a given date, it returns the Date of the next day with time set to 00:00:00:000
    //         * @param date
    //         * @return nextDay
    //         */
    //        public static Date getStartOfNextDay (Date date) {
    //            GregorianCalendar cal = new GregorianCalendar();
    //            cal.setTime(date);
    //            cal.add (Calendar.DATE, 1);
    //            cal.set(Calendar.AM_PM, Calendar.AM);
    //            cal.set(Calendar.HOUR,0);
    //            cal.set(Calendar.MINUTE,0);
    //            cal.set(Calendar.SECOND,0);
    //            cal.set(Calendar.MILLISECOND,0);
    //            return cal.getTime ();
    //        }
    //	/**
    //	 * given a date, return the date of the previous Sunday
    //	 */
    //	public static Date getStartOfWeek (Date date) {
    //		return getStartOfWeek(date, Calendar.SUNDAY);
    //	}
    //	/**
    //	 * Return the start date of the week this day falls in
    //	 * @param startDayOfWeek Calendar.MONDAY, etc.
    //	 */
    //	public static Date getStartOfWeek (Date date, int startDayOfWeek)
    //	{
    //		int dayOfWeek = getDatePart(date, Calendar.DAY_OF_WEEK);
    //		int dayDiff = getDayOfWeekDifference(startDayOfWeek, dayOfWeek);
    //		return addDay(date, dayDiff);
    //	}
    //
    //	/**
    //	 * given a date, return the date of the next Saturday
    //	 */
    //	public static Date getEndOfWeek (Date date) {
    //		return getEndOfWeek(date, Calendar.SATURDAY);
    //	}
    //	/**
    //	 * Return the end date of the week this day falls in
    //	 * @param startDayOfWeek Calendar.MONDAY, etc.
    //	 */
    //	public static Date getEndOfWeek (Date date, int endDayOfWeek)
    //	{
    //		int dayOfWeek = getDatePart(date, Calendar.DAY_OF_WEEK);
    //		int dayDiff = getDayOfWeekDifference(endDayOfWeek, dayOfWeek);
    //		return addDay(date, dayDiff);
    //	}
    //
    //	/**
    //	 * given a date, return the date of the next Friday
    //	 */
    //	public static Date getNextFriday(Date d)
    //	{
    //		// one day before saturday
    //		return addDay(getEndOfWeek(d),-1);
    //	}
    //
    //	/**
    //	 * Find out the number of quarter difference between two dates.
    //	 * @return int.  -ve if d1 quarter before d2, zero if d1 and d2 are in same quartet.
    //	 */
    //	public static int getQuarterDiff(Date d1, Date d2)
    //	{
    //		return (getQuarter(d1) - getQuarter(d2)) + 4 * (getYear(d1) - getYear(d2));
    //	}
    //
    //	/**
    //	 * A method that takes a taxPayment info and company tax information and returns the start date for that
    //	 * schedule
    //	 */
    //	public static Date getStartDateForSchedule(TaxPaymentInfo txInfo, String groupName, CompanyTaxInfo companyTaxInfo, Date endDate) {
    //		Date startDate = null;
    //		if (txInfo != null)
    //		{
    //			// Check with quarter the end date is in.
    //			Calendar cal = Calendar.getInstance();
    //			cal.setTime((Date)endDate.clone());
    //			if(txInfo.isSemiMonthly()) {
    //				cal.set(Calendar.DAY_OF_MONTH, 16);
    //				startDate = cal.getTime();
    //			} else if(txInfo.isMonthly()) {
    //				cal.set(Calendar.DAY_OF_MONTH, 1);
    //				startDate = cal.getTime();
    //			} else if(txInfo.isQuarterly()) {
    //				cal.set(Calendar.DAY_OF_MONTH, 1);
    //				int month = cal.get(Calendar.MONTH);
    //				cal.set(Calendar.MONTH, (month - 2));
    //				startDate = cal.getTime();
    //			}
    //		}
    //		else
    //		{
    //			// Check with quarter the end date is in.
    //			Calendar cal = Calendar.getInstance();
    //			cal.setTime((Date)endDate.clone());
    //			if(companyTaxInfo.isScheduleEffectiveOnEndDateEqualTo(ScheduleConstants.SEMI_MONTHLY, groupName))
    //			{
    //				cal.set(Calendar.DAY_OF_MONTH, 16);
    //				startDate = cal.getTime();
    //			}
    //			else if(companyTaxInfo.isScheduleEffectiveOnEndDateEqualTo(ScheduleConstants.MONTHLY, groupName))
    //			{
    //				cal.set(Calendar.DAY_OF_MONTH, 1);
    //				startDate = cal.getTime();
    //			}
    //			else if(companyTaxInfo.isScheduleEffectiveOnEndDateEqualTo(ScheduleConstants.QUARTERLY, groupName))
    //			{
    //				cal.set(Calendar.DAY_OF_MONTH, 1);
    //				int month = cal.get(Calendar.MONTH);
    //				cal.set(Calendar.MONTH, (month - 2));
    //				startDate = cal.getTime();
    //			}
    //		}
    //
    //		return startDate;
    //	}
    //
    //	public static Date max(Date date1, Date date2)
    //	{
    //		if (date1.after(date2))
    //			return date1;
    //		else
    //			return date2;
    //	}
    //
    //	public static Date min(Date date1, Date date2)
    //	{
    //		if (date1.before(date2))
    //			return date1;
    //		else
    //			return date2;
    //	}
    //
    //	public static boolean checkDate(int year, int month, int day)
    //	{
    //		String date = month + "/" + day + "/" + year;
    //		try {
    //			parseDate(date);
    //		} catch (Exception e) {
    //			return false;
    //		}
    //		return true;
    //	}
    //
    //	public static String getFullDayOfWeekName(Date date)
    //	{
    //		SimpleDateFormat format = new SimpleDateFormat("EEEEEEEE");
    //		return format.format(date);
    //	}
    //
    //	public static Period getQuarterAsPeriod(Quarter quarter, int year) {
    //		switch (quarter) {
    //		case first:
    //			return new Period(DateUtil.createDate(year,0,1), DateUtil.createDate(year,2,31));
    //		case second:
    //			return new Period(DateUtil.createDate(year,3,1), DateUtil.createDate(year,5,30));
    //		case third:
    //			return new Period(DateUtil.createDate(year,6,1), DateUtil.createDate(year,8,30));
    //		case fourth:
    //			return new Period(DateUtil.createDate(year,9,1), DateUtil.createDate(year,11,31));
    //		}
    //		return null;
    //	}
    //
    //	// Determine if the start date to end date matches a quarter period
    //	// Critiera: start/end dates have same year and quarter, match to start/end dates of that quarter
    //	// truncate hour/minutes to we compare date only
    //	public static boolean isQuarterPeriod(Date startDate, Date endDate)
    //	{
    //		if (getYear(startDate) != getYear(endDate))
    //			return false;
    //
    //		if (getQuarter(startDate) != getQuarter(endDate))
    //			return false;
    //
    //		return (getFirstDayOfQuarter(startDate).equals(dateTruncate(startDate)) && getLastDayOfQuarter(endDate).equals(dateTruncate(endDate)));
    //	}
    //
    //	// Determine if the start date to end date matches an annual period
    //	// Criteria: start/end dates have same year, match to start/end dates of that year
    //	// truncate hour/minutes to we compare date only
    //	public static boolean isAnnualPeriod(Date startDate, Date endDate)
    //	{
    //		if (getYear(startDate) != getYear(endDate))
    //			return false;
    //
    //		return (getFirstDayOfYear(startDate).equals(dateTruncate(startDate)) && getLastDayOfYear(endDate).equals(dateTruncate(endDate)));
    //	}
    //
    //	public static String getQTDOrYTDName(Date startDate, Date endDate)
    //	{
    //		String name = "";
    //		if (isAnnualPeriod(startDate, endDate))
    //		{
    //			name = "YTD";
    //		} else if (isQuarterPeriod(startDate, endDate)) {
    //			name = "QTR" + getQuarter(startDate);
    //		}
    //		return name;
    //	}
    //
    //	/**
    //	 * Check to see if we can use the YTD table instead of TaxDetail table.
    //	 * True if the start and end date is quarterly or annually, since that is what we currently support in YTD.
    //	 */
    //	public static boolean canUseYTDTable(Date startDate, Date endDate)
    //	{
    //		// IMPORTANT!: We can't use the YTD table on production currently because it does not match the tax detail table right now!
    //		boolean enableYTDCalculation = AppMgr.isProduction() ? false : false;
    //
    //		// We should not use YTD before 2009 on production unless we rebuild YTD/QTD for previous years
    //		if (AppMgr.isProduction() && DateUtil.getYear(startDate) < 2009)
    //			return false;
    //
    //		// Some forms like employer and employee setup has null values for start and end dates
    //		if (Helper.isEmpty(startDate) || Helper.isEmpty(endDate))
    //			return false;
    //
    //		return enableYTDCalculation && (isQuarterPeriod(startDate, endDate) || isAnnualPeriod(startDate, endDate));
    //	}
    //
    //	/**
    //	 * To check if the given date is a weekday or not
    //	 *
    //	 * @param date Input date to be checked
    //	 *
    //	 * @return Checks if the given date is a weekday and returns true,
    //	 * otherwise returns false
    //	 */
    //	public static boolean isAWeekDay(Date date) {
    //		Calendar cal = new GregorianCalendar();
    //		cal.setTime(date);
    //
    //		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
    //		return dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY;
    //	}
    //
    //     /**
    //      * Checks if the given date falls within the given start and end dates.
    //      * @param date The date to check
    //      * @param rangeStart The start of the range
    //      * @param rangeEnd The end of the range
    //      * @param inclusive Flag to indicate if the start and end dates are included in the check
    //      * @return Returns true if the given date is within the start and end date
    //      */
    //     public static boolean isDateInRange(Date date, Date rangeStart, Date rangeEnd, boolean inclusive) {
    //         if(inclusive) {
    //             if(date.equals(rangeStart) || date.equals(rangeEnd)) {
    //                 return true;
    //             }
    //         }
    //         if(rangeStart.before(date) && rangeEnd.after(date)) {
    //                 return true;
    //         }
    //         return false;
    //     }
    //
    //     /**
    //      * zero out the second in the date
    //      * @param date
    //      * @return zero second date
    //      */
    //     public static Date setZeroSecond(Date date) {
    //         Calendar cal = Calendar.getInstance();
    //         cal.setTime(date);
    //         cal.set(Calendar.SECOND, 0);
    //
    //         return cal.getTime();
    //     }
    //
    //
    //	 /**
    //	  * Sets anything past the minutes to zero; ie. seconds and milliseconds
    //	  * @param date The date to trim
    //	  * @return Returns a date with only hour and minute for time
    //	  */
    //	 public static Date setZeroPastMinutes(Date date) {
    //         Calendar cal = Calendar.getInstance();
    //         cal.setTime(date);
    //         cal.set(Calendar.SECOND, 0);
    //		 cal.set(Calendar.MILLISECOND, 0);
    //
    //         return cal.getTime();
    //     }
    //
    //    public static Date getLocalTimeForAddress(Address addr) {
    //        Date date = new Date();
    //        Calendar cal = Calendar.getInstance();
    //        cal.setTime(date);
    //
    //        TimeZone localZone = cal.getTimeZone();
    //        TimeInfo local = new TimeInfo(localZone.getRawOffset(), localZone.useDaylightTime());
    //        TimeInfo other = getTimeInfoForAddress(addr);
    //        int offset = local.getTimeDifferenceInHours(other, localZone.inDaylightTime(date));
    //        cal.add(Calendar.HOUR, offset);
    //
    //        return cal.getTime();
    //    }
    //
    //    public static TimeZone getTimeZoneForAddress(Address addr) {
    //        // special case for AZ since they are mountain but don't observe DST
    //        if(addr.getState().equalsIgnoreCase("AZ"))
    //            return TimeZone.getTimeZone("US/Arizona");
    //        else
    //            return getTimeInfoForAddress(addr).getTimezone();
    //    }
    //
    //    static private class TimeInfo {
    //
    //        int m_offset;
    //        boolean m_dst;
    //        TimeZone m_tz;
    //
    //        TimeInfo(int gmtOffset, boolean observesDST) {
    //            if (gmtOffset < -12 || gmtOffset > 12) {
    //                // assume the offset is specified in msec
    //                gmtOffset = gmtOffset / 3600000;
    //            }
    //            m_offset = gmtOffset;
    //            m_dst = observesDST;
    //            switch(m_offset)
    //            {
    //                case -5:
    //                    m_tz = TimeZone.getTimeZone("US/Eastern");
    //                    break;
    //                case -6:
    //                    m_tz = TimeZone.getTimeZone("US/Central");
    //                    break;
    //                case -7:
    //                    m_tz = TimeZone.getTimeZone("US/Mountain");
    //                    break;
    //                case -8:
    //                    m_tz = TimeZone.getTimeZone("US/Pacific");
    //                    break;
    //                case -9:
    //                    m_tz = TimeZone.getTimeZone("US/Alaska");
    //                    break;
    //                case -10:
    //                    m_tz = TimeZone.getTimeZone("US/Hawaii");
    //                    break;
    //                default:
    //                    m_tz = TimeZone.getDefault();
    //            }
    //        }
    //
    //        public TimeZone getTimezone() {
    //            return m_tz;
    //        }
    //
    //        public String toString() {
    //            return "GMT" + m_offset + "/" + (m_dst ? "y" : "n");
    //        }
    //
    //        public int getTimeDifferenceInHours(TimeInfo otherZone, boolean isDSTActive) {
    //            int diff = otherZone.m_offset - this.m_offset;
    //
    //            // If nec, adjust for DST diff
    //            if (isDSTActive && this.m_dst != otherZone.m_dst) {
    //                if (this.m_dst) {
    //                    diff -= 1;
    //                } else {
    //                    diff += 1;
    //                }
    //            }
    //
    //            return diff;
    //        }
    //    }
    //
    //    private static TimeInfo getTimeInfoForAddress(Address addr) {
    //        boolean dst = true;
    //        if(addr.getState().equals("AZ") || addr.getState().equals("HI"))
    //            dst = false;
    //
    //        Integer offset = stateGMTOffsetMap.get(addr.getState());
    //        if(offset == null)
    //        {
    //			/* build a query for TOPLink */
    //			ExpressionBuilder group = new ExpressionBuilder();
    //			Expression exp = group.get("zipCode").equal(addr.getZipCode());
    //
    //			ZipCodeTimezone result = Helper.readObject(com.paycycle.timetracking.entities.ZipCodeTimezone.class, exp, true);
    //
    //			if(result != null) {
    //				offset = result.getOffset();
    //			}
    //        }
    //
    //        // If it's still null, we don't have info for this zip code so we need to get the offset from the default map.
    //        if(offset == null)
    //            offset = stateGMTOffsetDefaultMap.get(addr.getState());
    //
    //        return new TimeInfo(offset, dst);
    //    }
    //
    //    // All of these states lie entirely in one timezone.
    //    private static final Map<String, Integer> stateGMTOffsetMap = new HashMap<String, Integer> () {{
    //        put("AK", -9);        put("AL", -6);        put("AR", -6);        put("AZ", -7);        put("CA", -8);        put("CO", -7);
    //        put("CT", -5);        put("DC", -5);        put("DE", -5);        put("GA", -5);        put("HI", -10);       put("IA", -6);
    //        put("IL", -6);        put("LA", -6);        put("MA", -5);        put("MD", -5);        put("ME", -5);        put("MN", -6);
    //        put("MO", -6);        put("MS", -6);        put("MT", -7);        put("NC", -5);        put("NH", -5);        put("NJ", -5);
    //        put("NM", -7);        put("NV", -8);        put("NY", -5);        put("OH", -5);        put("OK", -6);        put("PA", -5);
    //        put("RI", -5);        put("SC", -5);        put("UT", -7);        put("VA", -5);        put("VT", -5);        put("WA", -8);
    //        put("WI", -6);        put("WV", -5);        put("WY", -7);
    //    }};
    //
    //    // All of these states are split over two timezones, but the majority of the zip codes fall in this zone, so we can use it as a last resort.
    //    private static final Map<String, Integer> stateGMTOffsetDefaultMap = new HashMap<String, Integer> () {{
    //        put("FL", -5);        put("ID", -7);        put("IN", -5);        put("KS", -6);        put("KY", -5);        put("MI", -5);
    //        put("ND", -6);        put("NE", -6);        put("OR", -8);        put("SD", -6);        put("TN", -6);        put("TX", -6);
    //    }};
    //
    //    public static void main(String[] args) {
    //        Address addr = new Address("", "", "", "", "OH", "88888");
    //        System.out.println("Current time at (" + addr + ") = " + DateUtil.getLocalTimeForAddress(addr));
    //        System.out.println("Current timezone at (" + addr + ") = " + DateUtil.getTimeZoneForAddress(addr));
    //
    //        addr = new Address("", "", "", "", "CO", "88888");
    //        System.out.println("Current time at (" + addr + ") = " + DateUtil.getLocalTimeForAddress(addr));
    //        System.out.println("Current timezone at (" + addr + ") = " + DateUtil.getTimeZoneForAddress(addr));
    //
    //        String[] ids = TimeZone.getAvailableIDs();
    //        for(String id : ids)
    //        {
    ////            System.out.println(id);
    //        }
    //    }
    //
    //    /**
    //     * Constructs a GregorianCalendar object set to the current date and time.
    //     *
    //     * @return A GregorianCalendar object set to the current date and time.
    //     */
    //    public static GregorianCalendar newGregorianCalendar()  {
    //    	return newGregorianCalendar(new Date());
    //   	}
    //
    //    /**
    //     * Converts a Date to a GregorianCalendar object.
    //     *
    //     * @return A GregorianCalendar object set to the date/time in the Date object.
    //     */
    //    public static GregorianCalendar newGregorianCalendar (Date date)  {
    //    	GregorianCalendar gc = (GregorianCalendar) GregorianCalendar.getInstance();
    //    	gc.setTime(date);
    //    	return gc;
    //   	}
    //
    //    /**
    //     * Constructs a XMLGregorianCalendar object set to the current date and time.
    //     *
    //     * @return A XMLGregorianCalendar object set to the current date and time.
    //     */
    //    public static XMLGregorianCalendar newXMLGregorianCalendar()  {
    //    	try {
    //        	return DatatypeFactory.newInstance().newXMLGregorianCalendar(newGregorianCalendar());
    //    	} catch (DatatypeConfigurationException ex) {
    //    		throw new RuntimeException (ex);
    //    	}
    //   	}
    //
    //    /**
    //     * Converts a Date to a XMLGregorianCalendar object.
    //     *
    //     * @return A XMLGregorianCalendar object set to the date/time in the Date object.
    //     */
    //    public static XMLGregorianCalendar newXMLGregorianCalendar (Date date)  {
    //    	try {
    //        	return DatatypeFactory.newInstance().newXMLGregorianCalendar(newGregorianCalendar(date));
    //    	} catch (DatatypeConfigurationException ex) {
    //    		throw new RuntimeException (ex);
    //    	}
    //   	}
}
