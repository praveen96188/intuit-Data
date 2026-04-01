package com.intuit.spc.foundations.portabilitySpecific.text.regularExpressions;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfMatcher;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfPattern;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfPatternSyntaxException;

/**
 * Implementation class of portable abstract class- SpcfPattern.
 */
public class SpcfPatternImpl extends SpcfPattern
{
	private static final long serialVersionUID = -8539968704398053000L;
	
	private Pattern mPlatformSpecific;

	/**
	 * Compiles the given regular expression into a pattern.
	 * @param regex The expression to be compiled
     * @exception SpcfPatternSyntaxException If the regex's syntax is invalid
     * @exception SpcfArgumentNullException If regex is null
	 */
	public SpcfPatternImpl(String regex)
	{	
		super(regex);
		try
		{
			mPlatformSpecific = Pattern.compile(regex);
		}
		catch(PatternSyntaxException  ex)
		{
			throw new SpcfPatternSyntaxException(ex);
		}
	}
	
	/**
	 * Compiles the given regular expression into a pattern.
	 * @param regex The expression to be compiled
	 * @param options Match options
     * @exception SpcfPatternSyntaxException If the expression's syntax is invalid
     * @exception SpcfIllegalArgumentException If bit values other than those corresponding to the defined match 
     * flags are set in flags
     * @exception SpcfArgumentNullException If an argument is null
	 */
	public SpcfPatternImpl(String regex, int options)
	{	
		super(regex, options);
		
		//Convert SPC-F options into Java specific flags.
		int flags = 0;
		if((options & SpcfPattern.CaseInsensitive) == SpcfPattern.CaseInsensitive)
		{	
			flags |= Pattern.CASE_INSENSITIVE;
		}	
		if((options & SpcfPattern.SingleLine) == SpcfPattern.SingleLine)
		{	
			flags |= Pattern.DOTALL;
		}
		if((options & SpcfPattern.MultiLine) == SpcfPattern.MultiLine)
		{	
			flags |= Pattern.MULTILINE;
		}
		if((options & SpcfPattern.Comments) == SpcfPattern.Comments)
		{	
			flags |= Pattern.COMMENTS;
		}
		
		//Instantiate platform specific Pattern object.
		try
		{			
			mPlatformSpecific = Pattern.compile(regex, flags);
		}
		catch(PatternSyntaxException  ex)
		{
			throw new SpcfPatternSyntaxException(ex);
		}
		catch(IllegalArgumentException ex)
		{
			throw new SpcfIllegalArgumentException (ex);
		}
	}
	
	@Override
	public String getRegex()
	{
		return mPlatformSpecific.pattern();
	}	
	
	/**
     * Returns platform-specific object wrapped by the portability specific implementation.
	 * @return platform-specific object.
	 */
	public Pattern toSpecific()
	{		
		return mPlatformSpecific;		
	}

	@Override
	public SpcfMatcher matcher(String input) 
	{
		SpcfParamValidator.checkIsNotNull(input, "input");		
		return new SpcfMatcherImpl(this, input);					
	}	
}
