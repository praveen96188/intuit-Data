package com.intuit.spc.foundations.portabilitySpecific.net;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.intuit.spc.foundations.portability.net.SpcfUrlEncoder;
import com.intuit.spc.foundations.portability.text.SpcfEncoding;
import com.intuit.spc.foundations.portability.text.SpcfUnsupportedEncodingException;

/**
 * The implementation class for the SpcfUrlEncoder
 */
public class SpcfUrlEncoderImpl extends SpcfUrlEncoder 
{	
	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrlEncoder#doEncode(String, SpcfEncoding)
	 */
	@Override
	protected String doEncode(String text, SpcfEncoding encoding) 
	{		
		if(encoding != SpcfEncoding.Utf8)
		{
			throw new SpcfUnsupportedEncodingException();
		}
		else if(text != null)
		{	
			try 
			{
				return URLEncoder.encode(text, encoding.getEncoding());
			}
			catch (UnsupportedEncodingException ex) 
			{	
				throw new SpcfUnsupportedEncodingException(ex);
			}
		}
		else 
		{
			return null;
		}
	}
}
