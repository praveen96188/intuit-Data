package com.intuit.spc.foundations.portability.util;

import com.intuit.spc.foundations.portability.SpcfFactory;


/**
 * SpcfTimeZone represents a time zone offset, and also figures out daylight savings.
 * Typically, you get a SpcfTimeZone using getLocalTimeZone which creates a SpcfTimeZone
 * based on the time-zone where the program is running. For example, for a program
 * running in US/Eastern, getLocalTimeZone creates a SpcfTimeZone object based on
 * US Eastern Time.
 */
abstract public class SpcfTimeZone
{
	private static SpcfTimeZone sLocalTimeZone = SpcfFactory.getInstance().createLocalTimeZone();

	/**
	 * Gets local system time-zone.
	 */
	public static SpcfTimeZone getLocalTimeZone()
	{
		return sLocalTimeZone;
	}

	/**
	 * Gets the standard time zone name.
	 * @return The standard time zone name.
	 */
	public abstract String getStandardName();

	/**
	 * Gets the daylight saving time zone name.
	 * If daylight saving time is not used in the time zone, an empty string ("") is returned.
	 * @return The daylight saving time zone name.
	 */
	public abstract String getDaylightName();

	/**
	 * Returns the offset of this time zone from UTC at the specified
     * date. If Daylight Saving Time is in effect at the specified
     * date, the offset value is adjusted with the amount of daylight
     * saving.
     *
	 * @param year the year (1600 - 9999).
	 * @param month the month (1 - 12).
	 * @param day the day (1 - number of days in month).
	 * @return the amount of time in milliseconds to add to UTC to get time in this time-zone.
	 * @throws SpcfIllegalArgumentException if any of the specified parameters
     * are out of range.
	 */
	public int getOffset(int year, int month, int day)
	{
		SpcfCalendar cal = SpcfCalendar.createInstance(year, month, day, this);
		return getOffset(cal.getTimeInMilliseconds());
	}

	/**
	 * Returns the offset of this time zone from UTC at the specified
     * date. If Daylight Saving Time is in effect at the specified
     * date, the offset value is adjusted with the amount of daylight
     * saving.
     *
	 * @param date the number of milliseconds since 1/1/1970, can be a negative number.
	 * @return the amount of time in milliseconds to add to UTC to get time in this time-zone.
	 * @throws SpcfIllegalArgumentException if date is out of range.
	 */
	public abstract int getOffset(long date);

	/**
	 * Returns user friendly time-zone name.
	 * @return user friendly time-zone name.
	 */
	public String toString()
	{
		return getStandardName();
	}

    /**
     * set the timezone separately from the locale
     * @param id a valid timezone id
     */
    public abstract void setTimeZone(String id);
}

