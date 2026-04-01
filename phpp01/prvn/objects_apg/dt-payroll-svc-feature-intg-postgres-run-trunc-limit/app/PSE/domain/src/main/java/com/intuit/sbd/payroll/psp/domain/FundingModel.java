package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class FundingModel extends BaseFundingModel {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public FundingModel()
	{
		super();
	}

    public interface Codes {
        public static String ONE_DAY = "1D";
        public static String TWO_DAY = "2D";
        public static String FIVE_DAY = "5D";
    }
}