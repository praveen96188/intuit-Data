//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.IPaymentFrequency;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.util.IAgencyHoliday;
import com.intuit.payroll.agency.impl.PaymentPeriod;
//import com.intuit.spc.foundations.portability.collections.SpcfCollectionIterable;
/// <summary>
/// Summary description for DueDateHolidayAllowanceFilter.
/// </summary>
class DueDateHolidayAllowanceFilter extends DueDateFilter
{
	
	/// <summary>
	/// No public default constructor.
	/// </summary>
	public DueDateHolidayAllowanceFilter()
	{
	}
	
	/// <summary>
	/// Constructor that takes a child m_nextFilter.
	/// </summary>
	/// <param name="aFilter">The next m_nextFilter in the chain
	/// to call.</param>
	public DueDateHolidayAllowanceFilter(DueDateFilter aFilter) {
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
		IRulesPaymentTemplate template = getPaymentTemplate(request.getPaymentTemplateId());
		// Used to determine if we add a day's grace to the due date.
		IPaymentFrequency frequency = template.getPaymentFrequency(request.getFrequencyId());

		if (frequency.getAddHolidayAllowanceToDueDate())
		{
			Iterable<IAgencyHoliday> holidayList = getHolidays(template.getHolidayGroupID());
//			SpcfCollectionIterable<IAgencyHoliday> holidayList = getHolidays(template.getHolidayGroupID());
			// Check to see if there is a holiday between the To Date and the Due date.
			RulesCalendar from = response.getToAccrualDate();
			RulesCalendar due = response.getDueDate();
            long diffInMilliseconds = due.subtract(from);
            long diffInDays = (diffInMilliseconds+100) / 1000 / 60 / 60 / 24;
            RulesCalendar dueDate = RulesCalendar.createCalendar(response.getDueDate());
            for (int i = 0; i < diffInDays; i++)
			{
				from = from.addDays(1);
				if (from.isHoliday(holidayList) && from.isWeekDay())
				{
					dueDate = dueDate.addBusinessDays(1);
				}
            }
            response.setDueDate(dueDate);
		}
		
		return executeNext(request, response);
	}
}
