package com.intuit.spc.foundations.portabilitySpecific;

import com.intuit.spc.foundations.portability.*;
import com.intuit.spc.foundations.portability.text.*; 

import java.io.UnsupportedEncodingException; 
import java.util.Locale;
import com.intuit.spc.foundations.portabilitySpecific.text.*;

/**
 * This is the platform specific SpcfStringBuilder implementation.
 */
public class SpcfStringBuilderImpl extends SpcfStringBuilder
{

	protected StringBuilder mStringBuilder;

	public StringBuilder toSpecific()
	{
		return mStringBuilder;
	}
	
	/**
	 * Build empty object
	 *
	 */
    public SpcfStringBuilderImpl()
    {
    	mStringBuilder = new StringBuilder();
	}

    /**
     * Build from a String
     * @param str a String instance to encapsulate
     */
    public SpcfStringBuilderImpl(String str)
	{
    	SpcfParamValidator.checkIsNotNull(str, "str");
    	mStringBuilder = new StringBuilder(str);
	}

    /**
     * Build from a StringBuffer
     * @param str	StringBuffer instance to encapsulate
     */
    public SpcfStringBuilderImpl(StringBuilder str)
	{
    	SpcfParamValidator.checkIsNotNull(str, "str");
    	mStringBuilder = str;
	}

    /**
     * Build from a SpcfStringBuilder
     * @param str
     */
    public SpcfStringBuilderImpl(SpcfStringBuilder str)
    {
    	SpcfParamValidator.checkIsNotNull(str, "str");
    	mStringBuilder = new StringBuilder(str.toString());
    }
    
    /**
     * Build from a char buffer
     * @param buffer
     */
	public SpcfStringBuilderImpl(char[] buffer)
	{
		SpcfParamValidator.checkIsNotNull(buffer, "buffer");
		
		mStringBuilder = new StringBuilder(buffer.length);
		mStringBuilder.append(buffer);
	}
	
	/**
	 * Set an initial capacity
	 * @param capacity
	 */
	public SpcfStringBuilderImpl(int capacity)
	{
		if (capacity < 0)
		{
			throw new SpcfIllegalArgumentException();
		}
		mStringBuilder = new StringBuilder(capacity);
	}
	
	/**
	 * Constructs a new string by decoding the specified subarray of 
	 * bytes using the specified charset. If null is passed for 
	 * the charset, then the default encoding it is used.
	 * 
	 * The behavior of this constructor when the given bytes 
	 * are not valid for the encoding to be used is unspecified 
	 * and it may be different in Java and C# implementations.
	 *  
	 * @param buffer the bytes to be decoded into characters
	 * @param offset the index of the first byte to decode
	 * @param length the number of bytes to decode
	 * @param encoding SpcfEncoding object specifying a supported charset
	 * 
	 * @throws SpcfUnsupportedEncodingException if the encoding is not supported
	 * @throws SpcfIndexOutOfBoundsException - if the offset and length arguments index characters outside the bounds of the bytes array
	 * @throws SpcfArgumentNullException if buffer is null
	 */ 
	public SpcfStringBuilderImpl(byte[] buffer, int offset, 
			int length, SpcfEncoding encoding)
	{
		SpcfParamValidator.checkIsNotNull(buffer, "buffer");
		
		String javaEncoding = null;
		if (encoding != null)
		{
			javaEncoding = SpcfEncodingUtility.getEncodingName(encoding.getEncoding());
		}
		
		try
		{
			String str;
			if (javaEncoding == null)
			{
				str = new String(buffer, offset, length);
			}
			else
			{
				str = new String(buffer, offset, length, javaEncoding);
			}
			mStringBuilder = new StringBuilder(str);
		}
		catch (UnsupportedEncodingException e1)
		{
			throw new SpcfUnsupportedEncodingException(e1);
		}
		catch (IndexOutOfBoundsException e2)
		{
			throw new SpcfIndexOutOfBoundsException(e2);
		}
	}
		
	/**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#compareTo(Object)
	 */
	public int compareTo(Object o)
	{
		SpcfParamValidator.checkIsNotNull(o, "o");
		
		if (o instanceof SpcfStringBuilder)
		{
			String thisString = toString();
			String otherString = ((SpcfStringBuilder)o).toString();
			return thisString.compareTo(otherString);
		}
		else 
		{
			throw new SpcfClassCastException();
		}
	}

	/**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#compareTo(Object)
	 */
	public int compareToString(String str)
	{
		SpcfParamValidator.checkIsNotNull(str, "str");

		String thisString = toString();
		return thisString.compareTo(str);
	}

	/**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#equals(Object)
	 */
	public boolean equals(Object o)
	{
		if (o == null)
		{
			return false;
		}

		if (o instanceof SpcfStringBuilder)
		{
			String thisString = toString();
			String otherString = ((SpcfStringBuilder)o).toString();
			return thisString.equals(otherString);
		}
		return false;
	}
	
	/**
	 * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#hashCode()
	 */
	public int hashCode()
	{
		return this.mStringBuilder.toString().hashCode();
	}
	
    /**
     * 
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#toString()
    */
    public String toString()
    {
   		return mStringBuilder.toString();
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#getBytes()
     */
    public byte[] getBytes()
    {
    	SpcfEncoding encoding = null;
    	return SpcfStringUtil.getBytes(toString(), encoding);
    }

    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#getLength()
	 */
    public int getLength()
    {
    	return mStringBuilder.length();	
    }

    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#charAt(int)
     */
    public char charAt(int index)
    {
    	try
		{
    		return mStringBuilder.charAt(index);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
    	}
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#setCharAt(int, char)
     */
    public SpcfStringBuilder setCharAt(int index, char ch)
    {
    	try
		{
    		mStringBuilder.setCharAt(index, ch);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
    	}
    	return this;
    }  
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#setToUpperCase()
     */
    public SpcfStringBuilder setToUpperCase()
    {
    	return setToUpperCase(null);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#setToUpperCase(SpcfLocaleInfo)
     */
    public SpcfStringBuilder setToUpperCase(SpcfLocaleInfo locale)
    {
    	String tmp = null;
    	if (locale == null)
    	{
    		tmp = mStringBuilder.toString().toUpperCase();
    	}
    	else
    	{
    		Locale loc = SpcfLocaleInfoUtility.getLocale(locale);
    		tmp = mStringBuilder.toString().toUpperCase(loc);
    	}
    	return replace(tmp);
    }
     
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#setToLowerCase()
     */
    public SpcfStringBuilder setToLowerCase()
    {
    	return setToLowerCase(null);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#setToLowerCase(SpcfLocaleInfo)
     */
    public SpcfStringBuilder setToLowerCase(SpcfLocaleInfo locale)
    {
    	String tmp = null;
    	if (locale == null)
    	{
    		tmp = mStringBuilder.toString().toLowerCase();
    	}
    	else
    	{
    		Locale loc = SpcfLocaleInfoUtility.getLocale(locale);
    		tmp = mStringBuilder.toString().toLowerCase(loc);
    	}
    	return replace(tmp);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#replaceAll(String, String)
     */
    public SpcfStringBuilder replaceAll(String regex, String replacement)
    {
    	return replace(SpcfStringUtil.getReplaceAll(toString(), regex, replacement, false));
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#replaceAll(String, String, boolean)
     */
    public SpcfStringBuilder replaceAll(String regex, String replacement, boolean escapeMetaCharacters)
    {
    	return replace(SpcfStringUtil.getReplaceAll(toString(), regex, replacement, escapeMetaCharacters));
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#replaceFirst(String, String)
     */
    public SpcfStringBuilder replaceFirst(String regex, String replacement)
    {
    	return replace(SpcfStringUtil.getReplaceFirst(toString(), regex, replacement, false)); 
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#replaceFirst(String, String, boolean)
     */
    public SpcfStringBuilder replaceFirst(String regex, String replacement, boolean escapeMetaCharacters)
    {
    	return replace(SpcfStringUtil.getReplaceFirst(toString(), regex, replacement, escapeMetaCharacters));
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(boolean)
     */
    public SpcfStringBuilder append(boolean b)
    {
    	mStringBuilder.append(b);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(char)
     */
    public SpcfStringBuilder append(char c)
    {
    	mStringBuilder.append(c);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(int)
     */
    public SpcfStringBuilder append(int i)
    {
    	mStringBuilder.append(i);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(long)
     */
    public SpcfStringBuilder append(long l)
    {
    	mStringBuilder.append(l);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(float)
     */
    public SpcfStringBuilder append(float f)
    {
    	mStringBuilder.append(f);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(double)
     */
    public SpcfStringBuilder append(double d)
    {
    	mStringBuilder.append(d);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(Object)
     */
    public SpcfStringBuilder append(Object o)
    {
    	mStringBuilder.append(o);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(String)
     */
    public SpcfStringBuilder append(String f)
    {
    	mStringBuilder.append(f);
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(SpcfStringBuilder)
     */
    public SpcfStringBuilder append(SpcfStringBuilder sb)
    {
    	mStringBuilder.append( sb == null ? "null" : sb.toString());
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(char[])
     */
    public SpcfStringBuilder append(char[] str)
    {
    	SpcfParamValidator.checkIsNotNull(str, "str");
    	mStringBuilder.append(str);
		
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#append(char[], int, int)
     */
    public SpcfStringBuilder append(char[] str, int offset, int len)
    {
    	SpcfParamValidator.checkIsNotNull(str, "str");
    	try
		{
    		mStringBuilder.append(str, offset, len);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, boolean) 
     */
    public SpcfStringBuilder insert(int offset, boolean b)
    {
    	try
		{
    		mStringBuilder.insert(offset, b);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, char)
     */
    public SpcfStringBuilder insert(int offset, char c)
    {
    	try
		{
    		mStringBuilder.insert(offset, c);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, int)
     */
    public SpcfStringBuilder insert(int offset, int i)
    {
    	try
		{
    		mStringBuilder.insert(offset, i);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder
     */
    public SpcfStringBuilder insert(int offset, long l)
    {
    	try
		{
    		mStringBuilder.insert(offset, l);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, float)
     */
    public SpcfStringBuilder insert(int offset, float f)
    {
    	try
		{
    		mStringBuilder.insert(offset, f);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, double)
     */
    public SpcfStringBuilder insert(int offset, double d)
    {
    	try
		{
    		mStringBuilder.insert(offset, d);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, Object)
     */
    public SpcfStringBuilder insert(int offset, Object o)
    {
    	try
		{
    		mStringBuilder.insert(offset, o);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, String)
     */
    public SpcfStringBuilder insert(int offset, String f)
    {
    	try
		{
    		mStringBuilder.insert(offset, f);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, SpcfStringBuilder)
     */
    public SpcfStringBuilder insert(int offset, SpcfStringBuilder sb)
    {
    	try
		{
    		mStringBuilder.insert(offset, (sb == null) ? "null" : sb.toString());
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, char[])
     */
    public SpcfStringBuilder insert(int offset, char[] str)
    {
    	SpcfParamValidator.checkIsNotNull(str, "str");
    	
    	try
		{
    		mStringBuilder.insert(offset, str);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#insert(int, char[], int, int)
     */
    public SpcfStringBuilder insert(int index, char[] str, int offset, int len)
    {
    	SpcfParamValidator.checkIsNotNull(str, "str");
    	try
		{
    		mStringBuilder.insert(index, str, offset, len);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#deleteCharAt(int)
     */
	public SpcfStringBuilder deleteCharAt(int index)
	{
		try
		{
    		mStringBuilder.deleteCharAt(index);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#delete(int, int)
     */
    public SpcfStringBuilder delete(int start, int end)
    {
    	if (start == getLength())
    	{
    		throw new SpcfIndexOutOfBoundsException();
    	}
    	
    	try
		{
    		mStringBuilder.delete(start, end);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    }

    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#replace(int, int, String)
     */
    public SpcfStringBuilder replace(int start, int end, String str)
    {
    	SpcfParamValidator.checkIsNotNull(str, "str");
		
    	if (start == getLength())
    	{
    		throw new SpcfIndexOutOfBoundsException();
    	}
    	
    	try
		{
    		mStringBuilder.replace(start, end, str);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    	return this;
    } 
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#replaceAllStrings(String, String)
     */
    public SpcfStringBuilder replaceAllStrings(String oldValue, String newValue)
    {
    	SpcfParamValidator.checkIsNotNullOrEmptyString(oldValue, "oldValue");
    	SpcfParamValidator.checkIsNotNull(newValue, "newValue");
    	
    	int index = mStringBuilder.indexOf(oldValue);
    	
    	while (index != -1)
    	{
    		mStringBuilder.replace(index, index + oldValue.length(), newValue);
    		index = mStringBuilder.indexOf(oldValue, index + newValue.length());
    	}
    	
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#replaceFirstString(String, String)
     */
    public SpcfStringBuilder replaceFirstString(String oldValue, String newValue)
    {
    	SpcfParamValidator.checkIsNotNullOrEmptyString(oldValue, "oldValue");
    	SpcfParamValidator.checkIsNotNull(newValue, "newValue");
    	
    	int index = mStringBuilder.indexOf(oldValue);
    	
    	if (index != -1)
    	{
    		mStringBuilder.replace(index, index + oldValue.length(), newValue);
    	}
    	return this;
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#getCapacity()
     */
    public int getCapacity()
    {
    	return mStringBuilder.capacity();
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#ensureCapacity(int)
     */
    public void ensureCapacity(int minimumCapacity)
    {
    	if (minimumCapacity < 0)
		{
			throw new SpcfIllegalArgumentException();
		}
    	
    	mStringBuilder.ensureCapacity(minimumCapacity);
    }
    
    /**
     * @see com.intuit.spc.foundations.portability.SpcfStringBuilder#setLength(int)
     */
    public void setLength(int newLength)
    {
    	try
		{
    		mStringBuilder.setLength(newLength);
		}
    	catch (IndexOutOfBoundsException ex)
		{
    		throw new SpcfIndexOutOfBoundsException(ex);
		}
    }

    /**
     * Replaces the entire string buffer with the new content 
     * 
     */
    protected SpcfStringBuilder replace(String str)
    {
    	mStringBuilder.replace(0, mStringBuilder.length(), str); 
    	return this;
    }
    
}
