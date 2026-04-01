/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;

/**
 * This class implements the Intuit minimum password quality spec.
 * @author Vishnu Shankar
 */
public class SpcfIntuitMinimumPasswordQualitySpec extends SpcfPasswordQualitySpec {

	/**
	 * Constructor
	 */
	private SpcfIntuitMinimumPasswordQualitySpec()
	{
		setSpecValues();
	}
	
	/**
	 * Create a new instance of SpcfIntuitMinimumPasswordQualitySpec.
	 * @return SpcfIntuitMinimumPasswordQualitySpec object.
	 */
	public static SpcfIntuitMinimumPasswordQualitySpec createInstance()
	{
		return new SpcfIntuitMinimumPasswordQualitySpec();
	}
	
	/**
	 * Set the spec values.
	 */
	private void setSpecValues()
	{
		setMinLength(6);
	}
}
