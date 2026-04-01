package com.intuit.sbd.payroll.psp.domain;

import com.intuit.spc.foundations.portability.util.SpcfDecimal;

/**
 * Hand-written business logic
 */
public class LimitValue extends BaseLimitValue {

	/**
	 * Default constructor.
	 */
	public LimitValue()
	{
		super();
	}

    public SpcfDecimal getDecimalValue() {
        return SpcfDecimal.createInstance(getValue());
    }

    public int getIntegerValue() {
        return Integer.parseInt(getValue());
    }

}