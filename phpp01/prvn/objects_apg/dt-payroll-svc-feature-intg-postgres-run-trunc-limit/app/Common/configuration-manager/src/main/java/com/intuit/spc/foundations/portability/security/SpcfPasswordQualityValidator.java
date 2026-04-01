/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;

import com.intuit.spc.foundations.portability.SpcfPortabilityErrorEnum;
import com.intuit.spc.foundations.portability.SpcfSecurityException;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfMatcher;
import com.intuit.spc.foundations.portability.text.regularExpressions.SpcfPattern;

/**
 * This class validates a given password based on a given 
 * password quality spec.
 * @author Vishnu Shankar
 */
public class SpcfPasswordQualityValidator {

	private String password;
	private SpcfPasswordQualitySpec pwdSpec;


	/**
	 * Create a new instance of SpcfPasswordQualityValidator
	 * @return SpcfPasswordQualityValidator object.
	 */
	public static SpcfPasswordQualityValidator createInstance()
	{
		return new SpcfPasswordQualityValidator();
	}
	
	/**
	 * Set the password
	 * @param password Password string
	 */
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	/**
	 * Set the Password Quality Spec.
	 * @param spec Password Quality Spec.
	 */
	public void setPasswordQualitySpec(SpcfPasswordQualitySpec pwdSpec)
	{
		this.pwdSpec = pwdSpec;
	}
	
	/**
	 * Validate the password. This method uses the password and password quality spec 
	 * which have been set in this object.
	 * @return True if validated
	 * @throws SpcfSecurityException
	 */
	public boolean validatePassword()
	{
			return validatePassword(password, pwdSpec);
	}
	
	/**
	 * Validate the password based on the given password quality spec.
	 * The password is validated against the given password quality spec with regards to minimum length, 
	 * sequence characters, consecutive characters, symbol set, lowercase characters, uppercase characters, 
	 * letter characters, numeric characters and the blacklisted regular expression set.
	 * Examples for sequential characters that might fail based on the limitations specified in the password quality spec include:
	 *  "abcdef", "fedcba", "1357", "7531", "2468", "8642", "123456", "654321" 
	 * @param password Password string
	 * @param pwdSpec Password Quality Spec.
	 * @return True if validated.
	 * @throws SpcfSecurityException
	 */
	public boolean validatePassword(String password, SpcfPasswordQualitySpec pwdSpec)
	{
		
		int lowerCaseChars = 0;
		int upperCaseChars = 0;
		int letterChars = 0;
		int numericChars = 0;
		int symbolChars = 0;
		boolean match = false;
		

		if ( password == null )
		{
			throw new SpcfSecurityException("Password not specified", SpcfPortabilityErrorEnum.PASSWORD_NULL);
		}
		else if ( pwdSpec == null )
		{
			throw new SpcfSecurityException("Password Quality Spec not specified", SpcfPortabilityErrorEnum.PASSWORD_QUALITY_SPEC_NULL);
		}

		int maxSequenceChars = pwdSpec.getMaxSequenceChars();
		
		if ( maxSequenceChars == 0 )
			maxSequenceChars = password.length();
		
		int maxConsecutiveChars = pwdSpec.getMaxConsecutiveChars();
		
		if ( maxConsecutiveChars == 0 )
			maxConsecutiveChars = password.length();
		
		
		char[] symbolSet = pwdSpec.getSymbolSet();
		
		char[] charArray = password.toCharArray();
		
		int len = charArray.length;
		
		if ( len < 6 )
			throw new SpcfSecurityException("Password does not have necessary length. ", SpcfPortabilityErrorEnum.LENGTH_LESS_THAN_SIX_CHARS_ERROR);
		
		if ( len < pwdSpec.getMinLength() )
			throw new SpcfSecurityException("Password does not have necessary length. ", SpcfPortabilityErrorEnum.MIN_LENGTH_ERROR);
		
		
		for (int i=0; i<len; i++)
		{
			if ( Character.isLowerCase(charArray[i]) )
			{
				lowerCaseChars++;
				letterChars++;
			}
			else if ( Character.isUpperCase(charArray[i]) )
			{
				upperCaseChars++;
				letterChars++;
			}
			else if ( Character.isLetter(charArray[i]) )
				letterChars++;
			else if ( Character.isDigit(charArray[i]) )
				numericChars++;
			else
			{
				// Check for symbol characters.

				if ( symbolSet == null )
					throw new SpcfSecurityException("The password contains unacceptable characters. ", SpcfPortabilityErrorEnum.SYMBOL_CHARS_ERROR);
				
				match = false;
				int j = 0;
				do {
					if ( charArray[i] == symbolSet[j++] )
					{
						symbolChars++;
						match = true;
					}
						
				} while ( !match && j < symbolSet.length );
				
				if (!match)
					throw new SpcfSecurityException("The password contains characters which are not" +
							" part of the acceptable symbol set.", SpcfPortabilityErrorEnum.SYMBOL_CHARS_ERROR);
				
			}

			match = false;
			
			// Check for max sequence characters.
			// Ex.: If max sequence characters specified is 2, then "ab" is valid but "abc" is not valid.
			if ( i >= maxSequenceChars )
			{
				int k = maxSequenceChars;
				
				if ( Character.isLetter(charArray[i-k]) )
				{
					do {
						if ( Character.isLetter(charArray[i-k+1]) && (Character.toLowerCase(charArray[i-k+1]) == (Character.toLowerCase((char)(charArray[i-k] + 1)))) )
							match = true;
						else match = false;
						k--;
					} while ( match && k > 0 );
					
					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxSequenceChars + 
								" sequential characters." , SpcfPortabilityErrorEnum.MAX_SEQUENCE_CHARS_ERROR);
					
					// Check for sequence chars.
					// Ex.: fedcba
					k = maxSequenceChars;
					match = false;
					
					do {
						if ( Character.isLetter(charArray[i-k+1]) && (Character.toLowerCase(charArray[i-k+1]) == (Character.toLowerCase((char)(charArray[i-k] - 1)))) )
							match = true;
						else match = false;
						k--;
					} while ( match && k > 0 );
					
					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxSequenceChars + 
								" sequential characters." , SpcfPortabilityErrorEnum.MAX_SEQUENCE_CHARS_ERROR);


				} else if ( Character.isDigit(charArray[i-k]) )
				{
					
					// Check for sequence chars.
					// Ex.: 12345
					do {
						if ( Character.isDigit(charArray[i-k+1]))
						{
							int diff = charArray[i-k+1] - charArray[i-k]; 
							if ( diff ==1 )
								match = true;
							else match = false;
						} else match = false;
						k--;
					} while ( match && k > 0 );
					
					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxSequenceChars + 
								" sequential characters." , SpcfPortabilityErrorEnum.MAX_SEQUENCE_CHARS_ERROR);

					
					// Check for sequence chars.
					// Ex.: 2468, 1357
					k = maxSequenceChars;
					match = false;

					do {
						if ( Character.isDigit(charArray[i-k+1]))
						{
							int diff = charArray[i-k+1] - charArray[i-k]; 
							if ( diff == 2 )
								match = true;
							else match = false;
						} else match = false;
						k--;
					} while ( match && k > 0 );
					
					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxSequenceChars + 
								" sequential characters." , SpcfPortabilityErrorEnum.MAX_SEQUENCE_CHARS_ERROR);
					
					
					// Check for sequence chars.
					// Ex.: 7654
					k = maxSequenceChars;
					match = false;
					
					do {
						if ( Character.isDigit(charArray[i-k+1]))
						{
							int diff = charArray[i-k+1] - charArray[i-k]; 
							if ( diff == -1 )
								match = true;
							else match = false;
						} else match = false;
						k--;
					} while ( match && k > 0 );

					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxSequenceChars + 
								" sequential characters." , SpcfPortabilityErrorEnum.MAX_SEQUENCE_CHARS_ERROR);

					
					// Check for sequence chars.
					// Ex.: 7531, 8642
					k = maxSequenceChars;
					match = false;
					
					do {
						if ( Character.isDigit(charArray[i-k+1]))
						{
							int diff = charArray[i-k+1] - charArray[i-k]; 
							if ( diff == -2 )
								match = true;
							else match = false;
						} else match = false;
						k--;
					} while ( match && k > 0 );

					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxSequenceChars + 
								" sequential characters." , SpcfPortabilityErrorEnum.MAX_SEQUENCE_CHARS_ERROR);

				}
			}
			
			// Check for consecutive characters.
			if ( i >= maxConsecutiveChars )
			{
				int k = maxConsecutiveChars;
				match = false;
				
				
				if ( Character.isLetter(charArray[i-k]) )
				{
					do {
						if ( Character.isLetter(charArray[i-k+1]) && (Character.toLowerCase(charArray[i-k+1]) == (Character.toLowerCase(charArray[i-k]))) )
							match = true;
						else match = false;
						k--;
					} while ( match && k > 0 );
				
					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxConsecutiveChars + 
								" consecutive characters." , SpcfPortabilityErrorEnum.MAX_REPEATED_CONSECUTIVE_CHARS_ERROR);

					
				} 
				else 
				{
					do {
						if ( charArray[i-k+1] == charArray[i-k]  )
							match = true;
						else match = false;
						k--;
					} while ( match && k > 0 );

				
					if ( match )
						throw new SpcfSecurityException("The password has more than " + maxConsecutiveChars + 
								" consecutive characters." , SpcfPortabilityErrorEnum.MAX_REPEATED_CONSECUTIVE_CHARS_ERROR);
				
				}
			}
			
		}

		
		if ( lowerCaseChars < pwdSpec.getMinLowerCaseChars() )
			throw new SpcfSecurityException("The password has less than " + pwdSpec.getMinLowerCaseChars() + " lowercase characters", SpcfPortabilityErrorEnum.MIN_LOWER_CASE_CHARS_ERROR);
		
		if ( upperCaseChars < pwdSpec.getMinUpperCaseChars() )
			throw new SpcfSecurityException("The password has less than " + pwdSpec.getMinUpperCaseChars() + " uppercase characters", SpcfPortabilityErrorEnum.MIN_UPPER_CASE_CHARS_ERROR);

		if ( letterChars < pwdSpec.getMinLetterChars() )
			throw new SpcfSecurityException("The password has less than " + pwdSpec.getMinLetterChars() + " letter characters", SpcfPortabilityErrorEnum.MIN_LETTER_CHARS_ERROR);
		
		if ( numericChars < pwdSpec.getMinNumericChars() )
			throw new SpcfSecurityException("The password has less than " + pwdSpec.getMinNumericChars() + " numeric characters", SpcfPortabilityErrorEnum.MIN_NUMERIC_CHARS_ERROR);
		
		if ( symbolChars < pwdSpec.getMinSymbolChars() )
			throw new SpcfSecurityException("The password has less than " + pwdSpec.getMinSymbolChars() + " symbol characters", SpcfPortabilityErrorEnum.MIN_SYMBOL_CHARS_ERROR);


		// Finally check for blacklisted regular expressions.
		String[] blackList = pwdSpec.getBlackListedRegex();
		
		if ( blackList != null )
			validateBlackList(password, blackList);
		
		// If everything checks ok, return true.
		return true;
		
	}
	
	/**
	 * Validate the password against the black list regular expressions.
	 * @param password Password string.
 	 * @param blackList A string array of blacklist regular expressions.
	 * @return
	 */
	protected boolean validateBlackList(String password, String[] blackList)
	{
		for ( int i=0; i < blackList.length; i++ )
		{
			SpcfPattern pattern = SpcfPattern.createInstance(blackList[i]);
			
			SpcfMatcher matcher = pattern.matcher(password);
			
			
			if ( matcher.find() )
				throw new SpcfSecurityException("The password contains blacklisted set of characters." , SpcfPortabilityErrorEnum.BLACKLIST_MATCH_ERROR);
				
		}
		
		return true;
	}
	
}
