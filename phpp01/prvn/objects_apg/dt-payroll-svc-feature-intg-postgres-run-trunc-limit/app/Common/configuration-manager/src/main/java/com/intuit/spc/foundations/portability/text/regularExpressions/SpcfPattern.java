package com.intuit.spc.foundations.portability.text.regularExpressions;

import com.intuit.spc.foundations.portability.SpcfArgumentNullException;
import com.intuit.spc.foundations.portability.SpcfFactory;
import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
import com.intuit.spc.foundations.portability.SpcfParamValidator;

/**
 * A compiled representation of a regular expression.
 * 
 * A regular expression, specified as a string, must first be compiled into an instance of this class. 
 * The resulting pattern can then be used to create a SpcfMatcher object that can match arbitrary string 
 * against the regular expression. All of the state involved in performing a match resides in the matcher,
 * so many matchers can share the same pattern.<p>
 * 
 * A typical invocation sequence is thus<p>
 * 
 * <pre>
 * SpcfPattern p = SpcfFactory.getInstance().createRegexPattern("a*b");
 * SpcfMatcher m = p.matcher("aaaaab");
 * boolean b = m.find();
 * </pre>
 * 
 * Instances of this class are immutable and are safe for use by multiple concurrent threads. 
 * Instances of the SpcfMatcher class are not safe for such use. 
 * 
 * @author mgarg
 *
 */
abstract public class SpcfPattern
{	
	/**
	 * No options.
	 */
	public static final int None = 0;
	 
	/**
	 * Enables case-insensitive matching.
	 */
	public static final int CaseInsensitive = 1;

	/**
	 * Specifies single-line mode. Changes the meaning of the dot (.) so it matches every character 
	 * (instead of every character except \n). 
	 */
	public static final int SingleLine = 2;
	
	/**
	 * Multiline mode. Changes the meaning of ^ and $ so they match at the beginning and end, respectively, of any line, 
	 * and not just the beginning and end of the entire string. 
	 */
	public static final int MultiLine = 4;
	
	/**
	 * Permits whitespace and comments in pattern. <p>
	 * 
	 * In this mode, whitespace is ignored, and embedded comments starting with # are ignored until the end of a line.
	 */
	public static final int Comments = 8;
	
	/**
	 * User selected options.
	 */
	private int mOptions = None;
	
	/**
	 * Compiles the given regular expression into a pattern. 
	 * @param regex The expression to be compiled 
	 * @return SpcfPattern compiled representation of regular expression
	 * @throws SpcfPatternSyntaxException If the regex's syntax is invalid
	 * @throws SpcfArgumentNullException If regex is null
	 */
	public static SpcfPattern createInstance(String regex)
	{	
		return SpcfFactory.getInstance().createRegexPattern(regex);
	}
	
	/**
	 * Compiles the given regular expression into a pattern. 
	 * @param regex The expression to be compiled 
	 * @param options Match options
	 * @return SpcfPattern compiled representation of regular expression
	 * @throws SpcfPatternSyntaxException If the expression's syntax is invalid
	 * @throws SpcfIllegalArgumentException If bit values other than those corresponding to the defined match flags 
     * are set in flags
	 * @throws SpcfArgumentNullException If an argument is null
	 */
	public static SpcfPattern createInstance(String regex, int options)
	{	
		return SpcfFactory.getInstance().createRegexPattern(regex, options);
	}
	
	/**
	 * 
	 * @param regex Regular expression to be compiled
	 * @throws SpcfPatternSyntaxException If the expression's syntax is invalid
	 * @throws SpcfArgumentNullException If an argument is null 
	 */
	protected SpcfPattern(String regex)
	{	
		this(regex, SpcfPattern.None);	
	}
	
	/**
	 * 
	 * @param regex Regular expression to be compiled
	 * @param options One or more options for compiling the regex. If more than 
	 * one option needs to be set, the options must be bitwise ORed. 
	 * @throws SpcfPatternSyntaxException If the expression's syntax is invalid
	 * @throws SpcfIllegalArgumentException If bit values other than those corresponding to the defined are set in 
     * options 
	 * @throws SpcfArgumentNullException If an argument is null  
	 */
	protected SpcfPattern(String regex, int options)
	{	
		SpcfParamValidator.checkIsNotNull(regex, "regex");	
		
		//Check that the options flags are set correctly		
		int validOptions = 0;
		if((options & SpcfPattern.CaseInsensitive) == SpcfPattern.CaseInsensitive)
		{				
			validOptions |= SpcfPattern.CaseInsensitive;
		}
		if((options & SpcfPattern.SingleLine) == SpcfPattern.SingleLine)
		{				
			validOptions |= SpcfPattern.SingleLine;
		}
		if((options & SpcfPattern.MultiLine) == SpcfPattern.MultiLine)
		{				
			validOptions |= SpcfPattern.MultiLine;
		}
		if((options & SpcfPattern.Comments) == SpcfPattern.Comments)
		{				
			validOptions |= SpcfPattern.Comments;
		}		
		
		//If the input parameter-options contains invalid bits, throw an exception
		if(validOptions != options)
		{
			throw new SpcfIllegalArgumentException();
		}
		else
		{		
			mOptions = options;
		}	
		
		SpcfRegexValidator.validate(regex);					
	}	
	
	/**
	 * Returns this pattern's match options.
	 * @return The match options specified when this pattern was compiled.
	 */
	public int getOptions()
	{
		 return mOptions;
	}
	
	/**
	 * Returns the regular expression from which this pattern was compiled. 
	 * @return The regular expression string of this pattern
	 */
	public abstract String getRegex();
	
	/**
	 * Creates a matcher that will match the given input string against this pattern.
	 * @param input The string to be matched 
	 * @return A new matcher for this pattern
	 * @throws SpcfArgumentNullException If an argument is null  
	 */
	public abstract SpcfMatcher matcher(String input);	

	/**
	 * Returns the regular expression pattern. 
	 */
	public String toString()
	{	
		return getRegex();
	}	
}
