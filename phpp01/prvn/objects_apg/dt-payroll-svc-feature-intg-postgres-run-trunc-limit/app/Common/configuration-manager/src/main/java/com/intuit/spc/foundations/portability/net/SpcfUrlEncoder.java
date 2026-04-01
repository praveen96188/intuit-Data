package com.intuit.spc.foundations.portability.net;

import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.text.SpcfEncoding;
import com.intuit.spc.foundations.portability.text.SpcfUnsupportedEncodingException;

/**
 * Utility class for HTML form encoding.
 * 
 * This class contains static methods for converting a String to the application/x-www-form-urlencoded MIME format. 
 * For more information about HTML form encoding, consult the HTML specification.
 * 
 * When encoding a String, the following rules apply:
 * 1. The alphanumeric characters "a" through "z", "A" through "Z" and "0" through "9" remain the same.<br>
 * 2. The special characters ".", "-", "*", and "_" remain the same. <br>
 * 3. The space character " " is converted into a plus sign "+". <br> 
 * 4. All other characters are unsafe and are first converted into one or more bytes using some encoding scheme. 
 * Then each byte is represented by the 3-character string "%xy", where xy is the two-digit hexadecimal representation 
 * of the byte. The recommended encoding scheme to use is UTF-8. However, for compatibility reasons, if an encoding is 
 * not specified, then the default encoding of the platform is used. <p>
 *
 * For example using UTF-8 as the encoding scheme the string "The string üfoo-bar" would get converted to
 * "The+string+%C3%BCfoo-bar" because in UTF-8 the character ü is encoded as two bytes C3 (hex) and BC (hex). 
 * 
 * @author mgarg
 *
 */
abstract public class SpcfUrlEncoder 
{
	/**
	 * The instance of the SPCF platform specific URL implementation
	 */
	private static SpcfUrlEncoder sInstance;
	
	static
	{
		sInstance = SpcfFactory.getInstance().createUrlEncoder();	
	}
	
	/**
	 * Translates a string into application/x-www-form-urlencoded format using a specific encoding scheme. 
	 * This method uses the supplied encoding scheme to obtain the bytes for unsafe characters. <p>
	 * 
	 * Note: The World Wide Web Consortium Recommendation states that UTF-8 should be used. Not doing so may 
	 * introduce incompatibilites. <p>
	 * 
	 * @param text String to be translated
	 * @param encoding The name of a supported character encoding. 
	 * @return the translated String. 
	 * @throws SpcfUnsupportedEncodingException  If the encoding is not supported
	 */
	protected abstract String doEncode(String text, SpcfEncoding encoding);
	
	/**
	 * Translates a string into application/x-www-form-urlencoded format using UTF-8 encoding scheme. <p>
	 * 
	 * Java puts encoded characters in uppercase but .NET uses lowercase. Though it does not change the 
	 * meaning of the encoded string but if this string is used in comparision, results will be different.<p>
	 * 
	 * Note: The World Wide Web Consortium Recommendation states that UTF-8 should be used. Not doing so may 
	 * introduce incompatibilites. <p>
	 *  
	 * @param text String to be encoded
	 * @return the encoded String. 
	 */
	public static String encode(String text)
	{	
		return encode(text, SpcfEncoding.Utf8);
	}
	
	
	/**
	 * Translates a string into application/x-www-form-urlencoded format using a specific encoding scheme. 
	 * This method uses the supplied encoding scheme to obtain the bytes for unsafe characters. <p>
	 * 
	 * Java puts encoded characters in uppercase but .NET uses lowercase. Though it does not change the 
	 * meaning of the encoded string but if this string is used in comparision, results will be different.<p>
	 * 
	 * Note: The World Wide Web Consortium Recommendation states that UTF-8 should be used. Not doing so may 
	 * introduce incompatibilites. <p>
	 * 
	 * @param text String to be encoded
	 * @param encoding The name of a supported character encoding. Only UTF-8 encoding scheme is supported currently.
	 * Passing any other encoding scheme will throw SpcfUnsupportedEncodingException.
	 * @return the encoded String. 
	 * @throws SpcfUnsupportedEncodingException  If the encoding is not supported 
	 */
	public static String encode(String text, SpcfEncoding encoding)
	{		
		return sInstance.doEncode(text, encoding);		
	}	
}
