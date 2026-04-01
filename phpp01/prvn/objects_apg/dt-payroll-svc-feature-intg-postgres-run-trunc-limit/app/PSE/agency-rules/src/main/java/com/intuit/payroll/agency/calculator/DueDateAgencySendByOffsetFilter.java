//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.ISubmitMethod;
import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.impl.PaymentPeriod;
import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// DueDateAgencySendByOffsetFilter adjusts the dueOn
/// date/time if the payment is a e-payment. It needs to be
/// adjusted according to the Agency's requirements in the rules.
/// </summary>
class DueDateAgencySendByOffsetFilter extends DueDateFilter
{		
	/// <summary>
	/// Termination constructor. Used when this is the last 
	/// filter in the chain.
	/// </summary>
	public DueDateAgencySendByOffsetFilter()
	{
	}
	
	/// <summary>
	/// Continuation constructor. Used when this instance has another
	/// filter after it in the chain.
	/// </summary>
	/// <param name="filter">The next filter in the chain.</param>
	public DueDateAgencySendByOffsetFilter(DueDateFilter filter)
	{
		m_nextFilter = filter;
	}
	

	/// <summary>
	/// execute the supplied code on the request and modify the
	/// response parameter.
	/// </summary>
	/// <param name="request">The PaymentPeriodRequest containing
	/// the criteria for the payment period to obtain.</param>
	/// <param name="response">The payment period to return, which
	/// could be in any state depending on it's place in the chain.</param>
	/// <remarks>
	/// This method should be overriden in subclasses to define what
	/// filtering work should be executed on the request and response.
	/// </remarks>
	public PaymentPeriod execute(IPaymentPeriodRequest request, PaymentPeriod response)
	{
		ISubmitMethod submitMethod = DAOFactory.getDAOFactory().getSubmitMethodDAO().getSubmitMethod(request.getPaymentTemplateId(), request.getSubmitMethodId());
		if (submitMethod != null && submitMethod.getSendByOffset() != 0)
		{
			int bizDays = -(submitMethod.getSendByOffset());
            RulesCalendar dueDate = response.getDueDate().subtractBusinessDays(bizDays, getHolidaysForTemplate(request.getPaymentTemplateId()));
			response.setDueDate(dueDate);
		}

		return executeNext(request, response);
	}
}
