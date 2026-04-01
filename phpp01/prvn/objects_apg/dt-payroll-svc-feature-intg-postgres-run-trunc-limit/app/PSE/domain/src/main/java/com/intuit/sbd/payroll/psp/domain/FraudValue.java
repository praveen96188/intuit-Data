package com.intuit.sbd.payroll.psp.domain;

/**
 * Hand-written business logic
 */
public class FraudValue extends BaseFraudValue {

	/**
	 * Default constructor.
	 */
	public FraudValue()
	{
		super();
	}

    public int getIntValue() {
        return Integer.parseInt(getValue());
    }
}