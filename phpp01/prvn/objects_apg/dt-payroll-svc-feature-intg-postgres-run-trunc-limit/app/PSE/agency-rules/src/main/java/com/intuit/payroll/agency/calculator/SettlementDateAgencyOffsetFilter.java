package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.impl.SettlementDateRequest;
/// <summary>
/// Summary description for SettlementDateAgencyOffsetFilter.
/// </summary>
class SettlementDateAgencyOffsetFilter extends SettlementDateFilter
{
	public SettlementDateAgencyOffsetFilter(SettlementDateFilter peer)
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
		if (response != null) 
		{
			throw new RuntimeException ("filter should start with null response");
		}
		response = request.getToday();

		if (request.getIsStartDate())
		{
			if (request.getSettlementDatePolicy().getSettlementMinOffsetUnit() == 'D') 
			{
				response = response.addDays (request.getSettlementDatePolicy().getSettlementMinOffset());
			}
			else if (request.getSettlementDatePolicy().getSettlementMinOffsetUnit() == 'M') 
			{
				response = response.addMonths (request.getSettlementDatePolicy().getSettlementMinOffset());
			}
			// Apply business day adjustment.
			if (!response.isBusinessDay(getHolidayList(request.getPaymentTemplateId()))) 
			{
				response = response.addBusinessDays(1, getHolidayList(request.getPaymentTemplateId()));
			}
		}
		else
		{
			if (request.getSettlementDatePolicy().getSettlementMaxOffsetUnit() == 'D')
			{
				response = response.addDays (request.getSettlementDatePolicy().getSettlementMaxOffset());
			}
			else if (request.getSettlementDatePolicy().getSettlementMaxOffsetUnit() == 'M')
			{
				response = response.addMonths (request.getSettlementDatePolicy().getSettlementMaxOffset());
			}
			// Apply business day adjustment.
			if (!response.isBusinessDay (getHolidayList(request.getPaymentTemplateId()))) 
			{
				response = response.subtractBusinessDays(1, getHolidayList(request.getPaymentTemplateId()));
			}
		}
								
		return executeNext(request, response);
	}
}
