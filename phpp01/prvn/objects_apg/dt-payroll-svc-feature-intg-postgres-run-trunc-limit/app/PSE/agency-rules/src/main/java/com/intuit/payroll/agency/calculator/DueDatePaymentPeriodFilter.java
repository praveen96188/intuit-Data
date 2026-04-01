//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.NoResultsFoundException;
import com.intuit.payroll.agency.impl.PaymentPeriod;
import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.dao.mnemonics.InvalidMnemonicException;
/// <summary>
/// DueDatePaymentPeriodFilter retrieves the payment period using the request.
/// </summary>
class DueDatePaymentPeriodFilter extends DueDateFilter
{
	/// <summary>
	/// Default constructor.
	/// </summary>
	public DueDatePaymentPeriodFilter()
	{}
	
	/// <summary>
	/// Continuation constructor. Used when there are
	/// more filters after this to assign.
	/// </summary>
	/// <param name="filter">The next filter in the chain.</param>
	public DueDatePaymentPeriodFilter(DueDateFilter filter)
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
		// If an emergency date exists for the accrual period the supplied date is in, 
		// we should return it without further processing.  The dates are passed through.
		response= DAOFactory.getDAOFactory().getPaymentPeriodDAO().getPaymentPeriod(request);
        if (response == null)
        {
            // If no payment period is found that matches criteria, we will return throw an exception
            throw new NoResultsFoundException("No rules exist for the date and payment template id provided.");
        }
		return executeNext(request, response);
	}

}
