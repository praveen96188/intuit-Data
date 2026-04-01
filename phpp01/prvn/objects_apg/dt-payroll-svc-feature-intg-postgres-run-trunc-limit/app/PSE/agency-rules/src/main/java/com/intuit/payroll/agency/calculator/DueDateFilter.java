//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.util.DueDateRollingPolicy;
import com.intuit.payroll.agency.util.IAgencyHoliday;
import com.intuit.payroll.agency.impl.PaymentPeriod;
//import com.intuit.spc.foundations.portability.collections.SpcfCollectionIterable;
/// <summary>
/// DueDateFilter defines a base class for
/// specific filters to inherit from.
/// </summary>
abstract class DueDateFilter
{
	/// <summary>
	/// The child m_filter of this this m_nextFilter instance.
	/// </summary>
	protected DueDateFilter m_nextFilter = null;
	
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
	public abstract PaymentPeriod execute(IPaymentPeriodRequest request, PaymentPeriod response);
	
	/// <summary>
	/// A method subclasses can call to conditionally call the
	/// next filter in the chain (if one exists).
	/// </summary>
	/// <param name="request">The PaymentPeriodRequest containing
	/// the criteria for the payment period to obtain.</param>
	/// <param name="response">The payment period to return, which
	/// could be in any state depending on it's place in the chain.</param>
	protected PaymentPeriod executeNext(IPaymentPeriodRequest request, PaymentPeriod response)
	{
		if(m_nextFilter != null)
		{
			return m_nextFilter.execute(request, response);
		}
		return response;
	}

	protected DueDateRollingPolicy getDatePolicy(IPaymentPeriodRequest paymentPeriodRq)
	{
		return DAOFactory.getDAOFactory().getPaymentTemplateDAO().getDueDatePolicy(paymentPeriodRq.getPaymentTemplateId());
	}

	protected IRulesPaymentTemplate getPaymentTemplate(String paymentTemplateID)
	{
		return DAOFactory.getDAOFactory().getPaymentTemplateDAO().getPaymentTemplate(paymentTemplateID);
	}

	protected Iterable<IAgencyHoliday> getHolidays(String holidayGroupID)
//	protected SpcfCollectionIterable<IAgencyHoliday> getHolidays(String holidayGroupID)
	{
		return DAOFactory.getDAOFactory().getHolidayGroupDAO().getHolidays(holidayGroupID);
	}

	protected Iterable<IAgencyHoliday> getHolidaysForTemplate(String paymentTemplateID)
//	protected SpcfCollectionIterable<IAgencyHoliday> getHolidaysForTemplate(String paymentTemplateID)
	{
		return getHolidays(getPaymentTemplate(paymentTemplateID).getHolidayGroupID());
	}
}
