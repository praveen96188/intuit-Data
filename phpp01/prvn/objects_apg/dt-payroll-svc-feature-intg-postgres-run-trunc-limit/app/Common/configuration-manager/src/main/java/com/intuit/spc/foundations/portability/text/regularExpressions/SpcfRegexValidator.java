package com.intuit.spc.foundations.portability.text.regularExpressions;

/**
 * This is a utility class which provides validate() method to check if a given regular expression is in S
 * PC-F compliant format. <p>
 * 
 * @author mgarg
 *
 */
public class SpcfRegexValidator 
{	
	/**
	 * Determines if a regex is SPC-F compatible. <p>
	 * 
	 * This method must be used by other SPC-F classes (e.g. SpcfStringBuilder) which provide methods to take regex 
     * as input paremeter. Before processing that regex, the method must make sure that the input regex is SPC-F 
     * compliant. <p>
	 * 
	 * User shall note that this function does not check if the given regular expression will compile or not. It
     * just verfies if all the constructs used in the regular expression are supported by both Java and .NET 
     * platforms.<p>
	 * 
	 * Funtion's behavior for different inputRegex is given below:<p>
	 * 
	 * <pre>
	 * inputRegex           valid           Comments
	 * ---------------------------------------------------------------------------------------------------------------------------
	 * "[\\p{Graph}]*"      true            The POSIX character class (\\p{Graph}) used in this regular expression 
     *                                      is supported by both Java (but not .NET) and SPC-F thus validation will
     *                                      be passed on this regex.
	 * 
	 * "([\\d]*)?+"         false           The possessive quantifier (?+) used in this regular expression in 
     *                                      supported only by Java and both .NET and SPC-F does not support this 
     *                                      quantifier, thus SpcfPatternSyntaxException will be thrown.					
	 * 
	 * "abcd[xyz"           false           Unbalanced bracket- [, used in this regular expression in supported 
     *                                      only .NET and both Java and SPC-F does not support this quantifier, thus
	 *                                      SpcfPatternSyntaxException will be thrown.
	 * 
	 * ""03[.-/]"           true            Even though this regex is invalid on both .NET and Java, it will pass 
     *                                      the validation test. Since both the platforms interpret this regex in the 
     *                                      same way (i.e. invalid), thus it is SPC-F compliant.
	 * 														
	 * </pre>
	 * 
	 * 
	 * @param inputRegex regular expression which needs to be validated
	 * @throws SpcfPatternSyntaxException if the regex is not in SPC-F compatibel format
	 */
	public static void validate(String inputRegex)
	{
		if(containsPossessiveQuantifier(inputRegex))
		{
			throw new SpcfPatternSyntaxException("possessive quantifiers are not supported in SPC-F!");
		}
	}	
	
	/**
	 * Inspect if a possessive quantifier is used in the input regex.
	 * @param inputRegex regular expression which needs to be inspceted
	 * @return true if the regex uses a possessive quantifier
	 */
	public static boolean containsPossessiveQuantifier(String inputRegex)
	{		
		boolean retVal = false;
		if(inputRegex != null)
		{			
			for(int i=0; i<inputRegex.length(); i++)
			{
				char curChar = inputRegex.charAt(i);
				if ((curChar == '[' || curChar == '{') && !SpcfRegexUtil.isEscaped(inputRegex, curChar, i))
                {
                    //Since possessive quantifiers can not be within these brackets, thus read all the characters 
                    //till the end of the closing bracket.
                    char endingBracket = curChar == '[' ? ']' : '}';
                    int closingBracketIdx = SpcfRegexUtil.findClosingBracketIdx(inputRegex, endingBracket, i);                    
                    
                    if(closingBracketIdx < 0 || closingBracketIdx >= inputRegex.length())
                    { 
                    	throw new SpcfPatternSyntaxException();
                    }
                    else
                    {
	                    //move the pointer
	                    i = closingBracketIdx;
                    }
                }                
                else if ((curChar == ']' || curChar == '}') && !SpcfRegexUtil.isEscaped(inputRegex, curChar, i))
                {
                    //if closing bracket comes before the opening bracket, that means regex is invalid
                    throw new SpcfPatternSyntaxException("Unbalanced brackets count");
                }
				if ((inputRegex.charAt(i) == '+' && !SpcfRegexUtil.isEscaped(inputRegex, curChar, i)) && i - 1 >= 0)					
				{
					char prevChar = inputRegex.charAt(i-1);
					if( (prevChar == '?' || prevChar == '+' || prevChar == '*' || prevChar == '}') && 
						!SpcfRegexUtil.isEscaped(inputRegex, prevChar, i - 1) )
					{
						retVal = true;
						break;
					}
				}
			}
		}
		return retVal;
	 }	
}
