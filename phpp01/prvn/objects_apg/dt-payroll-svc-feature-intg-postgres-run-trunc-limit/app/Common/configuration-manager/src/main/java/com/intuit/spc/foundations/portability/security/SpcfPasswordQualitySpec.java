/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;

/**
 * This class provides methods to store password specifications. 
 * @author Vishnu Shankar
 */
public abstract class SpcfPasswordQualitySpec {

	private int minUpperCaseChars=0;
	private int minLowerCaseChars=0;
	private int minLetterChars=0;
	private int minNumericChars=0;
	private int minSymbolChars=0;
	private int minLength=6;		// Default value.
	private int maxSequenceChars;
	private int maxConsecutiveChars;
	private String[] blackListedRegex;
	private char[] symbolSet = "~`’!@#$%^&*()_-+={}[]|\\:;<>,.?/”".toCharArray();		// Default Symbol Set.

	/**
	 * Get the minimum length for lower case characters.
	 * @return The minimum length as an integer value.
	 */
	public int getMinLowerCaseChars()
	{
		return minLowerCaseChars;
	}
	
	/**
	 * Get the minimum length for upper case characters.
	 * @return The minimum length as an integer value.
	 */
	public int getMinUpperCaseChars()
	{
		return minUpperCaseChars;
	}
	
	/**
	 * Get the minimum length for letter characters.
	 * @return The minimum length as an integer value.
	 */
	public int getMinLetterChars()
	{
		return minLetterChars;
	}
	
	/**
	 * Get the minimum length for numeric characters.
	 * @return The minimum length as an integer value.
	 */
	public int getMinNumericChars()
	{
		return minNumericChars;
	}
	
	/**
	 * Get the minimum length for symbol characters.
	 * @return The minimum length as an integer value.
	 */
	public int getMinSymbolChars()
	{
		return minSymbolChars;
	}
	
	/**
	 * Get the minimum length for the password.
	 * @return The minimum length as an integer value.
	 */
	public int getMinLength()
	{
		return minLength;
	}
	
	/**
	 * Get the maximum length for sequence characters.
	 * @return The maximum length as an integer value.
	 */
	public int getMaxSequenceChars()
	{
		return maxSequenceChars;
	}
	
	/**
	 * Get the maximum length for consecutive characters.
	 * @return The maximum length as an integer value.
	 */
	public int getMaxConsecutiveChars()
	{
		return maxConsecutiveChars;
	}
	
	/**
	 * Get the array of blacklisted regular expressions.
	 * @return String array of blacklisted regular expressions.
	 */
	public String[] getBlackListedRegex()
	{
		return blackListedRegex;
	}
	
	/**
	 * Get the array of symbol characters.
	 * @return Char array of symbol characters.
	 */
	public char[] getSymbolSet()
	{
		return symbolSet;
	}
	

	/**
	 * Set the minimum length for lowercase characters.
	 * @param minLowerCaseChars Minimum length for lowercase characters.
	 */
	protected void setMinLowerCaseChars(int minLowerCaseChars)
	{
		this.minLowerCaseChars = minLowerCaseChars;
	}

	/**
	 * Set the minimum length for uppercase characters.
	 * @param minLowerCaseChars Minimum length for uppercase characters.
	 */
	protected void setMinUpperCaseChars(int minUpperCaseChars)
	{
		this.minUpperCaseChars = minUpperCaseChars;
	}

	/**
	 * Set the minimum length for letter characters.
	 * @param minLetterChars Minimum length for letter characters.
	 */
	protected void setMinLetterChars(int minLetterChars)
	{
		this.minLetterChars = minLetterChars;
	}
	
	/**
	 * Set the minimum length for numeric characters.
	 * @param minNumericChars Minimum length for numeric characters.
	 */
	protected void setMinNumericChars(int minNumericChars)
	{
		this.minNumericChars = minNumericChars;
	}

	/**
	 * Set the minimum length for symbol characters.
	 * @param minSymbolChars Minimum length for symbol characters.
	 */
	protected void setMinSymbolChars(int minSymbolChars)
	{
		this.minSymbolChars = minSymbolChars;
	}
	
	/**
	 * Set the minimum length for the password.
	 * @param minLength Minimum length for password
	 */
	protected void setMinLength(int minLength)
	{
		this.minLength = minLength;
	}
	
	/**
	 * Set the maximum length for sequence characters.
	 * Ex.: "abc", "123", etc.
	 * @param maxSequenceChars Maximum length for sequence characters.
	 */
	protected void setMaxSequenceChars(int maxSequenceChars)
	{
		this.maxSequenceChars = maxSequenceChars;
	}
	
	/**
	 * Set the maximum length for consecutive characters.
	 * Ex.: "aaa", "111", etc.
	 * @param maxConsecutiveChars Maximum length for consecutive characters.
	 */
	protected void setMaxConsecutiveChars(int maxConsecutiveChars)
	{
		this.maxConsecutiveChars = maxConsecutiveChars;
	}
	
	/**
	 * Set an array of blacklisted regular expressions.
	 * @param blackListedRegex String array of blacklisted regular expressions.
	 */
	protected void setBlackListedRegex(String[] blackListedRegex)
	{
		this.blackListedRegex = blackListedRegex;
	}
	
	/**
	 * Set an array of symbol characters. 
	 * @param symbolSet Character array of symbol characters.
	 */
	protected void setSymbolSet(char[] symbolSet)
	{
		this.symbolSet = symbolSet;
	}
	
}

