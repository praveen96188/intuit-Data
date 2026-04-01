package com.intuit.spc.foundations.portability.net;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.text.SpcfEncoding;
import com.intuit.spc.foundations.portability.text.SpcfUnsupportedEncodingException;

/**
 * Utility class for HTML form decoding. This class contains static methods for decoding a String from the 
 * application/x-www-form-urlencoded MIME format. <p>
 * 
 * The conversion process is the reverse of that used by the SpcfURLEncoder class. It is assumed that all 
 * characters in the encoded string are one of the following: "a" through "z", "A" through "Z", "0" through "9", 
 * and "-", "_", ".", and "*". The character "%" is allowed but is interpreted as the start of a special escaped
 * sequence. <p>
 * 
 * The following rules are applied in the conversion: <br>
 * 1. The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same. <br>
 * 2. The special characters ".", "-", "*", and "_" remain the same. <br>
 * 3. The plus sign "+" is converted into a space character " " . <br>
 * 4. A sequence of the form "%xy" will be treated as representing a byte where xy is the two-digit hexadecimal 
 * representation of the 8 bits. Then, all substrings that contain one or more of these byte sequences consecutively 
 * will be replaced by the character(s) whose encoding would result in those consecutive bytes. The encoding scheme 
 * used to decode these characters may be specified, or if unspecified, the default encoding of the platform will 
 * be used. <p>
 *
 * @author mgarg
 *
 */
abstract public class SpcfUrlDecoder 
{
	/**
	 * The instance of the SPCF platform specific URL implementation
	 */
	private static SpcfUrlDecoder sInstance;
	
	static
	{
		sInstance = SpcfFactory.getInstance().createUrlDecoder();
	}
	
	/**
	 * Decodes a application/x-www-form-urlencoded string using a specific encoding scheme. The supplied
     * encoding is used to determine what characters are represented by any consecutive sequences of the form "%xy".
	 * 
	 * Note: The World Wide Web Consortium Recommendation states that UTF-8 should be used. Not doing so may introduce 
	 * incompatibilites.
	 * 
	 * @param encodedText the String to decode
	 * @param encoding  The supported character encoding. 
	 * @return the newly decoded String 
	 * @throws SpcfUnsupportedEncodingException  If the encoding is not supported
	 */
	protected abstract String doDecode(String encodedText, SpcfEncoding encoding);
	
	/**
	 * Decodes a application/x-www-form-urlencoded string using UTF-8 encoding scheme.
	 * 
	 * Note: The World Wide Web Consortium Recommendation states that UTF-8 should be used. Not doing so may introduce 
	 * incompatibilites.
	 * 
	 * @param encodedText the String to decode
	 * @return the newly decoded String	
	 */
	public static String decode(String encodedText)
	{				
		String decodedText = null;		
		if(sInstance != null && encodedText != null)
		{	
			decodedText = sInstance.doDecode(encodedText, SpcfEncoding.Utf8);
		}	
		return decodedText;
	}
	
	/**
	 * Decodes a application/x-www-form-urlencoded string using a specific encoding scheme. The supplied encoding 
     * is used to determine what characters are represented by any consecutive sequences of the form "%xy".
	 * 
	 * Note: The World Wide Web Consortium Recommendation states that UTF-8 should be used. Not doing so may 
     * introduce incompatibilites.
	 * 
	 * @param encodedText the String to decode
	 * @param encoding  The supported character encoding. Only UTF-8 encoding scheme is supported currently; 
     * passing any other encoding scheme will throw SpcfUnsupportedEncodingException.
	 * @return the newly decoded String 
	 * @throws SpcfUnsupportedEncodingException  If the encoding is not supported
	 */
	public static String decode(String encodedText, SpcfEncoding encoding)
	{			
		return sInstance.doDecode(encodedText, encoding);		
	}
}
