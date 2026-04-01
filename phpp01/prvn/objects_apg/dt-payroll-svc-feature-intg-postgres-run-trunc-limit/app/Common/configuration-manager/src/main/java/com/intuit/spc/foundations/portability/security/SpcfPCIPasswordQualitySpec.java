/*
 * Intuit Inc. 2007
 */
package com.intuit.spc.foundations.portability.security;

/**
 * This class implements the PCI password quality spec.
 * @author Vishnu Shankar
 */
public class SpcfPCIPasswordQualitySpec extends SpcfPasswordQualitySpec {


	/**
	 * Constructor
	 */
	public SpcfPCIPasswordQualitySpec()
	{
		setSpecValues();
	}

	/**
	 * Create a new instance of SpcfPCIPasswordQualitySpec.
	 * @return SpcfPCIPasswordQualitySpec object.
	 */
	public static SpcfPCIPasswordQualitySpec createInstance()
	{
		return new SpcfPCIPasswordQualitySpec();
	}
	
	/**
	 * Set the spec values.
	 */
	private void setSpecValues()
	{
		setMinNumericChars(1);
		setMinLetterChars(1);
		setMinLength(7);
		setMinSymbolChars(0);
	}
}
