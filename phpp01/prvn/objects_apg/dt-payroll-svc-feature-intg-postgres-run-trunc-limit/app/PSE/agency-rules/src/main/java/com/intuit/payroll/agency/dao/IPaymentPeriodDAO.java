//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.dao;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.IPaymentPeriod;
import com.intuit.payroll.agency.impl.PaymentPeriod;

public interface IPaymentPeriodDAO
{
	/// <summary>
	/// Retrieve a PaymentPeriod object from the datasource where
	/// the accrual date in the PaymentPeriodRequest is in the period, and
	/// match the payment template id and frequency id provided.
	/// </summary>
	/// <param name="paymentPeriodRq">Request object containing query criteria.</param>
	/// <returns>A PaymentPeriod matching the criteria supplied by the arg.</returns>
	PaymentPeriod getPaymentPeriod(IPaymentPeriodRequest paymentPeriodRq);
}
