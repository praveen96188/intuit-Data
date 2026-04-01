package com.intuit.spc.foundations.portability.text;

import com.intuit.spc.foundations.portability.*;

/**
 * A class for number formatting (i.e., number -> text) and 
 * parsing (text -> number).
 * SpcfNumberFormat helps you to format and parse 
 * numbers for any locale. Your code can be completely independent of the 
 * geographical and cultural conventions for decimal points, 
 * thousands-separators, or even the particular decimal digits used. 
 * 
 * <p>The Numeric format 
 * symbols in the table below are consistent across target platforms. These 
 * symbols can be combined to construct custom patterns. The patterns are 
 * case sensitive. </p>
 * 
 * <pre>
 * Symbol  		Description
 * 0       		A digit
 * #       		A digit, zero shows as absent
 * .       		Placeholder for decimal separator
 * ,       		Placeholder for grouping separator
 * %       		Multiply by 100 and show as percentage
 * 
 * If you supply a pattern with multiple grouping characters 
 * (i.e. multiple commas), 
 * the interval between the last one and the end of the integer 
 * is the one that is used and must not exceed size 9. 
 * So "#,##,###,####" == "######,####" == "##,####,####". 
 * 
 * A valid custom pattern consists of the following:
 * - an optional Prefix, composed from [a-zA-Z] letters, plus these symbols: %()$ 
 * - a Number format (using the Numeric format symbols above)
 * - an optional Suffix, composed from same characters as the Prefix
 * 
 * The % char can only appear once, either in the Prefix or Suffix.
 * The Number format consists of an Integer_part and 
 * optionally followed by . and Fractional_Part.
 * The Integer_part is composed of one or more # and/or 0 optionally separated by commas. 
 * The Integer_part must group all the # at the beginning.
 * The Fractional_part is composed of one or more 0 and/or #, 
 * with all the 0s grouped at the beginning.
 * 
 * Note: The E (scientific exponential) pattern is not supported.
 *  
 * </pre>
 * @see          SpcfLocaleInfo
 */
public abstract class SpcfNumberFormat 
{

	/**
	 * Formats a number to produce a string.
	 * 
	 * <pre>
	 *WARNING
	 *This method produces different results in Java and in .NET 
	 *when returning string returns more than 15 non-zero significant digits. 
	 *E.g.   
     *SpcfNumberFormat.format(3.1415926535897932, "#.###############");
     *C#        "3.14159265358979"    //15 digits 
     *Java      "3.141592653589793"  //16
     *
     *SpcfNumberFormat.setPattern(7000000000000000d/3, "#,###.##############");
     *C#       "2,333,333,333,333,330"    //15 digit
     *Java     "2,333,333,333,333,333.5" //16
     *
     *SpcfNumberFormat.setPattern(99999999999999.99, "#,###.00############");
     *C#     "100,000,000,000,000.00"
     *Java   "99,999,999,999,999.98"
     *
     *There are several ways to work around this current limitation:
     *1. SpcfNumberFormat.format(long, String) instead of 
     *         SpcfNumberFormat.format(double, String) whenever you can
     *2. Use double numbers that do not have more than 15 significant digits. 
     *         E.g if you want to output numbers with 2 fractional decimals, 
     *         then the integer part should be less than 999,999,999,999 (12 digits). 
     *3. Use patterns that limit the number of fractional digits. 
     *         E.g. use #,###.00, rather than use #,###.## for outputting numbers. 
     *</pre>
	 * 
	 * @param number The double value to format
	 * @param pattern A format pattern
	 * @return A formatted string
	 * @throws SpcfArgumentNullException if pattern is null
	 * @throws SpcfArgumentOutOfRangeException if pattern is an empty string
	 * @throws SpcfFormatException if the pattern is invalid 
	 */
	public static String format(double number, String pattern)
	{
		SpcfNumberFormat numFormat = SpcfFactory.getInstance().createNumberFormat();
		numFormat.setPattern(pattern);
		return numFormat.format(number);
	}

	/**
	 * Formats a number to produce a string.
	 * @param number The long value to format
	 * @param pattern A format pattern
	 * @return A formatted string
	 * @throws SpcfArgumentNullException if pattern is null 
	 * @throws SpcfArgumentOutOfRangeException if pattern is an empty string
	 * @throws SpcfFormatException if the pattern is invalid
	 */
	public static String format(long number, String pattern)
	{
		SpcfNumberFormat numFormat = SpcfFactory.getInstance().createNumberFormat();
		numFormat.setPattern(pattern);
		return numFormat.format(number);
	}

	/**
	 * Parses the entire text to produce a number. A valid text is composed of 
	 * an integer part with digits that are optionally separated by commas. 
	 * The integer part is optionally followed by a . and a fractional part 
	 * composed of digits. The text can start or end with either % or $.
	 * @param text The string to be parsed
	 * @return The parsed value
	 * @throws SpcfArgumentNullException if text is null 
	 * @throws SpcfArgumentOutOfRangeException if text is an empty string
	 * @throws SpcfIllegalArgumentException if text is an invalid string
	 */
	public static double parseDouble(String text)
	{
		SpcfNumberFormat numFormat = SpcfFactory.getInstance().createNumberFormat();
		return numFormat.doParseDouble(text);
	}

	/**
	 * Parses the entire text to produce a number. A valid text is composed of 
	 * an integer part with digits that are optionally separated by commas. 
	 * The text can start or end with either % or $.
	 * @param text The string to be parsed
	 * @return The parsed value
	 * @throws SpcfArgumentNullException if text is null 
	 * @throws SpcfArgumentOutOfRangeException if text is an empty string
	 * @throws SpcfIllegalArgumentException if text is an invalid string
	 */
	public static long parseLong(String text)
	{
		SpcfNumberFormat numFormat = SpcfFactory.getInstance().createNumberFormat();
		return numFormat.doParseLong(text);
	}
	
	
	/**
	 * Sets the number pattern for formatting and parsing.
	 * @param pattern A format pattern
	 * @return previously set pattern as a string, or null, if no pattern
	 * has been set before
	 * @throws SpcfArgumentNullException if pattern is null 
	 * @throws SpcfArgumentOutOfRangeException if pattern is an empty string
	 * @throws SpcfFormatException if the pattern is invalid 
	 */
	public abstract String setPattern(String pattern);
	
	/**
	 * Formats a number to produce a string.
	 * 
	 * <p>
	 * WARNING
	 * This method produces different results in Java and in .NET 
	 * when returning string returns more than 15 non-zero significant digits. 
	 * For more info see format(double, String)
	 * </p>  
	 * 
	 * @param number The double value to format
	 * @return A formatted string
	 * @throws SpcfFormatException if the pattern 
     * was not previously set via setPattern(String) method
	 */
	public abstract String format(double number);
	
	/**
	 * Formats a number to produce a string.
	 * @param number The long value to format
	 * @return A formatted string
	 * @throws SpcfFormatException if the pattern 
     * was not previously set via setPattern(String) method
	 */
	public abstract String format(long number);
	
	/**
	 * Parses text from the beginning of the given string to produce a number.
	 * @param text The string to be parsed
	 * @return The parsed value
	 * @throws SpcfArgumentNullException if text is null
	 * @throws SpcfArgumentOutOfRangeException if text is an empty string
	 * @throws SpcfIllegalArgumentException if text is an invalid string
	 */
	protected abstract double doParseDouble(String text);
	
	/**
	 * Parses text from the beginning of the given string to produce a number.
	 * @param text The string to be parsed
	 * @return The parsed value
	 * @throws SpcfArgumentNullException if text is null
	 * @throws SpcfArgumentOutOfRangeException if text is an empty string
	 * @throws SpcfIllegalArgumentException if text is an invalid long string, 
	 * it's in hexadecimal representation, or it results in an overflow
	 */
	protected abstract long doParseLong(String text);
	
	/**
	 * Creates a number format object using the default geographical and
	 * cultural conventions.
	 */
	public static SpcfNumberFormat createInstance()
	{
		return SpcfFactory.getInstance().createNumberFormat();
	}

	/**
	 * Creates a number format object using the specified geographical and
	 * cultural conventions.
	 * @param localeInfo An instnace of SpcfLocaleInfo
	 */
	public static SpcfNumberFormat createInstance(SpcfLocaleInfo localeInfo)
	{
		return SpcfFactory.getInstance().createNumberFormat(localeInfo);
	}
}
