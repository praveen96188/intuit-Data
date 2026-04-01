package com.intuit.spc.foundations.portabilitySpecific.net;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.intuit.spc.foundations.portability.net.SpcfUrlDecoder;
import com.intuit.spc.foundations.portability.text.SpcfEncoding;
import com.intuit.spc.foundations.portability.text.SpcfUnsupportedEncodingException;

/**
 * The implementation for SpcfURLDecoder class.
 */
public class SpcfUrlDecoderImpl extends SpcfUrlDecoder
{
	/**
	 * @see com.intuit.spc.foundations.portability.net.SpcfUrlDecoder#doDecode(String, SpcfEncoding)
	 */
	@Override
	protected String doDecode(String encodedText, SpcfEncoding encoding) 
	{		
		if(encoding != SpcfEncoding.Utf8)
		{
			throw new SpcfUnsupportedEncodingException();
		}
		else if(encodedText != null)
		{		
			try 
			{
				return URLDecoder.decode(encodedText, encoding.getEncoding());
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
