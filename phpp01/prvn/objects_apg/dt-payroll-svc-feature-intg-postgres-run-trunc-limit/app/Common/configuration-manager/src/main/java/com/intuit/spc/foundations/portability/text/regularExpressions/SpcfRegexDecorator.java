package com.intuit.spc.foundations.portability.text.regularExpressions;


/**
 * This class comes handy when a regular expression uses a construct that is natively supported by only 
 * ne platform (i.e. Java or .NET) and that construct is also supported by the SPC-F framework. <p> 
 * 
 * For an example POSIX character classes are supported by SPC-F but only Java (not .NET) natively supports 
 * this construct. Thus, to provide the same functionality in .NET, a .NET class can be derived from this 
 * class which overrides the decorate() method of this class. The overridden decorate() method will then  
 * replace all POSIX character class occurrences in the regex with their equivalent character classes. <p>
 * 
 * Thus when a regular expression is formatted like...<p>
 * 
 * "xyz\\p{Lower}"<p> 
 * 
 * then it will be translated into...<p>
 * 
 * "xyz[a-z]" in .NET <p>
 * 
 * and will have no effect on Java since Java natively supports POSIX character classes.
 * 
 * @author mgarg
 *
 */
abstract public class SpcfRegexDecorator 
{
	private String mRegexPattern;	
	
	/**
	 * This method returns a decorated regular expression. <p>
	 * @return decorated regex 
	 */
	public abstract String decorate();
	
	/**
	 * Returns the regex which will be decorated.
	 * @return regex to be decorated
	 */
	public String getRegexPattern()
	{
		return mRegexPattern;
	}
	
	/**
	 * Sets the regex which will be decorated.
	 * @param regexPattern regex to be decorated
	 */
	public void setRegexPattern(String regexPattern)
	{		
		mRegexPattern = regexPattern;
	}	
}
