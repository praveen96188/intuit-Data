package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class PayrollFrequency extends BasePayrollFrequency {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public PayrollFrequency()
	{
		super();
	}

    /**
	 * Internal interface with each of the codes from the database
 */
public interface Codes
{
    public static final String DAILY_MISC = "260";
    public static final String WEEKLY = "52";
    public static final String BI_WEEKLY = "26";
    public static final String SEMI_MONTHLY = "24";
    public static final String MONTHLY = "12";
    public static final String QUARTERLY = "4";
    public static final String SEMI_ANNUALLY = "2";
    public static final String ANNUAL = "1";
}
}