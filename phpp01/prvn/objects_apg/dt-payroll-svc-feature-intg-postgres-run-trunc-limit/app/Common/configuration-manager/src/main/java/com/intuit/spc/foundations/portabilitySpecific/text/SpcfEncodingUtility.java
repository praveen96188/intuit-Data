package com.intuit.spc.foundations.portabilitySpecific.text;

import com.intuit.spc.foundations.portability.text.*;


/**
 * This class is a utility class for helping resolve the portable character encoding
 * to the Java specific encoding name.
 **/
public class SpcfEncodingUtility 
{
	

	/**
     * Resolves the Spcf portable encoding name to the Java specific character encoding name
     *
     * @param portableEncodingName the Spcf portable encoding name 
     *
     * @return the Java specific encoding name
     *
    */	
	public static String getEncodingName(String portableEncodingName)
	{
		String javaEncodedName = null;
		if(portableEncodingName.compareTo(SpcfEncoding.Utf8.getEncoding())== 0)
		{
			javaEncodedName = "UTF8";
		}
		else if(portableEncodingName.compareTo(SpcfEncoding.Utf16BigEndianUnmarked.getEncoding())== 0)
		{
			javaEncodedName = "UnicodeBigUnmarked";
		}
		else if(portableEncodingName.compareTo(SpcfEncoding.Utf16BigEndianByteOrderMarked.getEncoding())== 0)
		{
			javaEncodedName = "UnicodeBig";
		}
		else if(portableEncodingName.compareTo(SpcfEncoding.Utf16LittleEndianUnmarked.getEncoding())== 0)
		{
			javaEncodedName = "UnicodeLittleUnmarked";
		}
		else if(portableEncodingName.compareTo(SpcfEncoding.Utf16LittleEndianByteOrderMarked.getEncoding())== 0)
		{
			javaEncodedName = "UnicodeLittle";
		}
		else if(portableEncodingName.compareTo(SpcfEncoding.UsAscii.getEncoding())== 0)
		{
			javaEncodedName = "ASCII";
		}
		else if(portableEncodingName.compareTo(SpcfEncoding.WindowsLatin1.getEncoding())== 0)
		{
			javaEncodedName = "Cp1252";
		}
		else if(portableEncodingName.compareTo(SpcfEncoding.Latin1.getEncoding())== 0)
		{
			javaEncodedName = "ISO8859_1";
		}
		
		return javaEncodedName;
	}
}
