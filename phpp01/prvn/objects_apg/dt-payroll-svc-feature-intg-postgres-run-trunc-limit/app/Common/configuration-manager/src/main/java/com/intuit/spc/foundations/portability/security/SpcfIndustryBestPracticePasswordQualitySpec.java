/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;

/**
 * This class implements the industry best practice password quality spec. 
 * @author Vishnu Shankar
 */
public class SpcfIndustryBestPracticePasswordQualitySpec extends SpcfPasswordQualitySpec {

	/**
	 * Constructor
	 */
	private SpcfIndustryBestPracticePasswordQualitySpec()
	{
		setSpecValues();
	}

	/**
	 * Create a new instance of SpcfIndustryBestPracticePasswordQualitySpec.
	 * @return SpcfIndustryBestPracticePasswordQualitySpec object.
	 */
	public static SpcfIndustryBestPracticePasswordQualitySpec createInstance()
	{
		return new SpcfIndustryBestPracticePasswordQualitySpec();
	}
	
	/**
	 * Set the spec values.
	 */
	private void setSpecValues()
	{
		setMinUpperCaseChars(1);
		setMinLowerCaseChars(1);
		setMinNumericChars(1);
		setMinSymbolChars(1);
		setMaxSequenceChars(1);
		setMinLength(8);
	}
	
}
