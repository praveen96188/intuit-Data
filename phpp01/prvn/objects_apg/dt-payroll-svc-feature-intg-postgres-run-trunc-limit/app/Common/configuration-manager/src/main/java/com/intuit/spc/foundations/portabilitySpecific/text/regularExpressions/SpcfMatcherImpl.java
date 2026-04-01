package com.intuit.spc.foundations.portabilitySpecific.text.regularExpressions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.intuit.spc.foundations.portability.SpcfIllegalStateException;
import com.intuit.spc.foundations.portability.SpcfIndexOutOfBoundsException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfMatcher;

/**
 * The implementation for the abstract SpcfMatcher class.
 */
public class SpcfMatcherImpl extends SpcfMatcher 
{
	private Matcher mPlatformSpecific;
	
	public SpcfMatcherImpl(SpcfPatternImpl pattern, String input)
	{	
		super(pattern);
		SpcfParamValidator.checkIsNotNull(input, "input"); 		
		Pattern platformSpecificPattern = pattern.toSpecific();
		mPlatformSpecific = platformSpecificPattern.matcher(input);		
	}	

	@Override
	public int groupCount() 
	{
		return mPlatformSpecific.groupCount();
	}
	
	@Override
	public String group() 
	{
		try
		{
			return mPlatformSpecific.group();
		}
		catch(IllegalStateException ex)
		{
			throw new SpcfIllegalStateException(ex);
		}
	}

	@Override
	public String group(int group) 
	{
		try
		{
			return mPlatformSpecific.group(group);
		}
		catch(IllegalStateException ex)
		{
			throw new SpcfIllegalStateException(ex);
		}
		catch(IndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}

	@Override
	public boolean find() 
	{	
		return mPlatformSpecific.find();
	}
	
	@Override
	public int start()
	{	
		try
		{
			return mPlatformSpecific.start();
		}
		catch(IllegalStateException ex)
		{
			throw new SpcfIllegalStateException(ex);
		}
	}
	
	@Override
	public int start(int group) 
	{
		try
		{
			return mPlatformSpecific.start(group);
		}
		catch(IllegalStateException ex)
		{
			throw new SpcfIllegalStateException(ex);
		}
		catch(IndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}

	@Override
	public int end() 
	{
		try
		{
			return mPlatformSpecific.end();
		}
		catch(IllegalStateException ex)
		{
			throw new SpcfIllegalStateException(ex);
		}
	}
	
	@Override
	public int end(int group) 
	{		
		try
		{
			return mPlatformSpecific.end(group);
		}
		catch(IllegalStateException ex)
		{
			throw new SpcfIllegalStateException(ex);
		}
		catch(IndexOutOfBoundsException ex)
		{
			throw new SpcfIndexOutOfBoundsException(ex);
		}
	}		
}
