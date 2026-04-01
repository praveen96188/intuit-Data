package com.intuit.spc.foundations.portability.text;
import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.util.*;

/**
 * A class for date/time formatting (i.e., date -&gt; text) and
 * parsing (text -&gt; date). SpcfDateFormat helps you to format and parse dates
 * for any locale. Your code can be completely independent of the geographical
 * and cultural conventions for months and days of the week. The format
 * characters in the table below are consistent across target platforms. These
 * characters can be combined to construct custom patterns. The patterns are
 * case sensitive, and text can be quoted using single quotes to avoid
 * interpretation. Use of other platform specific format characters, such as G
 * are not allowed.
 * <p>
 * <pre>
 * SPC-F Custom Pattern     Description                                                  Sample
 * ======================================================================================================
 * y, yy                    Year without the century                                     05
 * ------------------------------------------------------------------------------------------------------
 * yyyy                     Year in 4 digits including the century                       2005
 * ------------------------------------------------------------------------------------------------------
 * M, MM                    Numeric month                                                1, 10
 * ------------------------------------------------------------------------------------------------------
 * MMM                      Abbreviated month name                                       Oct
 * ------------------------------------------------------------------------------------------------------
 * MMMM                     Full name of the month                                       October
 * ------------------------------------------------------------------------------------------------------
 * d, dd                    Numeric day of the month                                     1, 10
 * ------------------------------------------------------------------------------------------------------
 * h, hh                    Hour in day, 1-12                                            1, 10 
 * ------------------------------------------------------------------------------------------------------
 * H, HH                    Hour in day, 0-23                                            1, 15
 * ------------------------------------------------------------------------------------------------------
 * m,mm                     Minute in hour                                               1, 10                                               
 * ------------------------------------------------------------------------------------------------------
 * s, ss                    Second in minute                                             1, 10
 * ------------------------------------------------------------------------------------------------------
 * E                        Abbreviated day in a week                                    Fri
 * ------------------------------------------------------------------------------------------------------
 * EEEE                     Full name of the day in a week                               Friday
 * ------------------------------------------------------------------------------------------------------
 * a                        AM/PM designator                                             AM, PM
 * ------------------------------------------------------------------------------------------------------
 * S                        Milliseconds                                                 1,10,100,999
 * ------------------------------------------------------------------------------------------------------
 * 
 * Following standard SPC-F date formats are supported.
 * SPC-F Standard Pattern   Description                         Sample
 * ====================================================================================================== 
 * d                        Short Date                          1/3/2002
 * ------------------------------------------------------------------------------------------------------
 * D                        Long Date                           Thursday, January 03, 2002
 * ------------------------------------------------------------------------------------------------------
 * f                        Full: Long date and short time        Thursday, January 03, 2002 12:00 AM
 * ------------------------------------------------------------------------------------------------------
 * F                        Full: Long date and long time         Thursday, January 03, 2002 12:00:00 AM
 * ------------------------------------------------------------------------------------------------------
 * g                        General: Short date and short time    1/3/2002 12:00 AM
 * ------------------------------------------------------------------------------------------------------
 * G                        General: Short date and long time     1/3/2002 12:00:00 AM
 * ------------------------------------------------------------------------------------------------------
 * r, R                     RFC1123                             Thu, 03 Jan 2002 00:00:00 GMT
 * ------------------------------------------------------------------------------------------------------
 * s, S                     ISO8601                             2002-01-03T00:00:00.0Z
 * ------------------------------------------------------------------------------------------------------
 * t                        Short time                          12:00 AM
 * ------------------------------------------------------------------------------------------------------
 * T                        Long time                           12:00:00 AM
 * ------------------------------------------------------------------------------------------------------
 * </pre>
 * <p>
 * <a href="http://rfc.net/rfc1766.html"> RFC-1766</a>
 * <a href="http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt"> ISO-639</a>
 * <a href="http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html"> ISO-3166</a>
 * 
 * @see   SpcfDateEnum
 * @see   SpcfTimeEnum
 * @see   SpcfDateTimeEnum
 * @see   SpcfLocaleInfo
 */
public abstract class SpcfDateFormat
{
	/**
	 * Formats a date into a date/time string.
	 * @param date The date/time value to be formatted into a date string.
	 * @param dateEnum The given formatting style.
	 * @return The formatted date string.
	 * @throws SpcfArgumentNullException if dateEnum or date is null
	 * @throws SpcfFormatException if the specified pattern is invalid
	 * @throws SpcfClassCastException if the encapsulated 3rd party object cannot be extracted
	 * @throws SpcfClassCastException if the encapsulated format is an unexpected type (a valid example is Integer)
	 * @throws SpcfIllegalArgumentException if the encapsulated format has an unexpected value (a valid example would be DateFormat.SHORT)
	 */
	public static String format(SpcfCalendar date, SpcfDateEnum dateEnum)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(dateEnum);
		return dateFormat.format(date);
	}

	/**
	 * Formats a Date into a date/time string.
	 * @param date The date/time value to be formatted into a time string.
	 * @param timeEnum The given formatting style.
	 * @return The formatted time string.
	 * @throws SpcfArgumentNullException if timeEnum or date is null
	 * @throws SpcfFormatException if the specified pattern is invalid
	 * @throws SpcfClassCastException if the encapsulated 3rd party object cannot be extracted
	 * @throws SpcfClassCastException if the encapsulated format is an unexpected type (a valid example is Integer)
	 * @throws SpcfIllegalArgumentException if the encapsulated format has an unexpected value (a valid example would be DateFormat.SHORT)
	 */
	public static String format(SpcfCalendar date, SpcfTimeEnum timeEnum)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(timeEnum);
		return dateFormat.format(date);
	}

	/**	 
	 * Formats a Date into a date/time string. The resulting date-time pattern is made by combining dateEnum 
	 * and timeEnum patterns seperated by single blank space. <br/>
	 * As an example, if dateEnum is LongFormat and timeEnum is ShortFormat, then the resulting format will be like
	 * "Long_Date_Format Short_TimeFormat", e.g. "Thursday, January 03, 2002 12:00 AM".
	 * @param date The date/time value to be formatted into a date/time string.
	 * @param dateEnum The given date formatting style.
	 * @param timeEnum The given time formatting style.
	 * @return The formatted date/time string.
	 * @throws SpcfArgumentNullException if dateEnum or timeEnum or date is null
	 * @throws SpcfFormatException if the specified pattern is invalid
	 * @throws SpcfClassCastException if the encapsulated 3rd party object cannot be extracted
	 * @throws SpcfClassCastException if the encapsulated format is an unexpected type (a valid example is Integer)
	 * @throws SpcfIllegalArgumentException if the encapsulated format has an unexpected value (a valid example would be DateFormat.SHORT)
	 */
	public static String format(SpcfCalendar date, SpcfDateEnum dateEnum, SpcfTimeEnum timeEnum)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(dateEnum, timeEnum);
		return dateFormat.format(date);
	}

	/**
	 * Formats a Date into a date/time string.
	 * @param date The date/time value to be formatted into a date/time string.
	 * @param dateTimeEnum The given date/time formatting style.
	 * @return The formatted date/time string.
	 * @throws SpcfArgumentNullException if dateTimeEnum or date is null
	 * @throws SpcfFormatException if the specified pattern is invalid
	 * @throws SpcfClassCastException if the encapsulated 3rd party object cannot be extracted
	 * @throws SpcfClassCastException if the encapsulated format is an unexpected type (the valid type is String)
	 */
	public static String format(SpcfCalendar date, SpcfDateTimeEnum dateTimeEnum)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(dateTimeEnum);
		return dateFormat.format(date);
	}

	/**
	 * Formats a Date into a custom date/time string.
	 * @param date the date/time value to be formatted into a date/time string.
	 * @param pattern a custom format string
	 * @return the formatted date/time string.
	 * @throws SpcfArgumentNullException if pattern or date is null
	 * @throws SpcfArgumentOutOfRangeException if pattern is an empty string
	 * @throws SpcfFormatException if the specified pattern is invalid
	 * @throws SpcfClassCastException if the encapsulated 3rd party object cannot be extracted
	 */
	public static String format(SpcfCalendar date, String pattern)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(pattern);
		return dateFormat.format(date);
	}

	/**
	 * Parses text from the beginning of the given string to produce an
	 * SpcfCalendar (date).
	 * @param text The date/time string to be parsed.
	 * @param dateEnum The given date formatting style.
	 * @return An SpcfCalendar parsed from the string
	 * @throws SpcfArgumentNullException if dateEnum or text is null
	 * @throws SpcfFormatException if the specified pattern is invalid or the text cannot be parsed
	 * @throws SpcfClassCastException if the encapsulated format is an unexpected type (the valid type is Integer)
	 * @throws SpcfIllegalArgumentException if the encapsulated format has an unexpected value (a valid example would be DateFormat.SHORT)
	 */
	public static SpcfCalendar parse(String text, SpcfDateEnum dateEnum)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(dateEnum);
		return dateFormat.parse(text);
	}

	/**
	 * Parses text from the beginning of the given string to produce an
	 * SpcfCalendar (date).
	 * @param text The date/time string to be parsed.
	 * @param dateEnum the given date formatting style.
	 * @param timeEnum the given time formatting style.
	 * @return  an SpcfCalendar parsed from the string
	 * @throws SpcfArgumentNullException if dateEnum or timeEnum or text is null
	 * @throws SpcfFormatException if the specified pattern is invalid or the text cannot be parsed
	 * @throws SpcfClassCastException if the encapsulated format is an unexpected type (the valid type is Integer)
	 * @throws SpcfIllegalArgumentException if the encapsulated format has an unexpected value (a valid example would be DateFormat.SHORT)
	 */
	public static SpcfCalendar parse(String text, SpcfDateEnum dateEnum, SpcfTimeEnum timeEnum)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(dateEnum, timeEnum);
		return dateFormat.parse(text);
	}

	/**
	 * Parses text from the beginning of the given string to produce an
	 * SpcfCalendar (date).
	 * @param text The date/time string to be parsed.
	 * @param dateTimeEnum The given date/time formatting style.
	 * @return An SpcfCalendar parsed from the string
	 * @throws SpcfArgumentNullException if dateTimeEnum or text is null
	 * @throws SpcfFormatException if the specified pattern is invalid or the text cannot be parsed
	 * @throws SpcfClassCastException if the encapsulated format is an unexpected type (the valid type is String)
	 */
	public static SpcfCalendar parse(String text, SpcfDateTimeEnum dateTimeEnum)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(dateTimeEnum);
		return dateFormat.parse(text);
	}

	/**
	 * Parses text from the given string to produce an SpcfCalendar (date).
	 * @param text The date/time string to be parsed.
	 * @param pattern A custom format string
	 * @return An SpcfCalendar parsed from the string
	 * @throws SpcfArgumentNullException if pattern or text is null
	 * @throws SpcfArgumentOutOfRangeException if pattern is an empty string
	 * @throws SpcfFormatException if the specified pattern is invalid or the text cannot be parsed
	 */
	public static SpcfCalendar parse(String text, String pattern)
	{
		SpcfDateFormat dateFormat = SpcfFactory.getInstance().createDateFormat();
		dateFormat.setPattern(pattern);
		return dateFormat.parse(text);
	}

	/**
	 * Sets the date/time pattern for formatting and parsing.
	 * @param dateEnum The given date formatting style.
	 * @return previously set pattern as a string, or null, if no pattern
	 * has been set before.
	 * @throws SpcfArgumentNullException dateEnum is null
	 * @throws SpcfClassCastException dateEnum contains an invalid third party
	 *         format type
	 * @throws SpcfIllegalArgumentException dateEnum contains an invalid third
	 *         party standard format value
	 */
	public abstract String setPattern(SpcfDateEnum dateEnum);

	/**
	 * Sets the date/time pattern for formatting and parsing.
	 * @param timeEnum The given time formatting style.
	 * @return previously set pattern as a string, or null, if no pattern
	 * has been set before.
	 * @throws SpcfArgumentNullException timeEnum is null
	 * @throws SpcfClassCastException timeEnum contains an invalid third party
	 *         format type
	 * @throws SpcfIllegalArgumentException timeEnum contains an invalid third
	 *         party standard format value
	 */
	public abstract String setPattern(SpcfTimeEnum timeEnum);

	/**
	 * Formats a Date into a date/time string. The resulting date-time pattern is made by combining dateEnum 
	 * and timeEnum patterns seperated by single blank space. <br/>
	 * As an example, if dateEnum is LongFormat and timeEnum is ShortFormat, then the resulting format will be like
	 * "Long_Date_Format Short_TimeFormat", e.g. "Thursday, January 03, 2002 12:00 AM".
	 * @param dateEnum The given date formatting style.
	 * @param timeEnum The given time formatting style.
	 * @return previously set pattern as a string, or null, if no pattern
	 * has been set before.
	 * @throws SpcfArgumentNullException dateEnum or timeEnum is null
	 * @throws SpcfClassCastException dateEnum or timeEnum contains an invalid third party
	 *         format type or the encapsulated third party types are not the same
	 * @throws SpcfIllegalArgumentException dateEnum or timeEnum contains an invalid third
	 *         party standard format value
	 */
	public abstract String setPattern(SpcfDateEnum dateEnum, SpcfTimeEnum timeEnum);

	/**
	 * Sets the date/time pattern for formatting and parsing.
	 * @param dateTimeEnum The given date/time formatting style.
	 * @return previously set pattern as a string, or null, if no pattern
	 * has been set before. 
	 * @throws SpcfArgumentNullException dateTimeEnum is null
	 * @throws SpcfClassCastException dateTimeEnum contains an invalid third party
	 *         format type
	 */
	public abstract String setPattern(SpcfDateTimeEnum dateTimeEnum);

	/**
	 * Sets the date/time pattern for formatting and parsing.
	 * @param pattern A custom format string
	 * @return previously set pattern as a string, or null, if no pattern
	 * has been set before.
	 * @throws SpcfArgumentNullException pattern is null
	 * @throws SpcfArgumentOutOfRangeException pattern is an empty string
	 * @throws SpcfFormatException if the specified pattern is invalid or the text cannot be parsed
	 */
	public abstract String setPattern(String pattern);

	/**
	 * Formats a Date into a date/time string.
	 * @param date the date/time value to be formatted into a date/time string.
	 * @return The formatted date/time string.
	 * @throws SpcfArgumentNullException if date is null
	 * @throws SpcfClassCastException if date is not an instance of SpcfCalendarImpl
	 * @throws SpcfFormatException if no pattern is set or the pattern set is invalid
	 */
	public abstract String format(SpcfCalendar date);

	/**
	 * Parses text from the given string to produce an SpcfCalendar (date).
	 * @param text The date/time string to be parsed.
	 * @return An SpcfCalendar parsed from the string
	 * @throws SpcfFormatException if the specified string cannot be parsed
	 *         because either text is invalid or no pattern is set or the given
	 *         pattern is invalid
	 */
	public abstract SpcfCalendar parse(String text);
	
	/**
	 * Creates a date/time format object using the default geographical and
	 * cultural conventions.
	 */
	public static SpcfDateFormat createInstance()
	{
		return SpcfFactory.getInstance().createDateFormat();
	}

	/**
	 * Creates a date/time format object using the specified geographical and
	 * cultural conventions.
	 * @param localeInfo An instance of SpcfLocaleInfo or null to specify the
	 *  current culture in dotnet or default locale in java
	 */
	public static SpcfDateFormat createInstance(SpcfLocaleInfo localeInfo)
	{
		return SpcfFactory.getInstance().createDateFormat(localeInfo);
	}
}
