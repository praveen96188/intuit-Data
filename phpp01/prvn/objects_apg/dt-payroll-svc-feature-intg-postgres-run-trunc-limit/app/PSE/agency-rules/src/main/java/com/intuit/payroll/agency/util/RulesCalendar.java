//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction
// is a violation of applicable law. This material contains certain
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.util;

//import com.intuit.spc.foundations.portability.util.SpcfCalendar;
//import com.intuit.spc.foundations.portability.SpcfRuntimeException;
//import com.intuit.spc.foundations.portability.collections.SpcfCollectionIterable;
import com.intuit.payroll.agency.impl.PaymentPeriod;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.GregorianCalendar;

/// <summary>
///	IMMUTABLE to match behavior of .net DateTime
/// 
/// follow .net, not Java/SPCF convention for days of week and month numbers, since desktop has existing data in this format
/// that means Sunday is 0, Saturday is 6, January is 1, December is 12.
/// </summary>
public class RulesCalendar {

	// follow .net, not Java/SPCF convention since desktop has existing data in this format
	public static final int Sunday = 0;
	public static final int Monday = 1;
	public static final int Tuesday = 2;
	public static final int Wednesday = 3;
	public static final int Thursday = 4;
	public static final int Friday = 5;
	public static final int Saturday = 6;

    // GregorianCalendar factory object
    private static final DatatypeFactory DATATYPE_FACTORY;
    static {
        try {
            DATATYPE_FACTORY = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("datatype factory problem");
        }
    }

	static public RulesCalendar createCalendar ()
	{
		return new RulesCalendar();
	}

	static public RulesCalendar createCalendar (RulesCalendar that)
	{
		return new RulesCalendar(that);
	}

    static public RulesCalendar createCalendar (int year, int month, int day)
    {
        return new RulesCalendar(year, month, day);
    }

    static public RulesCalendar createCalendar (int year, int month, int day, int hour, int minute, int second)
    {
        return new RulesCalendar(year, month, day, hour, minute, second);
    }

	static public RulesCalendar createCalendar (String xml)
	{
		return new RulesCalendar(xml);
	}

    protected GregorianCalendar m_calendar;

    protected RulesCalendar()
    {
        m_calendar = new GregorianCalendar();
    }

    protected RulesCalendar(RulesCalendar that)
    {
        m_calendar = (GregorianCalendar)that.m_calendar.clone();
    }

    protected RulesCalendar(int year, int month, int day)
    {
        m_calendar = new GregorianCalendar(year, month-1, day);
    }

    protected RulesCalendar(int year, int month, int day, int hour, int minute, int second)
    {
        m_calendar = new GregorianCalendar(year, month-1, day, hour, minute, second);
    }

    protected RulesCalendar(String xml)
    {
		XMLGregorianCalendar xmlCal = DATATYPE_FACTORY.newXMLGregorianCalendar(xml);
		m_calendar = xmlCal.toGregorianCalendar();
    }

    public int getDaysInMonth()
    {
		return m_calendar.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    }

    public int getMonth()
    {
        return m_calendar.get(GregorianCalendar.MONTH)+1;
    }													

    public int getYear()
    {
        return m_calendar.get(GregorianCalendar.YEAR);
    }

    public int getDay()
    {
        return m_calendar.get(GregorianCalendar.DATE);
    }

    public int getDayOfWeek()
    {
        return m_calendar.get(GregorianCalendar.DAY_OF_WEEK)-1; // .net convention!
    }

    public boolean after(RulesCalendar that)
    {
        return m_calendar.after(that.m_calendar); 
    }

    public boolean before(RulesCalendar that)
    {
        return m_calendar.before(that.m_calendar);
    }

    public int compareTo(RulesCalendar that)
    {
        return m_calendar.compareTo(that.m_calendar);
    }

    public int getHour()
    {
        return m_calendar.get(GregorianCalendar.HOUR_OF_DAY);
    }

    public int getMinute()
    {
        return m_calendar.get(GregorianCalendar.MINUTE);
    }

    public int getSecond()
    {
        return m_calendar.get(GregorianCalendar.SECOND);
    }

    public RulesCalendar addDays(int days)
    {
		RulesCalendar result = RulesCalendar.createCalendar (this);
		result.m_calendar.add(GregorianCalendar.DATE, days);
        return result;
    }

    public RulesCalendar addWeeks(int weeks)
    {
        RulesCalendar result = RulesCalendar.createCalendar (this);
		result.m_calendar.add(GregorianCalendar.DATE, weeks*7);
        return result;
    }

    public RulesCalendar addMonths(int months)
    {
        RulesCalendar result = RulesCalendar.createCalendar (this);
		result.m_calendar.add(GregorianCalendar.MONTH, months);
        return result;
    }

    public RulesCalendar addYears(int years)
    {
        RulesCalendar result = RulesCalendar.createCalendar (this);
		result.m_calendar.add(GregorianCalendar.YEAR, years);
        return result;
    }

    /// <summary>
    /// Evaluates whether the provided date falls on
    /// a weekend day.
    /// </summary>
    /// <returns>True if day is Saturday or Sunday, false otherwise.</returns>
    public boolean isWeekend() {
        return getDayOfWeek() == RulesCalendar.Saturday || getDayOfWeek() == RulesCalendar.Sunday;
    }

    /// <summary>
    /// Returns boolean value based on whether the day is
    /// a week day (monday-friday) or not.
    /// </summary>
    /// <returns>
    /// True if the day is a weekday day, false if it is a weekend.
    /// </returns>
    public boolean isWeekDay()
    {
        return !isWeekend();
    }

    public long subtract (RulesCalendar that)
    {
        return m_calendar.getTimeInMillis() - that.m_calendar.getTimeInMillis();
    }
    /// <summary>
    /// Adjusts the date provided (if a weekend) according to
    /// the policy supplied.
    /// </summary>
    /// <param name="weekendDay">The weekend day to adjust.</param>
    /// <param name="policy">The policy defining how to adjust the date returned (forward,backward, etc.)</param>
    /// <returns>The adjusted date according to the supplied policy. Will be either a Monday or Friday.</returns>
    public RulesCalendar applyWeekendRollingPolicy(DueDateRollingPolicy policy) {
        RulesCalendar result = RulesCalendar.createCalendar (this);
        // If the policy is none, get oot of heah.
        if (policy.getWeekendDateRollPolicy() == DateRollingPolicy.None) { return result; }

        // If this ain't a weekend, get oot of heah.
        if(!isWeekend()) { return result; }

        // Otherwise we have work.
        int dayOfWeek = getDayOfWeek();
        switch(dayOfWeek) {
            case RulesCalendar.Saturday:
                if(policy.getWeekendDateRollPolicy() == DateRollingPolicy.Forward) {
                    return result.addDays(2);
                }
                else if(policy.getWeekendDateRollPolicy() == DateRollingPolicy.Backward) {
                    return result.addDays(-1);
                }
                break;
            case RulesCalendar.Sunday:
                if(policy.getWeekendDateRollPolicy() == DateRollingPolicy.Forward) {
                    return result.addDays(1);
                }
                else if(policy.getWeekendDateRollPolicy() == DateRollingPolicy.Backward) {
                    return result.addDays(-2);
                }
                break;
        }
//        throw new ArgumentOutOfRangeException("No valid date policy specified, or day is not a weekend.");
        throw new RuntimeException("No valid date policy specified, or day is not a weekend.");
//        throw new SpcfRuntimeException("No valid date policy specified, or day is not a weekend.");
    }

    /// <summary>
    /// Returns boolean value based on whether the day is a business day (monday-friday) or not.
    /// </summary>
    /// <param name="day">The day to test.</param>
    /// <param name="holidayList">A List of Agency holidays.</param>
    /// <returns>
    /// True if the day is a business day, false if it is a weekend or holiday.
    /// </returns>
    public boolean isBusinessDay(Iterable<IAgencyHoliday> holidayList)
//    public boolean isBusinessDay(SpcfCollectionIterable<IAgencyHoliday> holidayList)
    {
        return !isWeekend() && !isHoliday(holidayList);
    }

    /// <summary>
    /// get a date that is a given number of business days after the passed-in date.
    /// </summary>
    /// <param name="startDate">The date to start counting business days from.</param>
    /// <param name="businessDays">The number of business days to count.</param>
    /// <returns>A RulesCalendar representing a day the given number of business days after the given
    /// start date.</returns>
    public RulesCalendar addBusinessDays(int businessDays)
    {
        RulesCalendar result = RulesCalendar.createCalendar(this);
        DueDateRollingPolicy policy = new DueDateRollingPolicy();
        policy.setWeekendDateRollPolicy(DateRollingPolicy.Forward);

        for(int count = 0; count < businessDays; count++)
        {
            result = result.addDays(1);
            if(result.isWeekend())
            {
                result = result.applyWeekendRollingPolicy(policy);
            }
        }
        return result;
    }

    /// <summary>
    /// get a date that is a given number of business
    /// days after the passed-in date.
    /// </summary>
    /// <param name="startDate">The date to start
    /// counting business days from.</param>
    /// <param name="businessDays">The number of business
    /// days to count.</param>
    /// <param name="holidayList">A list of applicable holidays</param>
    /// <returns>A RulesCalendar representing a day the
    /// gievn number of business days after the given
    /// start date.</returns>
    public RulesCalendar addBusinessDays(int businessDays, Iterable<IAgencyHoliday> holidayList)
//    public RulesCalendar addBusinessDays(int businessDays, SpcfCollectionIterable<IAgencyHoliday> holidayList)
    {
        RulesCalendar result = RulesCalendar.createCalendar(this);
        DueDateRollingPolicy policy = new DueDateRollingPolicy();
        policy.setWeekendDateRollPolicy(DateRollingPolicy.Forward);
        policy.setHolidayDateRollPolicy(DateRollingPolicy.Forward);

        for(int count = 0; count < businessDays; count++)
        {
            result = result.addDays(1);
            if(result.isWeekend())
            {
                result = result.applyWeekendRollingPolicy(policy);
                if (result.isHoliday(holidayList))
                {
                    result = result.applyHolidayRollingPolicy(holidayList, policy);
                }
            }
            else if (result.isHoliday(holidayList))
            {
                result = result.applyHolidayRollingPolicy(holidayList, policy);
            }
        }
        return result;
    }

    /// <summary>
    /// get a date that is a given number of business days after the passed-in date.
    /// </summary>
    /// <param name="startDate">The date to start counting business days from.</param>
    /// <param name="businessDays">The number of business days to count.</param>
    /// <param name="holidayList">A list of applicable holidays</param>
    /// <returns>A RulesCalendar representing a day the given number of business days after the given
    /// start date.</returns>
    public RulesCalendar subtractBusinessDays(int businessDays, Iterable<IAgencyHoliday> holidayList)
//    public RulesCalendar subtractBusinessDays(int businessDays, SpcfCollectionIterable<IAgencyHoliday> holidayList)
    {
        RulesCalendar result = RulesCalendar.createCalendar (this);
        DueDateRollingPolicy policy = new DueDateRollingPolicy();
        policy.setWeekendDateRollPolicy(DateRollingPolicy.Backward);
        policy.setHolidayDateRollPolicy(DateRollingPolicy.Backward);

        for(int count = 0; count < businessDays; count++)
        {
            result = result.addDays(-1);
            if(result.isWeekend())
            {
                result = result.applyWeekendRollingPolicy(policy);
                if (result.isHoliday(holidayList))
                {
                    result = result.applyHolidayRollingPolicy(holidayList, policy);
                }
            }
            else if (result.isHoliday(holidayList))
            {
                result = applyHolidayRollingPolicy(holidayList, policy);
            }
        }
        return result;
    }

    /// <summary>
    /// Determines whether the date provided is a holiday in the list.
    /// </summary>
    /// <param name="day">The day in question.</param>
    /// <param name="holidayList">The list of holidays we wish to check against.</param>
    /// <returns>True if day is in the list of holidays, false if not.</returns>
    public boolean isHoliday(Iterable<IAgencyHoliday> holidayList)
//    public boolean isHoliday(SpcfCollectionIterable<IAgencyHoliday> holidayList)
    {
        if (holidayList == null)
        {
            return false;
        }
        for (IAgencyHoliday holiday: holidayList)
        {
            if (sameDay(holiday.getHolidayDate()))
            {
                return true;
            }
        }
        return false;
    }

    public boolean sameDay (RulesCalendar that)
    {
        return getDay() == that.getDay() && getMonth() == that.getMonth() && getYear() == that.getYear();
    }

    /// <summary>
    /// Adjusts the date passed in based on the policy provided. If
    /// the date passed in is not in the list of holidays contained
    /// by the date policy, then the date is returned unadjusted. If
    /// the adjustment causes the date to fall on a weekend, it will
    /// continue to adjust the date until one of two things happens:
    /// <list>
    /// <item>It finds a date that is not a weekend and is not
    /// a holiday. It will then return that date.</item>
    /// <item>It finds that after applying weekend and holiday policy
    /// it ends up back on the same date.  In that case an exception
    /// of type ApplicationException() will be thrown to avoid an
    /// infinite loop.</item>
    /// </list>
    /// </summary>
    /// <param name="holiday">
    /// The holiday to be modified (assumed to already be verified
    /// as a holiday against the supplied date policy.
    /// </param>
    /// <param name="holidayList">The List of Holidays.</param>
    /// <param name="policy">
    /// The policy containing the list of holidays and the policy by which
    /// to adjust the date.
    /// </param>
    /// <returns>The adjusted date based on the list of holidays and supplied policy.</returns>
    public RulesCalendar applyHolidayRollingPolicy(Iterable<IAgencyHoliday> holidayList, DueDateRollingPolicy policy)
//    public RulesCalendar applyHolidayRollingPolicy(SpcfCollectionIterable<IAgencyHoliday> holidayList, DueDateRollingPolicy policy)
    {
        RulesCalendar result = RulesCalendar.createCalendar (this);
        DateRollingPolicy holidayPolicy = policy.getHolidayDateRollPolicy();
        do
        {
            if (holidayPolicy==DateRollingPolicy.Forward)
            {
                result = result.addDays(1);
                if(result.isWeekend())
                {
                    result = result.applyWeekendRollingPolicy(policy);
                    if(compareTo(result) == 0)
                    {
                        throw new RuntimeException("Holiday and Weekend date policies conflict. Cannot resolve due date.");
//                        throw new SpcfRuntimeException("Holiday and Weekend date policies conflict. Cannot resolve due date.");
                    }
                }
            }

            if (holidayPolicy==DateRollingPolicy.Backward)
            {
                result = result.addDays(-1);
                if(result.isWeekend())
                {
                    result = result.applyWeekendRollingPolicy(policy);
                    if(compareTo(result) == 0)
                    {
                        throw new RuntimeException("Holiday and Weekend date policies conflict. Cannot resolve due date.");
//                        throw new SpcfRuntimeException("Holiday and Weekend date policies conflict. Cannot resolve due date.");
                    }
                }
            }
        } while(result.isHoliday(holidayList));
        return result;
    }


    public String toString()
    {
        // ah, now the debugger shows me something readable :)
        return toString("yyyy/MM/dd HH:mm:ss.S");
                
    }
    public String toString(String format)
	{	
		return toString(new SimpleDateFormat(format));
	}
    
    public String toString(SimpleDateFormat simpleDateFormat)
    {
    try
    {
        {
            return simpleDateFormat.format(m_calendar.getTime());
        }
    }
    catch (IllegalArgumentException e)
    {
        throw e;
    }
    }

    TimeZone getTimeZone ()
    {
        return m_calendar.getTimeZone();
    }

	public String parseUIString(String uiString)
	{
		if (uiString == null || uiString.trim().length() == 0)
		{
			return "";
		}

		int startBrace = uiString.indexOf('{');
		int endBrace = uiString.indexOf('}');
		if (startBrace >= 0)
		{
//			int diff = endBrace - startBrace;
			// With the braces
//			String formatCode = uiString.substring(startBrace, diff +1);
			String formatCode = uiString.substring(startBrace, endBrace +1);

			// Sans Braces
//			String format = formatCode.substring(formatCode.indexOf('{') + 1, diff -1);
			String format = formatCode.substring(formatCode.indexOf('{') + 1, formatCode.indexOf('}'));

//			String formattedDate = date.toString(format);
			String formattedDate = toString(new SimpleDateFormat(format));

			// put it together
            return uiString.replace(formatCode, formattedDate);
		}
		else
		{
			return uiString;
		}
	}

	/// <summary>
	/// Finds a PaymentPeriod that contains the accrual date.  Returns Null if not found.
	/// </summary>
	/// <param name="periods">The PaymentPeriods to look through.</param>
	/// <param name="accrualDate">The Date to hunt for.</param>
	/// <returns>A PaymentPeriod or null if none are found.</returns>
	public PaymentPeriod findInPaymentPeriods(Iterable<PaymentPeriod> periods)
	{
        for (PaymentPeriod period : periods) {
            int startCompare = compareTo(period.getFromAccrualDate());
            int endCompare = compareTo(period.getToAccrualDate());
            if (startCompare >= 0 && endCompare <= 0) {
                return period;
            }

        }
        return null;
	}

    public boolean equals(Object that)
    {
        if (that instanceof RulesCalendar)
        {
            RulesCalendar thatCal = ((RulesCalendar)that);
            return m_calendar.equals(thatCal.m_calendar);
        }
        else
        {
            return false;
        }
    }

	public RulesCalendar trimTime ()
	{
        RulesCalendar result = RulesCalendar.createCalendar (this);
		result.m_calendar.set(GregorianCalendar.HOUR_OF_DAY, 0);
		result.m_calendar.set(GregorianCalendar.MINUTE, 0);
		result.m_calendar.set(GregorianCalendar.SECOND, 0);
        result.m_calendar.set(GregorianCalendar.MILLISECOND, 0);

        return result;
	}
}


