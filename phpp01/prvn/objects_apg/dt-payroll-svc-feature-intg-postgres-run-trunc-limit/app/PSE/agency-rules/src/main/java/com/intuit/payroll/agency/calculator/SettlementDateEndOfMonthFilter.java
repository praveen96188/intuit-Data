package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.impl.SettlementDateRequest;
import com.intuit.payroll.agency.util.RulesCalendar;
/// <summary>
/// Summary description for SettlementDateEndOfMonthFilter.
/// </summary>
class SettlementDateEndOfMonthFilter extends SettlementDateFilter
{
	public SettlementDateEndOfMonthFilter()
	{
	}

	/// <summary>
	/// Use the filter pattern to make sure the settlement date is in the current month.
	/// </summary>
	/// <param name="request">The SettlementDateRequest containing
	/// the criteria for the settlement date to obtain.</param>
	/// <param name="response">set response to 12/31/1999 if the filter fails
	/// </param>
	/// <remarks>
	/// This method should be overriden in subclasses to define what
	/// filtering work should be executed on the request and response.
	/// </remarks>
	public RulesCalendar execute(SettlementDateRequest request, RulesCalendar response)
	{
		// this filter moves the end date back, possibly before the start date, 
		// in which case no valid settlement date exists.
		if (!request.getIsStartDate())
		{
			// first the ThisQuarterOrFirstMonthOfNextQuarter filter
			if (request.getSettlementDatePolicy().getSettlementThisQuarterOrFirstMonthOfNextQuarter())
			{
				RulesCalendar date = request.getToday();
				//RulesCalendar a struct, so this is an independent copy.
				while ((date.getMonth()-1)/3 == (request.getToday().getMonth()-1)/3)
					// bump the month until it is in a different quarter
				{
					date = date.addMonths(1);
				}
				
				RulesCalendar lastDayFirstMonthNextQuarter = RulesCalendar.createCalendar(
					date.getYear(),
					date.getMonth(),
					date.getDaysInMonth()
				);

				if (lastDayFirstMonthNextQuarter.compareTo(response) < 0)
				{
					response = lastDayFirstMonthNextQuarter;
				}
			}

			// then the ThisMonth filter
			if (request.getSettlementDatePolicy().getSettlementThisMonth().equals("true") ||
					(request.getSettlementDatePolicy().getSettlementThisMonth().equals("ifFirstMonthOfQuarter") &&
						(request.getToday().getMonth() == 1 ||
						request.getToday().getMonth() == 4 ||
						request.getToday().getMonth() == 7 ||
						request.getToday().getMonth() == 10
					)
				)
			)
			{
				if (response.getMonth() != request.getToday().getMonth() || response.getYear() != request.getToday().getYear())
				{
					response = RulesCalendar.createCalendar(
						request.getToday().getYear(),
						request.getToday().getMonth(),
						request.getToday().getDaysInMonth());
				}
			}
			
		}
		
		return executeNext(request, response);
	}

}
