package com.intuit.spc.foundations.portability.text.regularExpressions;

import com.intuit.spc.foundations.portability.SpcfParamValidator;

/**
 * An engine that performs match operations on a character sequence by interpreting a Pattern. 
 * <p>
 * A matcher is created from a pattern by invoking the pattern's matcher method. The explicit state of a matcher
 * is initially undefined; attempting to query any part of it before a successful match will cause an 
 * SpcfIllegalStateException to be thrown. The explicit state of a matcher is recomputed by every match operation. 
 * <p> 
 * A matcher may be reset explicitly by invoking its reset() method or, if a new input sequence is desired, its 
 * reset(String) method. Resetting a matcher discards its explicit state information and sets the append position
 * to zero. 
 * <p>
 * Instances of this class are not safe for use by multiple concurrent threads. <p>
 * 
 * Code sample to match a string against a given regular expression: <p>
 * 
 * <pre>
 * public static void FindMatch()
 * {
 *   string sampleInput = "One car red car blue car";
 *   string sampleRegex = "(\\w+)\\s+(car)";
 *   try
 *   {
 *     SpcfPattern  pattern = SpcfPattern.create(sampleRegex);
 *     SpcfMatcher matcher = pattern.matcher(sampleInput);
 *     while (matcher.find())
 *     {	
 *       String matchedString = matcher.group();
 *       //print the matchedString
 *     }
 *   }
 *   catch(SpcfPatternSyntaxException ex)
 *   {
 *     //Invalid regular expression pattern
 *   }
 * } 
 * 
 * This prints following texts...
 * One car
 * red car
 * blue car
 *  
 * </pre>
 * @author mgarg 
 */
abstract public class SpcfMatcher 
{
	private SpcfPattern mPattern;	
	
	/**
	 * 
	 * @param pattern
	 */
	protected SpcfMatcher(SpcfPattern pattern)
	{	
		SpcfParamValidator.checkIsNotNull(pattern, "pattern");        
		mPattern = pattern;
	}
	
	/**
	 * Returns SpcfPattern object being used by this object.
	 * @return SpcfPattern used by the matcher
	 */
	public SpcfPattern getPattern()
	{	
		return mPattern;
	}
	
	/**
	 * Attempts to find the next subsequence of the input string that matches the pattern. <p>
	 * 
	 * This method starts at the beginning of the input string or, if a previous invocation of the method was 
     * successful and the matcher has not since been reset, at the first character not matched by the previous 
     * match. <p>
	 * 
	 * If the match succeeds then more information can be obtained via group method.
	 * 
	 * @return true if, and only if, a subsequence of the input sequence matches this matcher's pattern
	 */
	public abstract boolean find();
	
	/**
	 * Returns the number of capturing groups in this matcher's pattern. <p>
	 * 
	 * Group zero denotes the entire pattern by convention. It is not included in this count. <p>
	 * 
	 * Any non-negative integer smaller than or equal to the value returned by this method is guaranteed to be 
     * a valid group index for this matcher.
	 * @return The number of capturing groups in this matcher's pattern
	 */
	public abstract int groupCount();
	
	/**
	 * Returns the input subsequence matched by the previous match. <p>
	 * 
	 * Note that some patterns, for example a*, match the empty string. This method will return the empty string 
     * when the pattern successfully matches the empty string in the input.
	 * 
	 * @return The (possibly empty) subsequence matched by the previous match, in string form
	 * @throws SpcfIllegalStateException If no match has yet been attempted, or if the previous match operation 
     * failed
	 */
	public abstract String group();
	
	/**
	 * Returns the input subsequence captured by the given group during the previous match operation. <p>
	 *	  
	 * Capturing groups are indexed from left to right, starting at one. Group zero denotes the entire pattern, 
     * so the expression m.group(0) is equivalent to m.group(). <p>
	 * 
	 * If the match was successful but the group specified failed to match any part of the input sequence, then 
     * null is returned. Note that some groups, for example (a*), match the empty string. This method will return 
     * the empty string when such a group successfully matches the empty string in the input.
	 * 
	 * @param group  The index of a capturing group in this matcher's pattern.
	 * @return The (possibly empty) subsequence captured by the group during the previous match, or null if the group
     * failed to match part of the input.
	 * @throws SpcfIllegalStateException If no match has yet been attempted, or if the previous match operation failed 
	 * @throws SpcfIndexOutOfBoundsException If there is no capturing group in the pattern with the given index
	 */
	public abstract String group(int group);
	
	
	/**
	 * Returns the start index of the previous match. 
	 * @return The index of the first character matched
	 * @throws SpcfIllegalStateException If no match has yet been attempted, or if the previous match operation failed
	 */
	public abstract int start();
	
	/**
	 * Returns the start index of the subsequence captured by the given group during the previous match operation. <p>
	 * 
	 * Capturing groups are indexed from left to right, starting at one. Group zero denotes the entire pattern, so the 
	 * expression m.start(0) is equivalent to m.start(). <p>
	 * 
	 * @param group The index of a capturing group in this matcher's pattern 
	 * @return The index of the first character captured by the group, or -1 if the match was successful but the group
	 * itself did not match anything 
	 * @throws SpcfIllegalStateException If no match has yet been attempted, or if the previous match operation failed
	 * @throws SpcfIndexOutOfBoundsException If there is no capturing group in the pattern with the given index
	 */
	public abstract int start(int group);
	
	/**
	 * Returns the offset after the last character matched. 
	 * @return The offset after the last character matched 
	 * @throws SpcfIllegalStateException If no match has yet been attempted, or if the previous match operation failed
	 */
	public abstract int end();
	
	/**
	 * Returns the offset after the last character of the subsequence captured by the given group during the previous 
	 * match operation. <p>
	 * 
	 * Capturing groups are indexed from left to right, starting at one. Group zero denotes the entire pattern, so the 
	 * expression m.end(0) is equivalent to m.end(). <p>
	 * 
	 * @param group  The index of a capturing group in this matcher's pattern 
	 * @return The offset after the last character captured by the group, or -1 if the match was successful but the 
     * group itself did not match anything 
	 * @throws SpcfIllegalStateException If no match has yet been attempted, or if the previous match operation failed
	 * @throws SpcfIndexOutOfBoundsException If there is no capturing group in the pattern with the given index
	 */
	public abstract int end(int group);
	
}
