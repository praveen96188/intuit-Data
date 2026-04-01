package com.intuit.spc.foundations.portabilitySpecific;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.text.SpcfEncoding;
import com.intuit.spc.foundations.portability.text.SpcfLocaleInfo;
import com.intuit.spc.foundations.portability.text.SpcfUnsupportedEncodingException;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfPatternSyntaxException;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfRegexValidator;
import com.intuit.spc.foundations.portabilitySpecific.text.SpcfEncodingUtility;
import com.intuit.spc.foundations.portabilitySpecific.text.SpcfLocaleInfoUtility;
/**
 * SpcfStringUtilImpl provides concrete implementation for SpcfStringUtil. 
 * Contains common utility methods for manipulating strings.
 */
public strictfp class SpcfStringUtilImpl extends SpcfStringUtil 
{  
    protected int doCompareStringsIgnoreCase(String strA, String strB)
    {
    	SpcfParamValidator.checkIsNotNull(strA, "strA");
    	SpcfParamValidator.checkIsNotNull(strB, "strB");
		
    	return strA.compareToIgnoreCase(strB);
    }
     
    protected byte[] doGetBytes(String s, SpcfEncoding encoding)
    {
    	SpcfParamValidator.checkIsNotNull(s, "s");
    	String javaEncoding = null;
		if (encoding != null)
		{
			javaEncoding = SpcfEncodingUtility.getEncodingName(encoding.getEncoding());
		}
		
    	try
		{
    		if (javaEncoding != null)
    		{
    			return s.getBytes(javaEncoding);
    		}
    		return s.getBytes();
		}
    	catch (UnsupportedEncodingException ex)
		{
    		throw new SpcfUnsupportedEncodingException(ex);
    	}
    }
     
    protected void doGetChars(String s, int srcBegin,
            int srcEnd, char[] dst, int dstBegin)
    {
    	SpcfParamValidator.checkIsNotNull(s, "s");
    	SpcfParamValidator.checkIsNotNull(dst, "dst");
    	
    	try
		{
    		s.getChars(srcBegin, srcEnd, dst, dstBegin);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
    	}
    } 
     
    protected String doGetReplaceAll(String s, String regex, String replacement, boolean escapeMetaCharacters)
    {
    	SpcfParamValidator.checkIsNotNull(s, "s");
    	SpcfParamValidator.checkIsNotNull(regex, "regex");
        SpcfParamValidator.checkIsNotNull(replacement, "replacement");
        regex = validateAndTransformRegex(regex, escapeMetaCharacters);       
        //        
        try
        {
            String tmp = s.replaceAll(regex, replacement);
            return tmp;
        }
        catch (PatternSyntaxException ex)
        {
            throw new SpcfIllegalArgumentException(ex);
        } 
    }
     
    protected String doGetReplaceFirst(String s, String regex, String replacement, boolean escapeMetaCharacters)
    {
    	SpcfParamValidator.checkIsNotNull(s, "s");
    	SpcfParamValidator.checkIsNotNull(regex, "regex");
        SpcfParamValidator.checkIsNotNull(replacement, "replacement");
        regex = validateAndTransformRegex(regex, escapeMetaCharacters);    
        //
        try
        {
            String tmp = s.replaceFirst(regex, replacement);
            return tmp;
        }
        catch (PatternSyntaxException ex)
        {
            throw new SpcfIllegalArgumentException(ex);
        }
    }
    
    protected String doToUpperCase(String s, SpcfLocaleInfo locale)
    {
    	SpcfParamValidator.checkIsNotNull(s, "s");
     
    	if (locale == null)
    	{
    		return s.toUpperCase();
    	}
    	else
    	{
    		Locale loc = SpcfLocaleInfoUtility.getLocale(locale);
    		return s.toUpperCase(loc);
    	} 
    }
   
    protected String doToLowerCase(String s, SpcfLocaleInfo locale)
    {
    	SpcfParamValidator.checkIsNotNull(s, "s");
    	 
    	if (locale == null)
    	{
    		return s.toLowerCase();
    	}
    	else
    	{
    		Locale loc = SpcfLocaleInfoUtility.getLocale(locale);
    		return s.toLowerCase(loc);
    	} 
    }   
      
    protected String[] doSplit(String s, String regex, int limit, boolean escapeMetaCharacters)
    {
    	SpcfParamValidator.checkIsNotNull(s, "s");
    	SpcfParamValidator.checkIsNotNull(regex, "regex");
        if (limit < 0)
        {
            throw new SpcfIndexOutOfBoundsException();
        }
        regex = validateAndTransformRegex(regex, escapeMetaCharacters);          
        //
        try
        { 
            if (limit == 0)
            {                
                limit = -1;
            }            
            //Note By default when split(regex,0)
            //is used, the empty trailing strings are discarded
            //so -1 will insure that the trailing strings
            //are NOT discarded
            return s.split(regex, limit);
        }
        catch (PatternSyntaxException ex)
        {
            throw new SpcfIllegalArgumentException(ex);
        } 
    }  
    
    private String validateAndTransformRegex(String regex, boolean escapeMetaCharacters)
    {  
        if(escapeMetaCharacters)
        {
            regex = Pattern.quote(regex); 
        }
        else
        {
            try
            {
                SpcfRegexValidator.validate(regex);
            }
            catch (SpcfPatternSyntaxException ex)
            {
                throw new SpcfIllegalArgumentException(ex);
            }
        } 
        return regex;
    }
    
}
