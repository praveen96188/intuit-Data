package com.intuit.spc.foundations.portabilitySpecific.text;
import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.text.*;
import java.text.*;
import java.util.*;

/**
 * A platform specific class for number formatting. 
 * 
 * @see          SpcfLocaleInfo
 * @see         <a href="http://rfc.net/rfc1766.html">RFC 1766</a>
 * @see         <a href="http://ftp.ics.uci.edu/pub/ietf/http/related/iso639.txt">ISO 639</a>
 * @see         <a href="http://www.chemie.fu-berlin.de/diverse/doc/ISO_3166.html">ISO 3166</a>
 */
public class SpcfNumberFormatImpl extends SpcfNumberFormat 
{

	/**
	 * Geographical and regional information.
	 */
	protected Locale mLocale;
	
	/**
	 * The object to use to format or parse a number.
	 */
	private NumberFormat mNumFormat;
	
	/**
	 * Constructs a number format object using the default geographical and
	 * cultural conventions.
	 */
	public SpcfNumberFormatImpl()
	{
		this.mLocale = Locale.getDefault();
	}
	
	/**
	 * Constructs a number format object using the specified geographical and
	 * cultural conventions.
	 * @param localeInfo An instnace of SpcfLocaleInfo
	 * @throws NullPointerException localeInfo is null
	 */
	public SpcfNumberFormatImpl(SpcfLocaleInfo localeInfo)
	{		
		mLocale = SpcfLocaleInfoUtility.getLocale(localeInfo); 
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfNumberFormat#setPattern(java.lang.String)
	 */
	public String setPattern(String pattern) 
	{
		String ret = null;
		if (mNumFormat != null)
		{
			ret = ((DecimalFormat)this.mNumFormat).toPattern();
		}
		
		SpcfParamValidator.checkIsNotNullOrEmptyString(pattern, "pattern");

		//Verify pattern and throw SpcfFormatException, if pattern is not valid
		String err = validatePattern(pattern);
		if (err != null)
		{
			throw new SpcfFormatException(err);
		}

		//Get a number format object for a locale
		NumberFormat aNumFormat = NumberFormat.getInstance(mLocale);
		
		//Java documentation warns that the factory methods could return
		//subclasses other than DecimalFormat
		if ( !(aNumFormat instanceof DecimalFormat) )
		{
			throw new SpcfFormatException("unexpected subclass");
		}
		
		try
		{	//Set the custom pattern
			((DecimalFormat) aNumFormat).applyPattern(pattern);		
		}
		catch(IllegalArgumentException e)
		{
			throw new SpcfFormatException(e);
		}
		
		mNumFormat = aNumFormat;
		return ret;
	}

	private char mNumberGroupSeparator = ',';
	private char mNumberDecimalSeparator = '.';
	private char mCurrencySymbol = '$';
	private char mPercentSymbol = '%';
	
	
	/**
	 * Validate a pattern, returns the error string if not valid
	 */
	private String validatePattern(String pattern)
	{
		/// A valid custom pattern consists of the following:
		/// - an optional Prefix, composed from [a-zA-Z] letters, 
		/// plus these symbols: %()$ 
		/// - a Number format (using the Numeric format symbols above)
		/// - an optional Suffix, composed from same characters as the Prefix
		/// 
		/// The % and $ chars can only appear once, either in the Prefix or Suffix.
		/// The Number format consists of an Integer_part and 
		/// optionally followed by . and Fractional_Part.
		/// The Integer_part is composed of one or more 0 and # optionally separated by commas. 
		/// The Integer_part must group all the # at the beginning.
		/// The Fractional_part is composed of one or more 0 and #, 
		/// with all the 0s grouped at the beginning.
		/// Exponential notation is not supported (E0)
		
		//if patterns starts with E followed by 0s only, reject it, as exponential notation
		String expNotation = "E";
		for (int i = 0; i < pattern.length() - 1; ++i)
		{
			expNotation += "0";
		}
				
		if (pattern.equals(expNotation))
		{
			return "Exponential notation formats are not supported.";
		}

		int i = 0; //index
		int percentCount = 0;

		//verify Prefix
		for (; i < pattern.length(); ++i)
		{
			char c = pattern.charAt(i);
			if (Character.isLetter(c) || c == ')' || c == '(' ||
				c == mCurrencySymbol) 
			{
				continue;
			}
			if (c == mPercentSymbol)
			{
				percentCount++;
				continue;
			}
			
			if (c == '#' || c == '0')
			{
				break;
			}

			//hm, invalid char here
			return "Invalid prefix char '" + c + "' at position " + i + " in '" + pattern + "' pattern";
		}

		//verify Number
		if (i == pattern.length())
		{
			return "Pattern '"+ pattern + "' doesn't contain the Numeric format";
		}

		boolean hasFractionalPoint = false;
		boolean hasZeros = false; //0
		boolean hasPound = false; //#
		boolean hasPreviousComma = false; //#
		for (;  i< pattern.length(); ++i)
		{
			char c = pattern.charAt(i);
			if (c == '#')
			{
				hasPound = true;
				hasPreviousComma = false;

				if (!hasFractionalPoint && hasZeros) 
					//in the integer part zeros are AFTER #s
				{
					return "Pattern '"+ pattern + "' contains #s after the 0s in the Numeric integer format";
				}
				continue;
			}
			else if (c == '0')
			{
				hasZeros = true;
				hasPreviousComma = false;

				if (hasFractionalPoint && hasPound) 
					//in the fractional part #s are AFTER 0s
				{
					return "Pattern '"+ pattern + "' contains 0s after the #s in the Numeric fractional format";
				}
				continue;
			}
			else if (c == mNumberGroupSeparator) 
			{
				if (hasPreviousComma)
				{
					return "Pattern '"+ pattern + "' contains consecutive commas";
				}
				if (hasFractionalPoint)
				{
					return "Pattern '"+ pattern + "' contains commas in the Numeric fractional format";
				}
				hasPreviousComma = true;
				continue;
			}
			else if (c == mNumberDecimalSeparator) 
			{
				if (hasFractionalPoint)
				{
					return "Pattern '"+ pattern + "' contains multiple fractional points in the Numeric fractional format";
				}

				hasFractionalPoint = true;
				hasPound = false;
				hasZeros = false;
				hasPreviousComma = false;

				//lookup that the following char exists and it's a 0 or #
				if (i+1 >= pattern.length() || 
						(pattern.charAt(i+1) != '0' && pattern.charAt(i+1) != '#'))
				{
					return "Pattern '"+ pattern + "' contains a fractional point not followed by a valid Numeric fractional format";
				}
				continue;
			}
			//not a numeric char
			break;
		}

		//verify Suffix
		for (; i < pattern.length(); ++i)
		{
			char c = pattern.charAt(i);
			if (Character.isLetter(c) || c == ')' || c == '(' || 
				c == mCurrencySymbol) 
			{
				continue;
			}
			if (c == mPercentSymbol)
			{
				percentCount++;
				continue;
			}
			
			//hm, invalid char here
			return "Invalid prefix char '" + c + "' at position " + i + " in '" + pattern + "' pattern";
		}

		//verify that we have max one %
		if (percentCount >=2)
		{
			return "Pattern '"+ pattern + "' contains multiple " +  mPercentSymbol + " chars";
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfNumberFormat#format(double)
	 */
	public String format(double number) 
	{
		//Consistency
		if (mNumFormat == null)
			throw new SpcfFormatException("setPattern() has not been called to set a pattern before.");
	
		String s = mNumFormat.format(number);
		
		//to be consistent with C#, make sure there are 15 significant digits
		//s = adjustTo15SignificantDigits(s);
		return s;
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfNumberFormat#format(long)
	 */
	public String format(long number) 
	{
		//Consistency
		if (mNumFormat == null)
			throw new SpcfFormatException("setPattern() has not been called to set a pattern before.");
	
		String s = mNumFormat.format(number);
		return s;
	}

	/* (non-Javadoc)
	 * @see com.intuit.spc.foundations.portability.text.SpcfNumberFormat#parse(String)
	 */
	protected double doParseDouble(String text)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(text, "text");
		
		//Get a number format object for a locale
		NumberFormat aNumFormat = NumberFormat.getInstance(mLocale);
		
		if (text.charAt(0) == mCurrencySymbol ||
			text.charAt(0) == mCurrencySymbol)
		{
			text = text.substring(1);
		}
		else if (text.charAt(text.length()-1) == mCurrencySymbol ||
				text.charAt(text.length()-1) == mCurrencySymbol)
		{
			text = text.substring(0, text.length()-1);
		}
		
		//Special case for parsing percentages
		boolean isPercent = false;
		int position = text.indexOf( this.mPercentSymbol );
		if ( position > -1 )
		{	text = text.substring(0, position) + text.substring(position+1);
			isPercent = true;
		}
		
		ParsePosition pos = new ParsePosition(0); 
		Number ret = aNumFormat.parse(text, pos);
		if (pos.getIndex() < text.length() || pos.getErrorIndex() != -1)
		{
			throw new SpcfIllegalArgumentException("parseDouble(" + text + ") failed.");
		}
		
		double returnValue = ret.doubleValue();
		if (isPercent) returnValue /= 100;
		
		return returnValue;		
	}	

	//This method is required only because DOT-NET doesn't handle the 
	//conversion from double to long very well...specifically parsing Int64.MAX_VALUE
	public long doParseLong(String text)
	{
		SpcfParamValidator.checkIsNotNullOrEmptyString(text, "text");

		//Get a number format object for a locale
		NumberFormat aNumFormat = NumberFormat.getInstance(mLocale);
		
		if (text.charAt(0) == mCurrencySymbol ||
				text.charAt(0) == mCurrencySymbol)
		{
			text = text.substring(1);
		}
		else if (text.charAt(text.length()-1) == mCurrencySymbol ||
				text.charAt(text.length()-1) == mCurrencySymbol)
		{
			text = text.substring(0, text.length()-1);
		}
			
			
		//Special case for parsing percentages
		boolean isPercent = false;
		int position = text.indexOf( this.mPercentSymbol );
		if ( position > -1 )
		{	text = text.substring(0, position) + text.substring(position+1);
			isPercent = true;
		}
			
		ParsePosition pos = new ParsePosition(0); 
		Number ret = aNumFormat.parse(text, pos);
		if (pos.getIndex() < text.length() || pos.getErrorIndex() != -1)
		{
			throw new SpcfIllegalArgumentException("parseLong(" + text + ") failed.");
		}
		
		if (!ret.getClass().equals(Long.class))
		{
			throw new SpcfIllegalArgumentException("parseLong(" + text + ") failed.");
		}
			
		long returnValue = ret.longValue();
		if (isPercent) returnValue /= 100;
		return returnValue;		
	}	

}
