//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.impl.SettlementDateRequest;
import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.util.IAgencyHoliday;
import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
//import com.intuit.spc.foundations.portability.collections.SpcfCollectionIterable;
/// <summary>
/// SettlementDateFilter defines a base class for
/// specific settlement date filters to inherit from.
/// </summary>
abstract class SettlementDateFilter
{
	/// <summary>
	/// The child m_filter of this this m_nextFilter instance.
	/// </summary>
	protected SettlementDateFilter m_nextFilter = null;
	
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
	public abstract RulesCalendar execute(SettlementDateRequest request, RulesCalendar response);
	
	/// <summary>
	/// A method subclasses can call to conditionally call the
	/// next filter in the chain (if one exists).
	/// </summary>
	/// <param name="request">The PaymentPeriodRequest containing
	/// the criteria for the payment period to obtain.</param>
	/// <param name="response">The payment period to return, which
	/// could be in any state depending on it's place in the chain.</param>
	protected RulesCalendar executeNext(SettlementDateRequest request, RulesCalendar response)
	{
		if(m_nextFilter != null)
		{
			return m_nextFilter.execute(request, response);
		}
		return response;
	}

	protected Iterable<IAgencyHoliday> getHolidayList(String paymentTemplateId)
//	protected SpcfCollectionIterable<IAgencyHoliday> getHolidayList(String paymentTemplateId)
	{
		IRulesPaymentTemplate paymentTemplate = DAOFactory.getDAOFactory().getPaymentTemplateDAO().getPaymentTemplate(paymentTemplateId);
		return DAOFactory.getDAOFactory().getHolidayGroupDAO().getHolidays(paymentTemplate.getHolidayGroupID());
	}
}
