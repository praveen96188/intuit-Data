//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.impl.SettlementDateRequest;
import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// Summary description for SettlementDateValidationFilter.
/// </summary>
class SettlementDateValidationFilter extends SettlementDateFilter
{
	public SettlementDateValidationFilter(SettlementDateFilter peer)
	{
		m_nextFilter = peer;
	}

	/// <summary>
	/// execute the supplied code on the request and modify the
	/// response parameter.
	/// </summary>
	/// <param name="request">The SettlementDateRequest containing
	/// the criteria for the settlement date to obtain.</param>
	/// <param name="response">The settlement date to return, which
	/// could be in any state depending on it's place in the chain.</param>
	/// <remarks>
	/// This method should be overriden in subclasses to define what
	/// filtering work should be executed on the request and response.
	/// </remarks>
	public RulesCalendar execute(SettlementDateRequest request, RulesCalendar response)
	{
		return executeNext(request, response);
	}
}
