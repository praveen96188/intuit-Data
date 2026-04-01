package com.intuit.spc.foundations.portabilitySpecific.text;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Pattern;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfArgumentOutOfRangeException;
import com.intuit.spc.foundations.portability.SpcfClassCastException;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.text.SpcfDateEnum;
import com.intuit.spc.foundations.portability.text.SpcfDateFormat;
import com.intuit.spc.foundations.portability.text.SpcfDateTimeEnum;
import com.intuit.spc.foundations.portability.text.SpcfFormatException;
import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;
import com.intuit.spc.foundations.portability.text.SpcfTimeEnum;
import com.intuit.spc.foundations.portability.util.SpcfCalendar;
import com.intuit.spc.foundations.portabilitySpecific.util.SpcfCalendarImpl;

/**
 * A platform specific class for date/time formatting. 
 * 
 * @see          SpcfDateEnum
 * @see          SpcfTimeEnum
 * @see          SpcfDateTimeEnum
 * @see          SpcfLocaleInfo
 * @see         <a href="http://rfc.net/rfc1766.html">RFC 1766</a>
 * @see         <a href="http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt">ISO 639</a>
 * @see         <a href="http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html">ISO 3166</a>
 */
public class SpcfDateFormatImpl extends SpcfDateFormat 
{
	/**
	 * Only following characters are allowed in the custom pattern string.
	 */	
	private static String  sCustomPatternChars = "yMdhHmsEaS";
	
	/**
	 * Only following characters are allowed in the standard pattern string.
	 */	
	private static String  sStandardPatternChars = "dDfFgGrRsStT";
	
	/**
	 * Custom date-time pattern originally set by the user.
	 */
	private String mUserPattern;	
	
	/**
	 * User pattern represented in platform specific pattern.
	 */
	private String mCompiledPattern;
	
	/**
	 * Geographical and regional information.
	 */
	protected Locale mLocale;
	
	/**
	 * The object to use to format or parse a date.
	 */
	private DateFormat mDateFormat;
	
	/**
	 * Constructs a date/time format object using the default geographical and
	 * cultural conventions.
	 */
	public SpcfDateFormatImpl()
	{
		this.mLocale = Locale.getDefault();
	}
	
	/**
	 * Constructs a date/time format object using the specified geographical and
	 * cultural conventions.
	 * @param localeInfo An instance of SpcfLocaleInfo or null to specify the 
	 *  current culture in dotnet or default locale in java
	 */
	public SpcfDateFormatImpl(SpcfLocaleInfo localeInfo)
	{	
		this.mLocale = SpcfLocaleInfoUtility.getLocale(localeInfo);
	}

	public String setPattern(SpcfDateEnum dateEnum) 
	{
		SpcfParamValidator.checkIsNotNull(dateEnum, "dateEnum");	
		//
		String ret = null;
		if(dateEnum == SpcfDateEnum.ShortFormat)
		{
			ret = setPattern("d");
		}
		else if(dateEnum == SpcfDateEnum.LongFormat)
		{
			ret = setPattern("D");
		}		
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfDateFormat#setPattern(com.intuit.spc.foundations.portability.text.SpcfTimeEnum)
	 */
	public String setPattern(SpcfTimeEnum timeEnum) 
	{
		SpcfParamValidator.checkIsNotNull(timeEnum, "timeEnum");
		//
		String ret = null;
		if(timeEnum == SpcfTimeEnum.ShortFormat)
		{
			ret = setPattern("t");
		}
		else if(timeEnum == SpcfTimeEnum.LongFormat)
		{
			ret = setPattern("T");
		}		
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfDateFormat#setPattern(com.intuit.spc.foundations.portability.text.SpcfDateEnum, com.intuit.spc.foundations.portability.text.SpcfTimeEnum)
	 */
	public String setPattern(SpcfDateEnum dateEnum, SpcfTimeEnum timeEnum) 
	{		
		SpcfParamValidator.checkIsNotNull(dateEnum, "dateEnum");
		SpcfParamValidator.checkIsNotNull(timeEnum, "timeEnum");
		//
		String ret = null;
		if(dateEnum == SpcfDateEnum.LongFormat && timeEnum == SpcfTimeEnum.ShortFormat)
		{
			ret = setPattern("f");
		}
		else if(dateEnum == SpcfDateEnum.LongFormat && timeEnum == SpcfTimeEnum.LongFormat)
		{
			ret = setPattern("F");
		}
		else if(dateEnum == SpcfDateEnum.ShortFormat && timeEnum == SpcfTimeEnum.ShortFormat)
		{
			ret = setPattern("g");
		}
		else if(dateEnum == SpcfDateEnum.ShortFormat && timeEnum == SpcfTimeEnum.LongFormat)
		{
			ret = setPattern("G");
		}
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfDateFormat#setPattern(com.intuit.spc.foundations.portability.text.SpcfDateTimeEnum)
	 */
	public String setPattern(SpcfDateTimeEnum dateTimeEnum) 
	{
		SpcfParamValidator.checkIsNotNull(dateTimeEnum, "dateTimeEnum");
		//
		String ret = null;
		if(dateTimeEnum == SpcfDateTimeEnum.Iso8601)
		{
			ret = setPattern("s");
		}
		else if(dateTimeEnum == SpcfDateTimeEnum.Rfc1123)
		{
			ret = setPattern("r");
		}		
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfDateFormat#setPattern(java.lang.String)
	 */
	public String setPattern(String pattern) 
	{			
		String compiledPattern = compilePattern(pattern);			
		mDateFormat = new SimpleDateFormat(compiledPattern, mLocale);
		//
		String ret = mUserPattern;
		mUserPattern = pattern;	
		mCompiledPattern = compiledPattern;
		return ret;
	}
	
	/**
	 * This method compiles the SPC-F pattern string into Java specific pattern string.
	 * 
	 * @param pattern SPC-F date-time pattern string.
	 * @return date-time pattern string in Java format.
	 * @throws SpcfArgumentNullException pattern is null.
	 * @throws SpcfArgumentOutOfRangeException pattern is an empty string.
	 * @throws SpcfFormatException if the specified pattern is invalid or the text cannot be parsed.
	 */
	private String compilePattern(String pattern)
	{
		String compiledPattern = null;
		SpcfParamValidator.checkIsNotNullOrEmptyString(pattern, "pattern");
		if(pattern.length() == 1)
		{
			compiledPattern = compileStandardCharacterPattern(pattern.charAt(0));
		}
		else
		{
			compiledPattern = compileCustomPattern(pattern);
		}
		return compiledPattern;
	}
	
	/**
	 * This method compiles the SPC-F standard pattern character into Java specific pattern string.
	 */
	private String compileStandardCharacterPattern(char patternChar)
	{
		if (sStandardPatternChars.indexOf(patternChar) == -1) 
	    {
	    	throw new SpcfFormatException("Illegal pattern character " + "'" + patternChar + "'");
	    }
		
		DateFormat dateFormat = null;
		String compiledPattern = null;
		try
		{
			switch(patternChar)
			{
			case 'd':
				dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, mLocale);
				compiledPattern = ((SimpleDateFormat)dateFormat).toPattern();
				//we must change the year from a 2 digit to a 4 digit year value so the result is consistant with dotnet.
				compiledPattern = compiledPattern.replaceFirst("yy", "yyyy");				
				break;
			case 'D':
				dateFormat = DateFormat.getDateInstance(DateFormat.FULL, mLocale);
				compiledPattern = ((SimpleDateFormat)dateFormat).toPattern();
				//we must change the day from a 1 digit to a 2 digit value so the result is consistant with dotnet.
				compiledPattern = compiledPattern.replaceFirst("d", "dd");
				break;
			case 'f':
				compiledPattern = compileStandardCharacterPattern('D') + " " + compileStandardCharacterPattern('t');
				break;
			case 'F':
				compiledPattern = compileStandardCharacterPattern('D') + " " + compileStandardCharacterPattern('T');
				break;
			case 'g':
				compiledPattern = compileStandardCharacterPattern('d') + " " + compileStandardCharacterPattern('t');				
				break;
			case 'G':
				compiledPattern = compileStandardCharacterPattern('d') + " " + compileStandardCharacterPattern('T');
				break;
			case 'r':
			case 'R':
				compiledPattern = "EEE, dd MMM yyyy HH':'mm':'ss 'GMT'";
				break;	
			case 's':
			case 'S':
				compiledPattern = "yyyy'-'MM'-'dd'T'HH':'mm':'ss.S'Z'";
				break;	
			case 't':
				dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, mLocale);
				compiledPattern = ((SimpleDateFormat)dateFormat).toPattern();
				break;	
			case 'T':
				dateFormat = DateFormat.getTimeInstance(DateFormat.MEDIUM, mLocale);
				compiledPattern = ((SimpleDateFormat)dateFormat).toPattern();
				break;	
			}
		}
		catch (IllegalArgumentException e)
		{
			throw new SpcfIllegalArgumentException(e);   //unexpected encapsulated format value
		}		
		return compiledPattern;
	}	
	
	/**
	 * This method converts SPC-F specific custom date-time pattern string into Java specific pattern string.
	 * Follwoing characters are allowed in the SPC-F specific date-time pattern string:
	 * <p>
	 * <li>[0-9]
	 * <li>[y, M, d, h, H, m, s, E, a, S]
	 * <li>[any non-alpha numeric character]
	 * <li>[any character set in single quotes] - characters in single quotes will not be interpreted and will appear in the 
	 * output string as it is.<br/>   
	 *
	 * @param pattern date-time pattern string in SPC-F custom format.
	 * @return pattern date-time pattern string in Java format.	
	 * @throws SpcfFormatException if the specified pattern is invalid or the text cannot be parsed.
	 */
	private String compileCustomPattern(String pattern)
	{		
		StringBuilder compiledPattern = new StringBuilder();
		//
    	int patternLength = pattern.length();    
    	for (int i=0; i<patternLength; i++) 
    	{
    	    char ch = pattern.charAt(i);    	    
    	    //All the characters between single quotes should not be interpreted.
    	    if (ch == '\'') 
    	    {
    	    	compiledPattern.append(ch);
    	    	boolean enclosingQuoteFound = false;    	    	
    	    	if(i++ != patternLength)
    	    	{
	    	    	//disregard all the characters between single quotes
	    	    	for(; i<patternLength; i++)
	    	    	{
	    	    		ch = pattern.charAt(i);
	    	    		compiledPattern.append(ch);
	    	    		if (ch == '\'') 
	    	    		{ 
	    	    			enclosingQuoteFound = true;
	    	    			break;
	    	    		}
	    	    	} 
    	    	}    	    	
    	    	//throw an exception if the closing single quote is not found.
    	    	if(!enclosingQuoteFound)
    	    	{
    	    		throw new SpcfFormatException("Unterminated quote");
    	    	}    	    		
    	    } 
    	    else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) 
    	    {   	    	
	    	    if (sCustomPatternChars.indexOf(ch) == -1) 
	    	    {
	    	    	throw new SpcfFormatException("Illegal pattern character " + "'" + ch + "'");
	    	    }
	    	    else
	    	    {
	    	    	//determine the count of consecutive occurences of the current character.	    	    	
	    	    	int charCount = 1;	    	    			    	    		
    	    		for(int j=i+1; j<patternLength; j++) 
    	    		{
    	    			char nextChar = pattern.charAt(j);  
    	    			if(nextChar == ch)
    	    			{
    	    				i++;
    	    				charCount++;
    	    			}
    	    			else
    	    			{
    	    				break;
    	    			}
    	    		}
    	    		compileCustomCharacterPattern(ch, charCount, compiledPattern);
	    	    }
    	    } 
    	    else
    	    {
    	    	compiledPattern.append(ch);
    	    }
    	} 
    	
    	return compiledPattern.toString();
	}
	
	/** 
	 * This method compiles the SPC-F standard pattern character sequence into Java specific pattern string and then appends the
	 * string to compiledPattern.<br/> 
	 * The table below describes how the SPC-F pattern character translation will be applied for JAVA and .NET. In the table Fn(x)
	 * stands for a function which returns number of conecutive pattern character-x, eg. for the follwoing pattern string "yyyyyy-mm"
	 * Fn(y)=6 and Fn(m)=2.<br/>
	 * <pre>
	 * SPC-F                    JAVA        .NET          	
	 * ========================================================================
	 * 1 <= Fn(y) <= 3           yy         yy	
	 * Fn(y) >= 4                yyyy       yyyy
	 * ------------------------------------------------------------------------
	 * Fn(M) == 1                M          M
	 * Fn(M) == 2                MM         MM
	 * Fn(M) == 3                MMM        MMM 
	 * Fn(M) >= 4                MMMM       MMMM 
	 * ------------------------------------------------------------------------
	 * Fn(d) == 1                d          d
	 * Fn(d) >= 2                dd         dd
	 * ------------------------------------------------------------------------
	 * Fn(h) == 1                h          h
	 * Fn(h) >= 2                hh         hh
	 * ------------------------------------------------------------------------
	 * Fn(H) == 1                H          H
	 * Fn(H) >= 2                HH         HH
	 * ------------------------------------------------------------------------
	 * Fn(m) == 1                m          m
	 * Fn(m) >= 2                mm         mm
	 * ------------------------------------------------------------------------
	 * Fn(s) == 1                s          s
	 * Fn(s) >= 1                ss         ss
	 * ------------------------------------------------------------------------
	 * 1 <= Fn(E) <= 3           E          ddd
	 * Fn(E) >= 4                EEEE       dddd
	 * ------------------------------------------------------------------------ 
	 * Fn(a) >= 1                a          tt
	 * ------------------------------------------------------------------------
	 * Fn(S) >= 1                S          fff
	 * ========================================================================	
	 * </pre>
	 */
	private void compileCustomCharacterPattern(char patternChar, int charCount, StringBuilder compiledPattern)
	{		
		//replace consecutive occurrences of the current character as per the rules outlined in the table above. 
		if(patternChar == 'd' || patternChar == 'h' || patternChar == 'H' || patternChar == 'm' || patternChar == 's')
		{   
			if(charCount == 1)
			{
				compiledPattern.append(patternChar);
			}
			else    	    				
			{
				compiledPattern.append(patternChar);
				compiledPattern.append(patternChar);
			}
		}
		else if(patternChar == 'a' || patternChar == 'S')
		{
			compiledPattern.append(patternChar);
		}
		else if(patternChar == 'y')
		{
			if(charCount == 1 || charCount == 2 || charCount == 3)
			{
				compiledPattern.append("yy");
			}    	    			
			else if(charCount >= 4)
			{
				compiledPattern.append("yyyy");
			}    	    			  	    			
		}
		else if(patternChar == 'M')
		{    	    			
			for(int MCount = 0; MCount < charCount && MCount <= 4; MCount++)
			{ 
				compiledPattern.append("M");
			}
		}
		else if(patternChar == 'E')
		{
			if(charCount >= 4)
			{
				compiledPattern.append("EEEE");
			}
			else
			{ 
				compiledPattern.append("E");
			}    	    			
		}			
    }	

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfDateFormat#format(com.intuit.spc.foundations.portability.util.SpcfCalendar)
	 */
	public String format(SpcfCalendar date) 
	{
		SpcfParamValidator.checkIsNotNull(date, "date");
		
		//Consistency
		if (mDateFormat == null)
			throw new SpcfFormatException();

		String formattedDate = null;

		try
		{	//Extract third party object
			GregorianCalendar calendar = ((SpcfCalendarImpl)date).toSpecific();			
			mDateFormat.setTimeZone( calendar.getTimeZone() );
			formattedDate = mDateFormat.format( calendar.getTime() );
		}
		catch(ClassCastException e)
		{
			throw new SpcfClassCastException(e);
		}

		return formattedDate;
	}
	
	/**
	 * Valid examples supported by SPC-F for parsing ISO8601 formats:<br/> 
	 * <li>2002-01-03T00:00:00.0Z -> full date-time format including milliseconds.</br>
	 * <li>2002-01-03T00:00:00Z -> date-time format, excluding milliseconds.</br> 
	 * <li>2002-01-03T00:00Z -> date-time format, excluding seconds and milliseconds.</br>
	 * <li>2002-01-03T00Z -> date-time format, excluding minutes, seconds and milliseconds.</br>
	 * <li>2002-01-03Z -> date-time format, excluding time.</br>	
	 */
	private String compileISO8601PatternForParsing(String dateTimeText)
	{						
		//split milliseconds from other parts
		String beforeDotSubstring = dateTimeText, afterDotSubstring = null;
		int idxOfDot = dateTimeText.indexOf('.');
		if(idxOfDot != -1)
		{
			beforeDotSubstring = dateTimeText.substring(0, idxOfDot);
			afterDotSubstring = dateTimeText.substring(idxOfDot + 1, dateTimeText.length());
		}	
		//now determine the correct pattern string and regular expression corresponding to the input dateTimeText.
		StringBuilder compiledPattern = new StringBuilder();		
		String regex = null;				
		switch(beforeDotSubstring.length())
		{
		case 20:
			compiledPattern.append("yyyy'-'MM'-'dd'T'HH':'mm':'ss'Z'");
			regex = "(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\dZ)";						
			break;
		case 19:
			compiledPattern.append("yyyy'-'MM'-'dd'T'HH':'mm':'ss");
			regex = "(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\d:\\d\\d.\\d+Z)";						
			break;
		case 17:
			compiledPattern.append("yyyy'-'MM'-'dd'T'HH':'mm'Z'");
			regex = "(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\d:\\d\\dZ)";						
			break;
		case 14:
			compiledPattern.append("yyyy'-'MM'-'dd'T'HH'Z'");
			regex = "(\\d\\d\\d\\d-\\d\\d-\\d\\dT\\d\\dZ)";						
			break;
		case 11:
			compiledPattern.append("yyyy'-'MM'-'dd'Z'");
			regex = "(\\d\\d\\d\\d-\\d\\d-\\d\\dZ)";			
			break;				
		}
		//Validate the input dateTimeText format against the regular expression.	
		boolean validDateTimeText = false;		
		if (regex != null && Pattern.compile(regex).matcher(dateTimeText).find())
        {
            validDateTimeText = true;
            if (afterDotSubstring != null)
            {
            	compiledPattern.append("'.'S'Z'");
            }               
        }	
		//
		if(!validDateTimeText)		
		{
			throw new SpcfFormatException("Not a valid SPC-F supported ISO8601 formatted date-time string.");			
		}
		//
		return compiledPattern.toString();
	}
	
	/**
	 * Java automatically corrects invalid dates and times but .NET throws an exception. To avoid these 
	 * inconsistencies, if Java has done any auto correction then throw SpcfFormatException.
	 * @param dateTimeText
	 * @return
	 */
	private void validateDateTimeParsingResult(String inputDateTimeText, String pattern, GregorianCalendar parsedDateTime)
	{			
    	int patternLength = pattern.length();    	
    	//
    	StringBuilder dateTimeBuffer = new StringBuilder(inputDateTimeText);
    	for (int i=0; i<patternLength; i++) 
    	{    		
    	    char ch = pattern.charAt(i);     	   
    	    //All the characters between single quotes should not be interpreted.
    	    if (ch == '\'') 
    	    {    	    	
    	    	if(i++ != patternLength)
    	    	{
	    	    	//disregard all the characters between single quotes
	    	    	for(; i<patternLength; i++)
	    	    	{
	    	    		ch = pattern.charAt(i);	 	    	    		
	    	    		if (ch == '\'') 
	    	    		{ 	    	    			
	    	    			break;
	    	    		}
	    	    		else
	    	    		{
	    	    			dateTimeBuffer.deleteCharAt(0);	    	    			
	    	    		}
	    	    	} 
    	    	}    	    
    	    } 
    	    else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) 
    	    {     	    	
    	    	StringBuilder patternCharsBuffer = new StringBuilder();    	    	
    	    	patternCharsBuffer.append(ch);
    	    	//determine consecutive occurences of the current character.
	    		for(int j=i+1; j<patternLength; j++) 
	    		{
	    			char nextChar = pattern.charAt(j); 
	    			if(nextChar == ch)
	    			{	    				
	    				i++;
	    				patternCharsBuffer.append(nextChar);			
	    			}
	    			else
	    			{
	    				break;
	    			}
	    		}
	    		//	    		   		
	    		try
	    		{	
	    			if(ch != 'a')
	    			{
		    			SimpleDateFormat simpleDateFormat = new SimpleDateFormat(patternCharsBuffer.toString(), mLocale);
		    			String parsedValue = simpleDateFormat.format(parsedDateTime.getTime());
	    				int endIdx = parsedValue.length();
	    				if(!parsedValue.equals(dateTimeBuffer.substring(0, endIdx))) 
	    				{
	    					//make sure that the test is not failing because of the leading 0 in the input string.
	    					if(patternCharsBuffer.length() == 1 && 
	    							(ch == 'y' || ch == 'M' || ch == 'd' || ch == 'h' || ch == 'H' || ch == 'm' || ch == 's') &&
	    							(dateTimeBuffer.charAt(0) == '0') && 
	    							(parsedValue.equals(dateTimeBuffer.substring(1, ++endIdx))))	    									
	    					{	    						
	    						dateTimeBuffer.delete(0, endIdx);
	    					}
	    					else
	    					{
	    						throw new SpcfFormatException();
	    					}
	    				}	
	    				else
	    				{
	    					dateTimeBuffer.delete(0, endIdx);
	    				}
	    			}
	    		}
	    		catch(NumberFormatException ex)
	    		{
	    			throw new SpcfFormatException(ex);
	    		}
    	    }    
    	    else
    	    {
    	    	dateTimeBuffer.deleteCharAt(0);
    	    }
    	} 
	}


	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfDateFormat#parse(java.lang.String)
	 */
	public SpcfCalendar parse(String text) 
	{
		SpcfParamValidator.checkIsNotNull(text, "text");

		//Consistency
		if (mDateFormat == null)
		{
			throw new SpcfFormatException();
		}
		
		SpcfCalendarImpl portableCalendarImpl = null;	
		try
		{
			if(mUserPattern == "s" || mUserPattern == "S")
			{
				String compiledPattern = compileISO8601PatternForParsing(text);	
				mDateFormat = new SimpleDateFormat(compiledPattern, mLocale);				
				mCompiledPattern = compiledPattern;				
			}
			//
			Date date = mDateFormat.parse(text);			
			if (mDateFormat instanceof SimpleDateFormat)
			{
				//If the caller specified a time zone, then let the Calendar
				//convert the time to UTC
				portableCalendarImpl = new SpcfCalendarImpl(date.getTime());
				validateDateTimeParsingResult(text, mCompiledPattern, portableCalendarImpl.toSpecific());
				//
				String pattern = ((SimpleDateFormat)mDateFormat).toPattern();
				boolean isTimeZoneInPattern = pattern.matches(".*z.*");				
				if ( !isTimeZoneInPattern )
				{
					//If the caller did not specify a time zone, override the
					//default assumption that the date string is local time.
					//Instead, treat it as GMT to be consistent with C#.
					portableCalendarImpl = new SpcfCalendarImpl(
							date.getTime() + 
							mDateFormat.getCalendar().get(Calendar.ZONE_OFFSET) + 
							mDateFormat.getCalendar().get(Calendar.DST_OFFSET));
				}				
			}
		}
		catch (ParseException e)
		{
			throw new SpcfFormatException(e);
		}		
		return portableCalendarImpl;
	}

}

