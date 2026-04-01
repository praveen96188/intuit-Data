/* 
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2004-10-21   Initial Implementation
 * 
 * NOTE: All third-party runtime exceptions are caught and re-thrown as 
 * portable exceptions
 */

package com.intuit.spc.foundations.portabilitySpecific.util;

import com.intuit.spc.foundations.portability.util.*;
import com.intuit.spc.foundations.portability.text.*;
import com.intuit.spc.foundations.portability.*;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * A platform specific implementation of SpcfCalendar
 */
public class SpcfCalendarImpl extends SpcfCalendar
{
	/**
	 * needed for serialization
	 */  
	private static final long serialVersionUID = 1052899532314460204L;

	/**
	 * The encapsulated calendar date time value
	 */
	protected long mTime;
	//protected GregorianCalendar mThirdPartyGregorianCalendar;
	
	/**
	 * encapsulated timezone
	 */
	protected TimeZone mTimeZone;
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar()
	 */
	public SpcfCalendarImpl()
	{
		mTimeZone = TimeZone.getTimeZone("UTC");
		mTime = new GregorianCalendar(mTimeZone).getTimeInMillis();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(SpcfTimeZone)
	 * @param zone the time-zone to be used.
	 */
	public SpcfCalendarImpl(SpcfTimeZone zone)
	{	
		assertLocalTimeZone(zone);
		mTimeZone = ((SpcfTimeZoneImpl)zone).toSpecific();
		mTime = new GregorianCalendar(mTimeZone).getTimeInMillis();
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int, int, int, int, int)
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 */
	public SpcfCalendarImpl(int year, int month, int day,
		int hour, int minute, int second, int millisecond)
	{
		mTimeZone = TimeZone.getTimeZone("UTC");
		constructorHelper(year, month, day, hour, minute, second, millisecond);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int, int, int, int, int, SpcfTimeZone)
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 * @param zone the time-zone.
	 */
	public SpcfCalendarImpl(int year, int month, int day,
			int hour, int minute, int second, int millisecond, SpcfTimeZone zone)
	{
		assertLocalTimeZone(zone);
		mTimeZone = ((SpcfTimeZoneImpl)zone).toSpecific();
		constructorHelper(year, month, day, hour, minute, second, millisecond);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int)
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 */
	public SpcfCalendarImpl(int year, int month, int day)
	{
		mTimeZone = TimeZone.getTimeZone("UTC");
		constructorHelper(year, month, day, 0, 0, 0, 0);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(int, int, int, SpcfTimeZone)
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param zone the time zone.
	 */
	public SpcfCalendarImpl(int year, int month, int day, SpcfTimeZone zone)
	{	
		assertLocalTimeZone(zone);
		mTimeZone = ((SpcfTimeZoneImpl)zone).toSpecific();
		constructorHelper(year, month, day, 0, 0, 0, 0);
	}
	
	/**
	 * Help the constructors save and validate the milliseconds
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 */
	public void constructorHelper(int year, int month, int day,
			int hour, int minute, int second, int millisecond)
	{
		GregorianCalendar gc = new GregorianCalendar(mTimeZone);
		gc = setValues(gc, year, month, day, hour, minute, second, millisecond);
		mTime = gc.getTimeInMillis();
	}
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(long)
	 * @param milliseconds the number of milliseconds since 1/1/1970, can be a negative number.
	 */
	public SpcfCalendarImpl(long milliseconds)
	{		
		mTimeZone = TimeZone.getTimeZone("UTC");
		constructorHelper(milliseconds);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfFactory#createCalendar(long, SpcfTimeZone)
	 * @param milliseconds the number of milliseconds since 1/1/1970, can be a negative number.
	 * @param zone the timezone to be used
	 */
	public SpcfCalendarImpl(long milliseconds, SpcfTimeZone zone)
	{
		assertLocalTimeZone(zone);
		mTimeZone = ((SpcfTimeZoneImpl)zone).toSpecific();
		constructorHelper(milliseconds);
	}
	
	/**
	 * Help the constructors save and validate the milliseconds
	 * @param milliseconds the milliseconds to save for the date/time
	 */
	private void constructorHelper(long milliseconds)
	{
		mTime = milliseconds;
		GregorianCalendar gc = new GregorianCalendar(mTimeZone);
		gc.setTimeInMillis(milliseconds);
		// now validate the resulting date is within range
		checkDateRange(gc);
	}
	
	/**
	 * Constructs a GregorianCalendar with the specified third party runtime object. 
	 * This constructor allows the user to encapsulate any GregorianCalendar 
	 * object.  It is assumed that the date and time is of UTC time zone in 
	 * the GregorianCalendar parameter.
	 * 
	 * @param	gregorianCalendar implementation
     * @throws SpcfArgumentNullException if gregorianCalendar is null
     * @throws SpcfIllegalArgumentException if the date/time value is out of range.
	*/
	public SpcfCalendarImpl(GregorianCalendar gregorianCalendar)
	{
		SpcfParamValidator.checkIsNotNull(gregorianCalendar, "gregorianCalendar");
		// now validate the resulting date is within range
		checkDateRange(gregorianCalendar);
		mTime = gregorianCalendar.getTimeInMillis();
		mTimeZone = gregorianCalendar.getTimeZone();
	}

	/**
	* Returns the encapsulated third party runtime object 
	* 
	* @return a System.DateTime implementation
	*/
	public GregorianCalendar toSpecific()
	{
		GregorianCalendar gc = new GregorianCalendar(mTimeZone);
		gc.setTimeInMillis(mTime);
		return gc;
	}
	
	/**
	* Returns the maximum date value stored in this instance in the 
	* TimeZone of UTC.
	*
	* @return a GregorianCalendar implementation
	*/
	private static GregorianCalendar getMaxDateValue()
	{
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.clear();
		cal.set(9999, 11, 31, 23, 59, 59);
		cal.set(Calendar.MILLISECOND, 999);
		return cal;
	}

	/**
	* Returns the minimum date value stored in this instance in the 
	* TimeZone of UTC.
	*
	* @return a GregorianCalendar implementation
	*/
	private static GregorianCalendar getMinDateValue()
	{
		GregorianCalendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
		cal.clear();
		cal.set(1600, 0, 1, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	/**
	 * Validates that the parameters are within range.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
     * @throws SpcfIndexOutOfBoundsException if the specified field is out of range.
	 */
	private void checkYearMonthDayValues(int year, int month, int day)
	{
		try
		{
			if ( (year > 9999) || (year < 1600))
				throw new SpcfIllegalArgumentException("year value out of range");
			if ( (month > 12) || (month < 1))
				throw new SpcfIllegalArgumentException("month value out of range");
			if ( (day > 31) || (day < 1))
				throw new SpcfIllegalArgumentException("day value out of range");
			// now see if this day number is valid for this month and year.
			GregorianCalendar tmpCal = new GregorianCalendar(mTimeZone);
			tmpCal.clear();
			tmpCal.set(year, month-1, day);
			int tmpDay = tmpCal.get(Calendar.DATE);
			if (tmpDay != day)
			{
				throw new SpcfIllegalArgumentException("day value out of range");
			}
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			// if an unknown field is given
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}
	
	/**
	 * Validates that the date is within range.
	 *
	 * @param tmpCal the date in question.
     * @throws SpcfIllegalArgumentException if any of the specified date
     *  is out of range.
	 */
	private void checkDateRange(GregorianCalendar tmpCal)
	{	
		//if necessary, convert tmpCal into Utc time zone for comparison
		long date = tmpCal.getTimeInMillis();
		// now validate the resulting date is within range
		if( date < SpcfCalendar.MinMillisecond  || date > SpcfCalendar.MaxMillisecond)
		{
			throw new SpcfIllegalArgumentException("date value out of range");
		}
	}

	/**
	 * Validates that the parameters are within range.
	 *
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param seconds the second (0 - 59).
	 * @param milliseconds the millisecond (0 - 999).
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
	 */
	private void checkHourMinuteSecondMillisecondValues(int hour, int minute, int seconds, int milliseconds)
	{
		if ( (hour > 23) || (hour < 0))
			throw new SpcfIllegalArgumentException("hour value out of range");
		if ( (minute > 59) || (minute < 0))
			throw new SpcfIllegalArgumentException("minute value out of range");
		if ( (seconds > 59) || (seconds < 0))
			throw new SpcfIllegalArgumentException("seconds value out of range");
		if ( (milliseconds > 999) || (milliseconds < 0))
			throw new SpcfIllegalArgumentException("milliseconds value out of range");
	}
	
	/**
	 * Convenience method to add a value to the specified date field.
	 *
	 * @param field the date/time field to add the value to.
	 * @param value the value to add to the date/time field.
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
     * @throws SpcfIndexOutOfBoundsException if the specified field is out of range.
	 */
	private void internalAdd(int field, int value)
	{
		try
		{
			// we will do the calculation on a tmp calendar in case
			// the result is out of range.
			GregorianCalendar tmpCal = (GregorianCalendar) toSpecific().clone();
			tmpCal.add(field, value);
			
			// now validate the resulting date is within range
			checkDateRange(tmpCal);
			
			// ok to save new value in our calendar
			mTime = tmpCal.getTimeInMillis();
			mTimeZone = tmpCal.getTimeZone();
		}
		catch(IllegalArgumentException e)
		{
			// if an unknown field is given
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}

	/**
	 * Convenience method to get the value of the specified date field.
	 *
	 * @param field the date/time field to get the value of.
	 * @return the value of the date/time field.
     * @throws SpcfIndexOutOfBoundsException if the specified field is out of range.
	 */
	private int internalGet(int field)
	{
		try
		{
			return toSpecific().get(field);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			// if an unknown field is given
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}

	/**
	 * Convenience method to get actual maximum value of the specified date field.
	 *
	 * @param field the date/time field to get the maximum value of.
	 * @return the value of the date/time field.
     * @throws SpcfIndexOutOfBoundsException if the specified field is out of range.
	 */
	private int internalGetActualMaximum(int field)
	{
		try
		{
			return toSpecific().getActualMaximum(field);
		}
		catch(ArrayIndexOutOfBoundsException e)
		{
			// if an unknown field is given
			throw new SpcfIndexOutOfBoundsException(e);
		}
	}

	//SpcfCalendar overrides

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#doParse(String, String)
	 */
	@Override
	protected SpcfCalendar doParse(String pattern, String dateString)
	{
		return SpcfDateFormat.parse(dateString, pattern);
	}
		
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getNow()
	 */
	@Override
	protected SpcfCalendar doGetNow()
	{
		return new SpcfCalendarImpl();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#doGetMaxDate()
	 */
	@Override
	protected SpcfCalendar doGetMaxDate()
	{	
		return new SpcfCalendarImpl(getMaxDateValue());		
	}
		
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#doGetMinDate()
	 */
	@Override
	protected SpcfCalendar doGetMinDate()
	{	
		return new SpcfCalendarImpl(getMinDateValue());
	}
		
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#addDays(int)
	 */
	@Override
	public void addDays(int days)
	{
		internalAdd(Calendar.DATE, days);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#addHours(int)
	 */
	@Override
	public void addHours(int hours)
	{
		internalAdd(Calendar.HOUR, hours);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#addMilliseconds(int)
	 */
	@Override
	public void addMilliseconds(int milliseconds)
	{
		internalAdd(Calendar.MILLISECOND, milliseconds);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#addMinutes(int)
	 */
	@Override
	public void addMinutes(int minutes)
	{
		internalAdd(Calendar.MINUTE, minutes);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#addMonths(int)
	 */
	@Override
	public void addMonths(int months)
	{
		internalAdd(Calendar.MONTH, months);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#addSeconds(int)
	 */
	@Override
	public void addSeconds(int seconds)
	{
		internalAdd(Calendar.SECOND, seconds);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#addYears(int)
	 */
	@Override
	public void addYears(int years)
	{
		internalAdd(Calendar.YEAR, years);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#after(SpcfCalendar)
	 */
	@Override
	public boolean after(SpcfCalendar calendar)
	{
		SpcfParamValidator.checkIsNotNull(calendar, "calendar");
		
		if (calendar instanceof SpcfCalendarImpl)
		{
			SpcfCalendarImpl inCalImpl = (SpcfCalendarImpl) calendar;
			GregorianCalendar inGC = inCalImpl.toSpecific();
			return toSpecific().after(inGC);
		}
		else 
		{
			throw new SpcfClassCastException();
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#before(SpcfCalendar)
	 */
	@Override
	public boolean before(SpcfCalendar calendar)
	{
		SpcfParamValidator.checkIsNotNull(calendar, "calendar");
		
		if (calendar instanceof SpcfCalendarImpl)
		{
			SpcfCalendarImpl inCalImpl = (SpcfCalendarImpl) calendar;
			GregorianCalendar inGC = inCalImpl.toSpecific();
			return toSpecific().before(inGC);
		}
		else 
		{
			throw new SpcfClassCastException();
		}
	}

    @Override
    public boolean between(SpcfCalendar beginningDate, SpcfCalendar endingDate) {
        if(beginningDate == null || endingDate == null) {
            return false;
        }
        
        return this.compareTo(beginningDate) > -1 && this.compareTo(endingDate) < 1;
    }

    /**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#compareTo(Object)
	 */
	@Override
	public int compareTo(Object o)
	{
		SpcfParamValidator.checkIsNotNull(o, "o");
		
		if (o instanceof SpcfCalendarImpl)
		{	
			SpcfCalendarImpl calendar = (SpcfCalendarImpl) o;
			Date inDateTime = calendar.toSpecific().getTime();
			Date dateTime = toSpecific().getTime();
			return dateTime.compareTo(inDateTime);
		}
		else 
		{
			throw new SpcfClassCastException();
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#equals(Object)
	 */
	@Override
	public boolean equals(Object value)
	{
		if (value == null)
    	{
    		return false;
    	}
		
		if (value instanceof SpcfCalendarImpl)
		{
			SpcfCalendarImpl calendar = (SpcfCalendarImpl) value;
			GregorianCalendar inGC = calendar.toSpecific();
			return toSpecific().equals(inGC);
		}
		else 
		{
			return false;
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#format(String)
	 */
	@Override
	public String format(String pattern)
	{
		// replace with SpcfDateFormat usage
		return SpcfDateFormat.format(this, pattern);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getDay()
	 */
	@Override
	public int getDay()
	{
		return internalGet(Calendar.DATE);
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return toSpecific().hashCode();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getHour()
	 */
	@Override
	public int getHour()
	{
		return internalGet(Calendar.HOUR_OF_DAY);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getMillisecond()
	 */
	@Override
	public int getMillisecond()
	{
		return internalGet(Calendar.MILLISECOND);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getMinute()
	 */
	@Override
	public int getMinute()
	{
		return internalGet(Calendar.MINUTE);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getMonth()
	 */
	@Override
	public int getMonth()
	{
		return internalGet(Calendar.MONTH) + 1;
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getSecond()
	 */
	@Override
	public int getSecond()
	{
		return internalGet(Calendar.SECOND);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getTimeInMilliseconds()
	 **/
	@Override
	public long getTimeInMilliseconds()
	{
		return toSpecific().getTimeInMillis();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getYear()
	 */
	@Override
	public int getYear()
	{
		return internalGet(Calendar.YEAR);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getDayOfYear()
	 */
	@Override
	public int getDayOfYear()
	{
		return internalGet(Calendar.DAY_OF_YEAR);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getDayOfWeek()
	 */
	@Override
	public int getDayOfWeek()
	{
		return internalGet(Calendar.DAY_OF_WEEK);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getDaysInMonth()
	 */
	@Override
	public int getDaysInMonth()
	{
		return internalGetActualMaximum(Calendar.DAY_OF_MONTH);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#getDaysInYear()
	 */
	@Override
	public int getDaysInYear()
	{
		return internalGetActualMaximum(Calendar.DAY_OF_YEAR);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#setValues(int, int, int, int, int, int, int)
	 */
	@Override
	public void setValues(int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		try
		{
			// check the parameter values are within range.
			checkYearMonthDayValues(year, month, day);
			checkHourMinuteSecondMillisecondValues(hour, minute, second, millisecond);
			
			// we will do the calculation on a tmp calendar in case
			// the result is out of range.
			GregorianCalendar tmpCal = new GregorianCalendar(mTimeZone);
			tmpCal.clear();
			// java months are range 0-11.  The input parameter range is 1-12.
			tmpCal.set(year, month-1, day, hour, minute, second);
			tmpCal.set(Calendar.MILLISECOND, millisecond);

			// now validate the resulting date is within range
			checkDateRange(tmpCal);
			
			// ok to save new value in our calendar
			mTime = tmpCal.getTimeInMillis();
			mTimeZone = tmpCal.getTimeZone();
		}
		catch(IllegalArgumentException e)
		{
			// if an unknown field is given in set
			throw new SpcfIllegalArgumentException(e);
		}
	}
	
	/**
	 * Sets the values for the fields year, month, day, hour, minute, second 
	 * and millisecond into the GregorianCalendar
	 * @param gc the GregorianCalendar to set the values into
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 * @return the GregorianCalendar with the values set
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
	 **/
	private GregorianCalendar setValues(GregorianCalendar gc, int year, int month, int day, int hour, int minute, int second, int millisecond)
	{
		try
		{
			// check the parameter values are within range.
			checkYearMonthDayValues(year, month, day);
			checkHourMinuteSecondMillisecondValues(hour, minute, second, millisecond);
			
			// we will do the calculation on a tmp calendar in case
			// the result is out of range.
			GregorianCalendar tmpCal = new GregorianCalendar(gc.getTimeZone());
			tmpCal.clear();
			// java months are range 0-11.  The input parameter range is 1-12.
			tmpCal.set(year, month-1, day, hour, minute, second);
			tmpCal.set(Calendar.MILLISECOND, millisecond);

			// now validate the resulting date is within range
			checkDateRange(tmpCal);
			
			// ok to save new value in our calendar
			return tmpCal;
		}
		catch(IllegalArgumentException e)
		{
			// if an unknown field is given in set
			throw new SpcfIllegalArgumentException(e);
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#setValues(int, int, int)
	 */
	@Override
	public void setValues(int year, int month, int day)
	{
		setValues(year, month, day, 0, 0, 0, 0);
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#subtract(SpcfCalendar)
	 */
	@Override
	public long subtract(SpcfCalendar calendar)
	{
		return (getTimeInMilliseconds() - calendar.getTimeInMilliseconds());
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#toString()
	 */
	@Override
	public String toString()
	{
		return SpcfDateFormat.format(this, "yyyy/MM/dd HH:mm:ss.S");
		
		//SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.S");
		/* this still gives the format in local TZ
		Date thisDate = mThirdPartyGregorianCalendar.getTime();	
		long ms = thisDate.getTime();
		Date newDate = new Date();
		newDate.setTime(ms);
		return df.format(newDate);
		*/
		/* this still gives the format in local TZ
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(mThirdPartyGregorianCalendar.getTimeInMillis());
		return df.format(gc.getTime());
		*/
		// this will output the date in UTC
		/*
		GregorianCalendar gc = new GregorianCalendar();
		int year = mThirdPartyGregorianCalendar.get(GregorianCalendar.YEAR);
		int month = mThirdPartyGregorianCalendar.get(GregorianCalendar.MONTH);
		int day = mThirdPartyGregorianCalendar.get(GregorianCalendar.DATE);
		int hour = mThirdPartyGregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY);
		int minute = mThirdPartyGregorianCalendar.get(GregorianCalendar.MINUTE);
		int second = mThirdPartyGregorianCalendar.get(GregorianCalendar.SECOND);
		int millisecond = mThirdPartyGregorianCalendar.get(GregorianCalendar.MILLISECOND);
		gc.set(year, month, day, hour, minute, second);
		gc.set(GregorianCalendar.MILLISECOND, millisecond);
		return df.format(gc.getTime());
		*/
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#copy()
	 */
	@Override
	public SpcfCalendar copy() 
	{	
		return new SpcfCalendarImpl((GregorianCalendar)toSpecific().clone());
	}
	
	/**
	 * Throw an exception if not using the local time zone
	 * @param zone the time zone in question
	 * @throws SpcfIllegalArgumentException if the time zone is not local
	 * @throws SpcfArgumentNullException if the time zone is null
	 */
	private void assertLocalTimeZone(SpcfTimeZone zone)
	{
		SpcfParamValidator.checkIsNotNull(zone, "zone");
		if(zone != SpcfTimeZone.getLocalTimeZone())
		{
			throw new SpcfIllegalArgumentException("Currently only local time-zone is supported.");
		}
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#isLocal()
	 */
	@Override
	public boolean isLocal() 
	{	
		return mTimeZone.hasSameRules(TimeZone.getDefault());
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#isUTC()
	 */
	@Override
	public boolean isUTC() 
	{
		return mTimeZone.hasSameRules(TimeZone.getTimeZone("UTC"));
	}
	
	/**
	 * Gets the current object's date/time value in ISO8601 format and in UTC time-zone.
	 * This property is provided to support Xml serialization.
	 * @return date/time value of this instance in ISO8601 format.
	 */
	public String getISO8601()
	{
		return toISO8601();
	}
	
	/**
	 * Sets this object's date/time value from an ISO8601 formatted date/time string.
	 * This proerty is provided to support Xml serialization.
	 * @param date The ISO8601 formatted date/time string to be parsed.
	 * @throws SpcfArgumentNullException if date is null
	 * @throws SpcfFormatException if the specified pattern is invalid or the date cannot be parsed
	 * @throws SpcfIllegalArgumentException if the date is out of range ( MinDate > date > MaxDate).
	 */
	public void setISO8601(String date)
	{
		GregorianCalendar gc = ((SpcfCalendarImpl)SpcfCalendar.fromISO8601(date)).toSpecific();
		mTime = gc.getTimeInMillis();
		mTimeZone = gc.getTimeZone();
	}

	/**
	 * @see com.intuit.spc.foundations.portability.util.SpcfCalendar#toDate()
	 */
	@Override
	public Date toDate() {
		return new Date(getTimeInMilliseconds());
	}
	
	public DateTime toDateTime() {
		return new DateTime(getTimeInMilliseconds());
	}

	public LocalDate toLocalDate() {
		return new LocalDate(getTimeInMilliseconds());
	}
}

