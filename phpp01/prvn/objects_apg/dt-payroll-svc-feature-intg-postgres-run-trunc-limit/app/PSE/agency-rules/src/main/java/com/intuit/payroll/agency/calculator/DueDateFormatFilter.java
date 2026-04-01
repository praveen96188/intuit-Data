//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.impl.PaymentPeriod;
/// <summary>
/// DueDateFormatFilter ensures the formatting of
/// payment period fields is correct.
/// </summary>
class DueDateFormatFilter extends DueDateFilter
{
	/// <summary>
	/// Default constructor.
	/// </summary>
	public DueDateFormatFilter()
	{}
	
	/// <summary>
	/// Continuation constructor. Used when there are
	/// more filters after this to assign.
	/// </summary>
	/// <param name="filter">The next filter in the chain.</param>
	public DueDateFormatFilter(DueDateFilter filter)
	{
		m_nextFilter = filter;
	}

	/// <summary>
	/// This filter is used to do any final formatting of the 
	/// fields in the payment period.
	/// </summary>
	/// <param name="request">The PaymentPeriodRequest containing
	/// the criteria for the payment period to obtain.</param>
	/// <param name="response">The payment period to return, which
	/// could be in any state depending on it's place in the chain.</param>
	public PaymentPeriod execute(IPaymentPeriodRequest request, PaymentPeriod response)
	{
		response.fixupUIString();
		
		return executeNext(request, response);
	}
}
