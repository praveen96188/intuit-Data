package com.intuit.spc.foundations.portability;

import com.intuit.spc.foundations.portability.text.SpcfEncoding;
import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;
import com.intuit.spc.foundations.portability.text.SpcfUnsupportedEncodingException;

/**
 * SpcfStringUtil contains portable utility methods for manipulating native strings.
 */
public abstract strictfp class SpcfStringUtil {
	
	/**
     * SpcfStringUtil instance that is used for static methods
     */
    private static SpcfStringUtil sStringUtil;
    
    static
    {
        sStringUtil = SpcfFactory.getInstance().createStringUtil(); //instance for static methods
    }

    /**
	 * Compares two strings lexicographically. 
	 * The comparison is based on the Unicode value of each character 
	 * in the strings. If there is no index position at which the strings 
	 * differ, then the shorter string lexicographically precedes 
	 * the longer string. 
	 * By default C#'s System.String.Compare() and System.String.CompareTo()
	 * use a different comparison algorithm.
	 * @param strA a string
	 * @param strB a string
	 * @return negative integer, 0, or positive integer 
	 * if strA is less, equal or bigger than strB, respectively.
	 * @throws SpcfArgumentNullException if any argument is null
	 */
	public static int compareStrings(String strA, String strB)
	{
		SpcfParamValidator.checkIsNotNull(strA, "strA");
		SpcfParamValidator.checkIsNotNull(strB, "strB");
		return strA.compareTo(strB);
	} 
	
	/**
     * Compares two strings lexicographically, 
     * ignoring case differences.
     * 
     * @param strA a string
	 * @param strB a string
     * @return a negative integer, zero, 
     * or a positive integer as the the specified 
     * String is greater than, equal to, 
     * or less than this String, ignoring case considerations
     * @throws SpcfArgumentNullException if any argument is null
     */
    public static int compareStringsIgnoreCase(String strA, String strB)
    {
		return sStringUtil.doCompareStringsIgnoreCase(strA, strB);
	}
    
	/**
     * Encodes a String into a sequence of 
     * bytes using the default encoding, 
     * storing the result into a new byte array
     * @param s the String to be copied from.
     * @return The resultant byte array
     * @throws SpcfArgumentNullException if s is null
     */
    public static byte[] getBytes(String s)
    {
		return sStringUtil.doGetBytes(s, null);
	}
    
    /**
     * Encodes a String into a sequence of 
     * bytes using the specified encoding, 
     * storing the result into a new byte array
     * If encoding is null, it uses the default platform encoding
     * @see com.intuit.spc.foundations.portability.text.SpcfEncoding 
     * for list of accepted encodings 
     * @throws SpcfUnsupportedEncodingException if encoding is not supported
     * @throws SpcfArgumentNullException if s is null
     * @param s the String to be copied from.
     * @param encoding the encoding used for conversion
     * @return The resultant byte array
     */
    public static byte[] getBytes(String s, SpcfEncoding encoding)
    {
		return sStringUtil.doGetBytes(s, encoding);
	}
    
    /**
     * Copies characters from a String into 
     * the destination character array. The first character 
     * to be copied is at index srcBegin; the last character 
     * to be copied is at index srcEnd-1 (thus the total number 
     * of characters to be copied is srcEnd-srcBegin). 
     * The characters are copied into the subarray of dst 
     * starting at index dstBegin and ending at index: 
     * dstbegin + (srcEnd-srcBegin) - 1
     * 
     * @param s the String to be copied from.
     * @param srcBegin index of the first character in the String to copy.
     * @param srcEnd index after the last character in the String to copy.
     * @param dst the destination array.
     * @param dstBegin the start offset in the destination array
     * @throws SpcfArgumentNullException if s or dst is null
     * @throws SpcfIndexOutOfBoundsException if any of the following is true: 
     * srcBegin is negative. 
     * srcBegin is greater than srcEnd 
     * srcEnd is greater than the length of this String 
     * dstBegin is negative 
     * dstBegin+(srcEnd-srcBegin) is larger than dst.length
     */
    public static void getChars(String s, int srcBegin,
            int srcEnd, char[] dst, int dstBegin)  
    {
    	
    	sStringUtil.doGetChars(s, srcBegin, srcEnd, dst, dstBegin);
    } 
    
    /**
     * Replaces each substring of this string that matches the 
     * given regular expression with the given replacement.
     *  
     * @param s the source String 
     * @param regex the regular expression to which this string is to be matched
     * @param replacement the string that replaces the regular expression
     * @return copy of string after modification
     * @throws SpcfIllegalArgumentException  
     * if the regular expression's syntax is invalid
     * @throws SpcfArgumentNullException if any argument is null
     */
    public static String getReplaceAll(String s, String regex, String replacement)
    {
    	return sStringUtil.doGetReplaceAll(s, regex, replacement, false);
    }
    
    /**
     * This method is same as its overloaded couterpart- getReplaceAll(String, String, String). <p>
     * 
     * The only difference is that this method takes an extra boolean parameter- escapeMetaCharacters.
     * If this parameter is true, all metacharacters or escape sequences in the input sequence- regex
     * will be given no special meaning and regex will be treated as a literal pattern. If escapeMetaCharacters
     * is false, there will be no difference between this method and overloaded getReplaceAll(String, String, String) method.
     */
    public static String getReplaceAll(String s, String regex, String replacement, boolean escapeMetaCharacters)
    {
    	return sStringUtil.doGetReplaceAll(s, regex, replacement, escapeMetaCharacters);
    }
    /**
     * Replaces first matches of the  
     * given regular expression with the given replacement.
     *  
     * @param s the source String
     * @param regex the regular expression to which this string is to be matched
     * @param replacement the string that replaces the regular expression
     * @return copy of string after modification
     * @throws SpcfIllegalArgumentException 
     * if the regular expression's syntax is invalid
     * @throws SpcfArgumentNullException if any argument is null
     */
    public static String getReplaceFirst(String s, String regex, String replacement)
    {
    	return sStringUtil.doGetReplaceFirst(s, regex, replacement, false);
    }
    
    /**
     * This method is same as its overloaded couterpart- replaceFirst(String, String). <p>
     * 
     * The only difference is that this method takes an extra boolean parameter- escapeMetaCharacters.
     * If this parameter is true, all metacharacters or escape sequences in the input sequence- regex
     * will be given no special meaning and regex will be treated as a literal pattern. If escapeMetaCharacters
     * is false, there will be no difference between this method and overloaded replaceFirst(String, String) method.
     */
    public static String getReplaceFirst(String s, String regex, String replacement, boolean escapeMetaCharacters)
    {
    	return sStringUtil.doGetReplaceFirst(s, regex, replacement, escapeMetaCharacters);
    }
    
    /**
     * Converts all of the characters in a String 
     * to upper case using the rules of the given Locale
     * If locale is null, the system default is used
     * 
     * @param s the String to be converted 
     * @param locale use the case transformation rules for this locale 
     * @return copy of the string after being converted to upper case
     */
    public static String toUpperCase(String s, SpcfLocaleInfo locale)
    {
    	return sStringUtil.doToUpperCase(s, locale);
    }
  
    /**
     * Converts all of the characters in a String 
     * to lower case using the rules of the given Locale
     * If locale is null, the system default is used
     * 
     * @param s the String to be converted 
     * @param locale use the case transformation rules for this locale 
     * @return copy of the string after being converted to lower case
     */
    public static String toLowerCase(String s, SpcfLocaleInfo locale)
    {
    	return sStringUtil.doToLowerCase(s, locale);
    }
    
    /**
     * Splits a string around matches of the given regular expression
     * Trailing empty strings will NOT be discarded.
     * 
     * @param s the string to be split 
     * @param regex the delimiting regular expression 
     * @return the array of strings computed by splitting 
     * the string around matches of the given regular expression
     * @throws SpcfIllegalArgumentException  
     * if the regular expression's syntax is invalid 
     * @throws SpcfArgumentNullException if s or regex is null
     */
    public static String[] split(String s, String regex)
    {
    	return sStringUtil.doSplit(s, regex, 0, false);
    }
    
    /**
     * This method is same as its overloaded couterpart- split(String, String). <p>
     * 
     * The only difference is that this method takes an extra boolean parameter- escapeMetaCharacters.
     * If this parameter is true, all metacharacters or escape sequences in the input sequence- regex
     * will be given no special meaning and regex will be treated as a literal pattern. If escapeMetaCharacters
     * is false, there will be no difference between this method and overloaded split(String, String) method.
     */
    public static String[] split(String s, String regex, boolean escapeMetaCharacters)
    {
    	return sStringUtil.doSplit(s, regex, 0, escapeMetaCharacters);
    }
    /**
     * Splits a string around matches of the given regular expression
     * The array returned by this method contains each substring 
     * of the string that is terminated by another substring 
     * that matches the given expression or is terminated 
     * by the end of the string. 
     * 
     * The substrings in the array are in the order in which they 
     * occur in the string. If the expression does not match 
     * any part of the input then the resulting array has just 
     * one element, namely the string.
     * 
     * The limit parameter controls the number of times the 
     * pattern is applied and therefore affects the length of 
     * the resulting array. If the limit n is greater than zero 
     * then the pattern will be applied at most n - 1 times, 
     * the array's length will be no greater than n, 
     * and the array's last entry will contain all input 
     * beyond the last matched delimiter. 
     * If n is zero then the pattern will be applied as many 
     * times as possible, the array can have any length. 
     * Note that in this case, the trailing empty strings 
     * will NOT be discarded. 
     *
     * @param s the String to be split 
     * @param regex the delimiting regular expression
     * @param limit the result threshold 
     * @return the array of strings computed by splitting 
     * the string around matches of the given regular expression
     * @throws SpcfIllegalArgumentException  
     * if the regular expression's syntax is invalid
     * @throws SpcfIndexOutOfBoundsException  
     * if limit is negative  
     * @throws SpcfArgumentNullException if s or regex is null
     */
    public static String[] split(String s, String regex, int limit) 
    {
    	return sStringUtil.doSplit(s, regex, limit, false);
    }
    
    /**
     * This method is same as its overloaded couterpart- split(String, String, int). <p>
     * 
     * The only difference is that this method takes an extra boolean parameter- escapeMetaCharacters.
     * If this parameter is true, all metacharacters or escape sequences in the input sequence- regex
     * will be given no special meaning and regex will be treated as a literal pattern. If escapeMetaCharacters
     * is false, there will be no difference between this method and overloaded split(String, String, int) method.
     */
    public static String[] split(String s, String regex, int limit, boolean escapeMetaCharacters)
    {
    	return sStringUtil.doSplit(s, regex, limit, escapeMetaCharacters);
    }
     
	/**
     * Forwarding virtual method for compareStringsIgnoreCase 
     */
    protected abstract int doCompareStringsIgnoreCase(String strA, String strB); 
    
    /**
     * Forwarding virtual method for getBytes  
     */
    protected abstract byte[] doGetBytes(String s, SpcfEncoding encoding);
    
    /**
     * Forwarding virtual method for getChars  
     */
    protected abstract void doGetChars(String s, int srcBegin,
            int srcEnd, char[] dst, int dstBegin); 
    
    /**
     * Forwarding virtual method for getReplaceAll 
     */
    protected abstract String doGetReplaceAll(String s, String regex, String replacement, boolean escapeMetaCharacters);
    
    /**
     * Forwarding virtual method for getReplaceFirst 
     */
    protected abstract String doGetReplaceFirst(String s, String regex, String replacement, boolean escapeMetaCharacters);
   
    /**
     * orwarding virtual method for toUpperCase
     */
    protected abstract String doToUpperCase(String s, SpcfLocaleInfo locale);
  
    /**
     * Forwarding virtual method for toLowerCase 
     */
    protected abstract String doToLowerCase(String s, SpcfLocaleInfo locale); 
     
    /**
     * Forwarding virtual method for all split methods
     */
    protected abstract String[] doSplit(String s, String regex, int limit, boolean escapeMetaCharacters);
    
}
