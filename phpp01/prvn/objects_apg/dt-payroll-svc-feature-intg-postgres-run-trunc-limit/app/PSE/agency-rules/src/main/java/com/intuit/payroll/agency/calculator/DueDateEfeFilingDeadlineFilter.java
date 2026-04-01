//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.impl.IEfeRules;
import com.intuit.payroll.agency.impl.PaymentPeriod;
import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.portability.SpcfRuntimeException;
/// <summary>
/// DueDateEfeFilingDeadlineFilter adjusts the dueOn
/// date/time if the payment is a e-payment. It needs to be
/// adjusted according to the EFE rules in the rules.
/// </summary>
class DueDateEfeFilingDeadlineFilter extends DueDateFilter
{
	private IEfeRules m_efeRules = null;
	
	/// <summary>
	/// Termination constructor. Used when this is the last 
	/// filter in the chain.
	/// </summary>
	public DueDateEfeFilingDeadlineFilter()
	{
		initializeEfeRules();
	}
	
	/// <summary>
	/// Continuation constructor. Used when this instance has another
	/// filter after it in the chain.
	/// </summary>
	/// <param name="filter">The next filter in the chain.</param>
	public DueDateEfeFilingDeadlineFilter(DueDateFilter filter)
	{
		m_nextFilter = filter;
		initializeEfeRules();
	}
	
	private void initializeEfeRules()
	{
		IEfeRules efeRules = DAOFactory.getDAOFactory().getEfeRulesDAO().getEfeRules();
		if(efeRules == null)
		{
			throw new RuntimeException("EFERules are missing");
//			throw new SpcfRuntimeException("EFERules are missing");
		}
		m_efeRules = efeRules;
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
		RulesCalendar newDueDate = RulesCalendar.createCalendar(
            response.getDueDate().getYear(), // use date info from the calculated period
			response.getDueDate().getMonth(),
			response.getDueDate().getDay(),
			m_efeRules.getTimeDue().getHour(), // use the time info of the efe rules
			m_efeRules.getTimeDue().getMinute(),
			m_efeRules.getTimeDue().getSecond()
        );
			// add the number of days (usually negative) from the efe rules

		newDueDate = newDueDate.addDays(m_efeRules.getDaysOffsetFromAgencyDueDate());
		response.setDueDate(newDueDate);
		
		return executeNext(request, response);
	}
}
