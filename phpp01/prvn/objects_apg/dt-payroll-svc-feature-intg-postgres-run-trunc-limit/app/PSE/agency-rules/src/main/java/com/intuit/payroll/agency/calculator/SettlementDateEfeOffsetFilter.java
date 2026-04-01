package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.impl.IEfeRules;
import com.intuit.payroll.agency.impl.SettlementDateRequest;
import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.portability.SpcfRuntimeException;
/// <summary>
/// Summary description for SettlementDateEfeOffsetFilter.
/// </summary>
class SettlementDateEfeOffsetFilter extends SettlementDateFilter
{
	private IEfeRules m_efeRules = null;

	public SettlementDateEfeOffsetFilter(SettlementDateFilter peer)
	{
		m_nextFilter = peer;

		IEfeRules efeRules = DAOFactory.getDAOFactory().getEfeRulesDAO().getEfeRules();
		if(efeRules == null)
		{
//			throw new ArgumentNullException("EFERules are missing");
			throw new RuntimeException("EFERules are missing");
//			throw new SpcfRuntimeException("EFERules are missing");
		}
		m_efeRules = efeRules;
	}

	private int getEfeOffset()
	{
		return -(m_efeRules.getDaysOffsetFromAgencyDueDate());
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
		if (request.getIsStartDate())
		{
			// Add time to the start date to EFE offset
			response = response.addDays(getEfeOffset());

			// Apply business day adjustment.
			if (!response.isBusinessDay (getHolidayList(request.getPaymentTemplateId()))) 
			{
				response = response.addBusinessDays (1, getHolidayList(request.getPaymentTemplateId()));
			}
		} 
		else
		{
			// Take off time from the end due to EFE offset
			response = response.addDays(- (getEfeOffset()));

			// Apply business day adjustment.
			if (!response.isBusinessDay (getHolidayList(request.getPaymentTemplateId()))) 
			{
				response = response.subtractBusinessDays(1, getHolidayList(request.getPaymentTemplateId()));
			}
		}

		return executeNext(request, response);
	}
}
