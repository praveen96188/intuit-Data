//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.util.DueDateRollingPolicy;
import com.intuit.payroll.agency.util.IAgencyHoliday;
import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.impl.PaymentPeriod;
//import com.intuit.spc.foundations.portability.collections.SpcfCollectionIterable;
/// <summary>
/// Summary description for DueDateHolidayFilter.
/// </summary>
class DueDateHolidayFilter extends DueDateFilter
{
	
	/// <summary>
	/// No public default constructor.
	/// </summary>
	public DueDateHolidayFilter()
	{
	}
	
	/// <summary>
	/// Constructor that takes a child m_nextFilter.
	/// </summary>
	/// <param name="aFilter">The next m_nextFilter in the chain
	/// to call.</param>
	public DueDateHolidayFilter(DueDateFilter aFilter) {
		m_nextFilter = aFilter;
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
		DueDateRollingPolicy datePolicy = getDatePolicy(request);
		IRulesPaymentTemplate template = getPaymentTemplate(request.getPaymentTemplateId());
		
		Iterable<IAgencyHoliday> holidayList = getHolidays(template.getHolidayGroupID());
//		SpcfCollectionIterable<IAgencyHoliday> holidayList = getHolidays(template.getHolidayGroupID());
		if(response.getDueDate().isHoliday(holidayList)) {
            RulesCalendar dueDate = response.getDueDate().applyHolidayRollingPolicy(holidayList, datePolicy);
            response.setDueDate(dueDate);
		}
		
		return executeNext(request, response);
	}
}
