/*
 * author		LGM
 * department	SPC Foundations
 * project	    Portability
 * 2004-10-21   Initial Implementation
 */

package com.intuit.spc.foundations.portability.util;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfStringBuilder;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.text.SpcfDateTimeEnum;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


/**
 * A Calendar implementation.
 * <p>
 * The date range valid in SpcfCalendar is from MinDate to MaxDate inclusive.
 * <br>The MinDate is 1600/1/1 00:00:00.000
 * <br>The MaxDate is 9999/12/31 23:59:59.999
 * </p>
 */
public abstract class SpcfCalendar implements Serializable, Comparable
{
	/**
	 * Value used only on java side for serialization.
	 */
	private static final long serialVersionUID = -3984644784731367972L;

	/**
	 * The maximum allowed date represented in milliseconds since January 1, 1970 00:00:00 GMT.
	 * This date is equivalent to 9999-12-31T23:59:59.999Z represented in ISO8601 format.
	 */
	public static final long MaxMillisecond = 253402300799999L;

	/**
	 * The minimum allowed date represented in milliseconds since January 1, 1970 00:00:00 GMT.
	 * This date is equivalent to 1600-01-01T00:00:00.0Z represented in ISO8601 format.
	 */
	public static final long MinMillisecond = -11676096000000L;

	/**
	 * Used for converting SpcfCalendar to date literal in "YYYY-MM-DD" format.
	 */
	public static final String DATE_TEMPLATE = "%s-%s-%s"; //YYYY-MM-DD

	/**
	 * Default constructor, used by the derived classes.
	 * Any XML Serializable class requires a public default ctor.
	 */
	public SpcfCalendar()
	{
		// this is intentially blank
	}

	/**
	 * calendar instance that is used for static methods
	 */
	protected static SpcfCalendar sCalendar;

	/**
	 * The maximum date value stored in SpcfCalendarImpl in the
	 * TimeZone of UTC.
	 */
	public static final SpcfCalendar MaxDate;

	/**
	 * The minimum date value stored in SpcfCalendarImpl in the
	 * TimeZone of UTC.
	 */
	public static final SpcfCalendar MinDate;

	/**
	 * The integer value for Sunday.
	 */
	public static final int Sunday = 1;

	/**
	 * The integer value for Monday.
	 */
	public static final int Monday = 2;

	/**
	 * The integer value for Tuesday.
	 */
	public static final int Tuesday = 3;

	/**
	 * The integer value for Wednesday.
	 */
	public static final int Wednesday = 4;

	/**
	 * The integer value for Thursday.
	 */
	public static final int Thursday = 5;

	/**
	 * The integer value for Friday.
	 */
	public static final int Friday = 6;

	/**
	 * The integer value for Saturday.
	 */
	public static final int Saturday = 7;

	/**
	 * The integer value for January.
	 */
	public static final int January = 1;

	/**
	 * The integer value for February.
	 */
	public static final int February = 2;

	/**
	 * The integer value for March.
	 */
	public static final int March = 3;

	/**
	 * The integer value for April.
	 */
	public static final int April = 4;

	/**
	 * The integer value for May.
	 */
	public static final int May = 5;

	/**
	 * The integer value for June.
	 */
	public static final int June = 6;

	/**
	 * The integer value for July.
	 */
	public static final int July = 7;

	/**
	 * The integer value for August.
	 */
	public static final int August = 8;

	/**
	 * The integer value for September.
	 */
	public static final int September = 9;

	/**
	 * The integer value for October.
	 */
	public static final int October = 10;

	/**
	 * The integer value for November.
	 */
	public static final int November = 11;

	/**
	 * The integer value for December.
	 */
	public static final int December = 12;

	static
	{
		sCalendar = SpcfFactory.getInstance().createCalendar();
		//Returns the maximum date value stored in SpcfCalendarImpl in the TimeZone of UTC.
		MaxDate = sCalendar.doGetMaxDate();
		//Returns the minimum date value stored in SpcfCalendarImpl in the TimeZone of UTC.
		MinDate = sCalendar.doGetMinDate();
	}

	/**
	* Converts the specified string representation of a date and time
	* of TimeZone of UTC to its calendar equivalent.
	*
	* @param pattern the format pattern to use.
	* @param dateString the date/time string to be parsed.
	* @return a portable calendar implementation
	*/
	protected abstract SpcfCalendar doParse(String pattern, String dateString);

	/**
	 * Constructs a calendar object with the
	 * current time in the TimeZone of UTC.
	 *
	 * @return a SpcfCalendar implementation
     * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
	 */
	protected abstract SpcfCalendar doGetNow();

	/**
	* Returns the maximum date value stored in SpcfCalendarImpl in the
	* TimeZone of UTC.
	*
	* @return a portable calendar implementation
	*/
	protected abstract SpcfCalendar doGetMaxDate();

	/**
	* Returns the minimum date value stored in SpcfCalendarImpl in the
	* TimeZone of UTC.
	*
	* @return a portable calendar implementation
	*/
	protected abstract SpcfCalendar doGetMinDate();

	/**
	 * Constructs a calendar object with the
	 * current time in the TimeZone of UTC.
	 *
	 * @return a SpcfCalendar implementation
	 */
	static public SpcfCalendar getNow()
	{
		return sCalendar.doGetNow();
	}

	static public long getCurrentTimeInMilliseconds()
	{
		return getNow().getTimeInMilliseconds();
	}

	/**
	 * Converts the specified string representation of a date and time
	 * of TimeZone of UTC to its calendar equivalent.
	 * <p>
	 * See SpcfDateFormat for portable pattern formats.
	 * See the SimpleDateFormat class on the java platform for a definition
	 * of the valid patterns on the java platform.
	 * See the DateTimeFormatInfo class on the
	 * dotnet platform for a definition of the valid patterns on the
	 * dotnet platform.
	 * </p>
	 * <p>
	 * <br>If dateString contains only a time, and no date, then January 1, 1970 is used.
	 * <br>If dateString contains only a date and no time, midnight (00:00:00) is used.
	 * </p>
	 * @param pattern the format pattern to use.
	 * @param dateString the date/time string to be parsed.
	 * @return an SpcfCalendar instance
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if pattern or dateString is null
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if pattern is an empty string or if the specified dateString is out of range.
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfFormatException if pattern invalid or the dateString
	 * does not conform to the format specified in the pattern.
	 **/
	static public SpcfCalendar parse(String pattern, String dateString)
	{
		return sCalendar.doParse(pattern, dateString);
	}

	/**
	 * Parses ISO8601 formatted date/time string to produce an SpcfCalendar (date).<br>
	 * This format is also used for the XML (XSD) dateTime datatype.<br>
	 * Though a valid ISO8601 formated string can take many variations but SPC-F allows only
	 * a limited set of variations. For a IS08601 formatted date to be compliant with SPC-F,
	 * following rules must be followed: <br>
	 * <ul>
	 * <li>The text must follow the format "yyyy-MM-ddTHH:mm:ss.SZ".
	 * <li>If time is present, it must be seperated from date using character 'T'.
	 * <li>Date parts must be seperated using character '-'.
	 * <li>Time parts must be seperated using character ':' and milliseconds must be seperated by character '.'.
	 * <li>Time information is optional but date is mandatory.
	 * <li>The string must always end with 'Z'.
	 * </ul>
	 * <br>Examples:
	 * <ul>
	 * <li>2002-01-03T00:00:00.0Z -> full date-time format including milliseconds.
	 * <li>2002-01-03T00:00:00Z -> date-time format, excluding milliseconds.
	 * <li>2002-01-03T00:00Z -> date-time format, excluding seconds and milliseconds.
	 * <li>2002-01-03T00Z -> date-time format, excluding minutes, seconds and milliseconds.
	 * <li>2002-01-03Z -> date-time format, excluding time.
	 * </ul>
	 * @param date The ISO8601 formatted date/time string to be parsed.
	 * @return An SpcfCalendar parsed from the string
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfArgumentNullException if date is null
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfFormatException if the specified pattern is invalid or the text cannot be parsed
	 * @throws com.intuit.sbd.payroll.psp.spcf.SpcfIllegalArgumentException if the date is out of range ( MinDate > date > MaxDate).
	 */
	static public SpcfCalendar fromISO8601(String date)
	{
		return SpcfDateFormat.parse(date, SpcfDateTimeEnum.Iso8601);
	}

	/**
	 * Returns the current object's date/time value in ISO8601 format and in UTC time-zone.
	 * @return date/time value of this instance in ISO8601 format.
	 */
	public String toISO8601()
	{
		SpcfStringBuilder buffer = SpcfStringBuilder.createInstance();
		//
		SpcfCalendar cal = this.toUtc();
		buffer.append(cal.getYear());
		buffer.append('-');
		//
		if(cal.getMonth()<10)
		{
			buffer.append('0');
		}
		buffer.append(cal.getMonth());
		//
		buffer.append('-');
		if(cal.getDay()<10)
		{
			buffer.append('0');
		}
		buffer.append(cal.getDay());
		//
		buffer.append('T');
		if(cal.getHour()<10)
		{
			buffer.append('0');
		}
		buffer.append(cal.getHour());
		//
		buffer.append(':');
		if(cal.getMinute()<10)
		{
			buffer.append('0');
		}
		buffer.append(cal.getMinute());
		//
		buffer.append(':');
		if(cal.getSecond()<10)
		{
			buffer.append('0');
		}
		buffer.append(cal.getSecond());
		//
		buffer.append('.');
		buffer.append(cal.getMillisecond());
		buffer.append('Z');
		return buffer.toString();
	}

	/**
	 * Returns true if the current object is in UTC time-zone.
	 * @return true if the current object is in UTC time-zone, else false.
	 */
	abstract public boolean isUTC();

	/**
	 * Returns true if the current object is in Local time-zone.
	 * @return true if the current object is in Local time-zone, else false.
	 */
	abstract public boolean isLocal();

	/**
	 * Returns a new SpcfCalendar object, representing the current objects's date/time in
	 * UTC time-zone.
	 * @return A new SpcfCalendar object in UTC time-zone.
	 */
	public SpcfCalendar toUtc()
	{
		return SpcfCalendar.createInstance(this.getTimeInMilliseconds());
	}

	/**
	 * Returns a new SpcfCalendar object, representing the current objects's date/time in
	 * Local time-zone.
	 * @return A new SpcfCalendar object in local time-zone.
	 */
	public SpcfCalendar toLocal()
	{
		return SpcfCalendar.createInstance(this.getTimeInMilliseconds(), SpcfTimeZone.getLocalTimeZone());
	}

	/**
	 * Adds the specified (signed) number of weeks based on the calendar's rules.
	 * It is equivalent to the call to the method - addDays(7 * weeks).
	 *
	 * @param weeks the number of weeks to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public void addWeeks(int weeks)
	{
		addDays(7 * weeks);
	}

	/**
	 * Adds the specified (signed) number of days based on the calendar's rules.
	 *
	 * @param days the number of days to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public abstract void addDays(int days);

	/**
	 * Adds the specified (signed) number of hours based on the calendar's rules.
	 *
	 * @param hours the number of hours to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public abstract void addHours(int hours);

	/**
	 * Adds the specified (signed) number of milliseconds based on the calendar's rules.
	 *
	 * @param milliseconds the number of milliseconds to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public abstract void addMilliseconds(int milliseconds);

	/**
	 * Adds the specified (signed) number of minutes based on the calendar's rules.
	 *
	 * @param minutes the number of minutes to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public abstract void addMinutes(int minutes);

	/**
	 * Adds the specified (signed) number of months based on the calendar's rules.
	 *
	 * @param months the number of months to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public abstract void addMonths(int months);

	/**
	 * Adds the specified (signed) number of seconds based on the calendar's rules.
	 *
	 * @param seconds the number of seconds to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public abstract void addSeconds(int seconds);

	/**
	 * Adds the specified (signed) number of years based on the calendar's rules.
	 *
	 * @param years the number of years to be added.
     * @throws SpcfIllegalArgumentException if the resulting date is out of range.
	 **/
	public abstract void addYears(int years);

	/**
	 * Compares the date/time encapsulated value.
	 *
	 * @param calendar the SpcfCalendar to be compared with this SpcfCalendar
	 * @return true if the current timeof this SpcfCalendar is after the time
	 * of SpcfCalendar calendar
	 * @throws SpcfClassCastException if SpcfCalendar calendar is not SpcfCalendarImpl
	 * @throws SpcfArgumentNullException if calendar is null
	 **/
	public abstract boolean after(SpcfCalendar calendar);

	/**
	 * Compares the date/time encapsulated value.
	 *
	 * @param calendar the SpcfCalendar to be compared with this SpcfCalendar
	 * @return true if the current timeof this SpcfCalendar is before the time
	 * of SpcfCalendar calendar
	 * @throws SpcfClassCastException if SpcfCalendar calendar is not SpcfCalendarImpl
	 * @throws SpcfArgumentNullException if calendar is null
	 **/
	public abstract boolean before(SpcfCalendar calendar);

    /**
     * Compares this object with the two argument dates
     *
     * @param beginningDate - date before or equal to this
     * @param endingDate - date after or equal to this
     * @return returns true if this is after or equal to the beginning date
     * and before or equal to the ending date
     */
    public abstract boolean between(SpcfCalendar beginningDate, SpcfCalendar endingDate);

	/**
	* Compares this object with the specified object for order.
	*
	* @param o value to be compared with
	* @return the value 0 if the argument Calendar is equal to this Calendar;
	* a value less than 0 if this Calendar is before the Calendar argument;
	* and a value greater than 0 if this Calendar is after the Calendar argument
	* @throws SpcfClassCastException if Object o is not SpcfCalendar
	* @throws SpcfArgumentNullException if o is null
	*/
	public abstract int compareTo(Object o);

	/**
	 * Compares this calendar to the specified object.
	 *
	 * @param val - the object to compare with.
	 * @return  true if the objects are the same; false otherwise.
 	 **/
	@Override
	public abstract boolean equals(Object val);

	/**
	 * Converts the value of this instance to the format of the pattern.
	 * <p>
	 * See SpcfDateFormat for portable pattern formats.
	 * See the SimpleDateFormat class on the java platform for a definition
	 * of the valid patterns on the java platform.
	 * See the DateTimeFormatInfo class on the
	 * dotnet platform for a definition of the valid patterns on the
	 * dotnet platform.
	 * </p>
	 * <p>
	 * <br>If dateString contains only a time, and no date, then January 1, 1970 is used.
	 * <br>If dateString contains only a date and no time, midnight (00:00:00) is used.
	 * </p>
	 *
	 * @param pattern - the new date and time pattern for this format.
	 * @return  string the formatted date-time string
	 * @throws SpcfArgumentNullException if pattern is null
	 * @throws SpcfIllegalArgumentException if pattern is an empty string
	 * @throws SpcfFormatException the given pattern is invalid
 	 **/
	public abstract String format(String pattern);

	/**
	 * Returns the day of the month.
	 *
	 * @return  the day of the month.
	 **/
	public abstract int getDay();

	/**
	 * Returns a hash code for this calendar.
	 *
	 * @return  a hash code value for this object.
 	 **/
	@Override
	public abstract int hashCode();

	/**
	 * Returns the hour value.
	 *
	 * @return  the hour value.
	 **/
	public abstract int getHour();

	/**
	 * Returns the millisecond value.
	 *
	 * @return  the millisecond value.
	 **/
	public abstract int getMillisecond();

	/**
	 * Returns the minute value.
	 *
	 * @return  the minute value.
	 **/
	public abstract int getMinute();

	/**
	 * Returns the month value.
	 *
	 * @return  the month value (1 - 12).
	 **/
	public abstract int getMonth();

	/**
	 * Returns the second value.
	 *
	 * @return  the second value.
	 **/
	public abstract int getSecond();

	/**
	 * Returns the date in milliseconds.
	 *
	 * @return  the number of milliseconds in the date since 1970/01/01.
	 **/
	public abstract long getTimeInMilliseconds();

	/**
	 * Returns the year value.
	 *
	 * @return  the year value.
	 **/
	public abstract int getYear();

	/**
	 * Returns the day of year value.
	 *
	 * @return  the day of year value.
	 **/
	public abstract int getDayOfYear();

	/**
	 * Returns the day of week value.  The integer values start at 1 with Sunday,
	 * 2 is Monday, 3 is Tuesday, 4 is Wednesday, 5 is Thursday, 6 is Friday,
	 * 7 is Saturday.
	 *
	 * @return  the day of week value.
	 **/
	public abstract int getDayOfWeek();

	/**
	 * Returns the number of days in the current month.
	 *
	 * @return  the maximum number of days of month for the current time instance.
	 **/
	public abstract int getDaysInMonth();

	/**
	 * Returns the number of days in the current year.
	 *
	 * @return  the maximum number of days in the year for the current time instance.
	 **/
	public abstract int getDaysInYear();

	/**
	 * Sets the values for the fields year, month, day, hour, minute, second
	 * and millisecond.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
	 **/
	public abstract void setValues(int year, int month, int day, int hour, int minute, int second, int millisecond);

	/**
	 * Sets the values for the fields year, month, day, hour, minute, second
	 * and millisecond.  The time of day is set to midnight (00:00:00).
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
	 **/
	public abstract void setValues(int year, int month, int day);

	/**
	 * Returns the number of milliseconds difference between the instance
	 * and the parameter value.
	 *
	 * @param calendar the date to compare.
	 * @return  the number of milliseconds the two dates differ value.
 	 **/
	public abstract long subtract(SpcfCalendar calendar);

	/**
	 * Returns a string representation of this calendar.  This method
	 * is intended to be used only for debugging purposes, and the
	 * format of the returned string may vary between implementations.
	 * The returned string may be empty but may not be null.
	 *
	 * @return  a string representation of this calendar.
	 **/
	@Override
	public abstract String toString();

	/**
	 * Returns the java.util.Date instance,
	 * convert SpcfCalendar into Date.
	 *
	 * @return java.util.Date object
	 **/
	public abstract Date toDate();

	/**
	 * Creates and returns a copy of this object.
	 * @return  a copy of this object.
	 */
	public abstract SpcfCalendar copy();

	public abstract DateTime toDateTime();

	public abstract LocalDate toLocalDate();

	/**
	 * Constructs an empty calendar object with the current
	 * UTC date/time.
	 *
	 * @return an SpcfCalendar implementation object
	 */
	public static SpcfCalendar createInstance()
	{
		return SpcfFactory.getInstance().createCalendar();
	}

	/**
	 * Constructs a GregorianCalendar object with the current date and
	 * time with the given time-zone.
	 * @param zone the time-zone to be used.
	 * @return an SpcfCalendar implementation object
	 * @throws SpcfArgumentNullException if zone is null
	 * @throws SpcfIllegalArgumentException if zone is not in local time-zone
	 */
	public static SpcfCalendar createInstance(SpcfTimeZone zone)
	{
		return SpcfFactory.getInstance().createCalendar(zone);
	}

	/**
	 * Constructs a calendar object with the specified date and
	 * TimeZone of UTC.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
	 */
	public static SpcfCalendar createInstance(int year, int month, int day,
			int hour, int minute, int second, int millisecond)
	{
		return SpcfFactory.getInstance().createCalendar(year, month, day, hour, minute, second, millisecond);
	}

	/**
	 * Constructs a GregorianCalendar object with the specified date and
	 * time zone.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param hour the hour (0 - 23).
	 * @param minute the minute (0 - 59).
	 * @param second the second (0 - 59).
	 * @param millisecond the millisecond (0 - 999).
	 * @param zone the time-zone.
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     * are out of range or if zone is not in local time-zone.
     * @throws SpcfArgumentNullException if zone is null
	 */
	public static SpcfCalendar createInstance(int year, int month, int day,
			int hour, int minute, int second, int millisecond, SpcfTimeZone zone)
	{
		return SpcfFactory.getInstance().createCalendar(year, month, day, hour, minute, second, millisecond, zone);
	}

	/**
	 * Constructs a calendar object with the specified date and
	 * TimeZone of UTC.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     *  are out of range.
 	 */
	public static SpcfCalendar createInstance(int year, int month, int day)
	{
		return SpcfFactory.getInstance().createCalendar(year, month, day);
	}

	/**
	 * Constructs a GregorianCalendar object with the specified date and time-zone.
	 *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @param zone the time zone.
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if any of the specified parameters
     * are out of range or if zone is not in local time-zone.
     * @throws SpcfArgumentNullException if zone is null
 	 */
	public static SpcfCalendar createInstance(int year, int month, int day, SpcfTimeZone zone)
	{
		return SpcfFactory.getInstance().createCalendar(year, month, day, zone);
	}

	/**
	 * Constructs a calendar object with the specified milliseconds in the
	 * TimeZone of UTC.
	 *
	 * @param milliseconds the number of milliseconds since 1/1/1970, can be a negative number.
	 * @return an SpcfCalendar implementation object
     * @throws SpcfIllegalArgumentException if the specified parameter
     *  is out of range.
 	 */
	public static SpcfCalendar createInstance(long milliseconds)
	{
		return SpcfFactory.getInstance().createCalendar(milliseconds);
	}

	/**
	 * Constructs a GregorianCalendar object with the specified milliseconds in the
	 * given time-zone.
	 * @param milliseconds the number of milliseconds since 1/1/1970, can be a negative number.
	 * @param zone the timezone to be used
	 * @return an SpcfCalendar implementation object
	 * @throws SpcfIllegalArgumentException if the specified parameter
     * is out of range or if zone is not in local time-zone.
     * @throws SpcfArgumentNullException if zone is null
	 */
	public static SpcfCalendar createInstance(long milliseconds, SpcfTimeZone zone)
	{
		return SpcfFactory.getInstance().createCalendar(milliseconds, zone);
	}

	public static Date toDate(SpcfCalendar spcfCalendar) {
		if (Objects.isNull(spcfCalendar)) {
			return null;
		}
		return spcfCalendar.toDate();
	}

	public static DateTime toDateTime(SpcfCalendar spcfCalendar) {
		if(Objects.isNull(spcfCalendar)) {
			return null;
		}
		return spcfCalendar.toDateTime();
	}

	public static LocalDate toLocalDate(SpcfCalendar spcfCalendar) {
		if(Objects.isNull(spcfCalendar)) {
			return null;
		}
		return spcfCalendar.toLocalDate();
	}

	public static String toDateLiteral(SpcfCalendar spcfCalendar) {
		if(Objects.isNull(spcfCalendar)) {
			return null;
		}
		SpcfCalendar truncDate = SpcfCalendar.createInstance(spcfCalendar.getYear(), spcfCalendar.getMonth(), spcfCalendar.getDay(), SpcfTimeZone.getLocalTimeZone());
		String date = String.format(DATE_TEMPLATE,
				truncDate.getYear(), truncDate.getMonth(), truncDate.getDay());

		return date;
	}

}
