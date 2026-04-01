package com.intuit.sbd.payroll.psp.domain;

import com.intuit.sbd.payroll.psp.Application;

/**
 * Hand-written business logic
 */
public class TransactionState extends BaseTransactionState {

    /////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // Instance methods
    //
    /////////////////////////////////////////////////////////////////////////////////////////////////////////
	public TransactionState()
	{
		super();
	}

    
    public static TransactionState findTransactionState(TransactionStateCode code) {
        return Application.findById(TransactionState.class, code);
    }
}