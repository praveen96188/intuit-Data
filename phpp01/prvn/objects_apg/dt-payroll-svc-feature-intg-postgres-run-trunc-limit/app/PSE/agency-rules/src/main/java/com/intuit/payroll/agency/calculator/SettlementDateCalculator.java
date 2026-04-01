//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.util.RulesCalendar;
import com.intuit.payroll.agency.util.IAgencyHoliday;
import com.intuit.payroll.agency.impl.SettlementDatePolicy;
import com.intuit.payroll.agency.impl.SettlementDateRequest;
import com.intuit.payroll.agency.api.IRulesPaymentTemplate;
import com.intuit.payroll.agency.dao.DAOFactory;
/// <summary>
/// Provides methods for calculating the correct settlement date
/// which must fall on a business day.
/// </summary>
public class SettlementDateCalculator
{
	
//	private static final SettlementDateCalculator instance = new SettlementDateCalculator();
	private SettlementDateFilter m_filterChain = null;

	private SettlementDateCalculator()
	{
		m_filterChain = createChain();
	}

	/// <summary>
	/// Use this static method to get an the instance.  Not a singleton, could be, but if so lock carefully.
	/// </summary>
	/// <returns>An instance of the SettlementDateCalculator.</returns>
	public static SettlementDateCalculator getInstance()
	{
		return new SettlementDateCalculator();
	}

	/// <summary>
	/// get the settlement date based on the offset number and unit.
	/// </summary>
	/// <remarks>
	/// The method takes the date to start counting on as
	/// an argument.
	/// </remarks>
	/// <returns>A RulesCalendar corresponding to the calculated
	/// settlement date.</returns>
	public RulesCalendar getDefaultSettlementDate(RulesCalendar today, String paymentTemplateId, SettlementDatePolicy policy)
	{
		return getStartSettlementDate(today, paymentTemplateId, policy);
	}
	
	private RulesCalendar getStartSettlementDate(RulesCalendar today, String paymentTemplateId, SettlementDatePolicy policy)
	{
		return m_filterChain.execute(new SettlementDateRequest(today, true, paymentTemplateId, policy), null).trimTime();
	}
	
	private RulesCalendar getEndSettlementDate(RulesCalendar today, String paymentTemplateId, SettlementDatePolicy policy)
	{
		return m_filterChain.execute(new SettlementDateRequest(today, false, paymentTemplateId, policy), null).trimTime();
	}


	/// <summary>
	/// Validate whether the supplied date is an acceptable
	/// settlement date.
	/// </summary>
	/// <remarks>
	/// The method uses the supplied min- and max- offsets to establish
	/// a validation frame that starts minDayOffset number of business 
	/// days after the start date, and ends maxDayOffset number of business 
	/// days after the start date.  It then verifies whether the 
	/// dateToValidate is in the frame, and that it does not fall on 
	/// a weekend.
	/// </remarks>
	/// <returns>True if the supplied date is valid, false if it
	/// is not.</returns>
	public boolean validateSettlementDate(RulesCalendar dateToValidate, RulesCalendar today, String paymentTemplateId, SettlementDatePolicy policy) 
	{
		Iterable<IAgencyHoliday> holidayList = getHolidayList(paymentTemplateId);
//		SpcfCollectionIterable<IAgencyHoliday> holidayList = getHolidayList(paymentTemplateId);
		if (dateToValidate.isWeekend() || dateToValidate.isHoliday(holidayList))
		{
			return false;
		}

		RulesCalendar fullStartDate = getStartSettlementDate(today, paymentTemplateId, policy);
		RulesCalendar startDate = fullStartDate.trimTime();
		RulesCalendar endDate = getEndSettlementDate(today, paymentTemplateId, policy);

        return dateToValidate.compareTo(startDate) >= 0 && dateToValidate.compareTo(endDate) <= 0;


    }

	/// <summary>
	/// Sometimes a valid settlement date does not exist because of agency rules
	/// and EFE lag time.  Call this first to get fair warning.
	/// </summary>
	/// <returns>
	/// True if a valid settlement date exists.
	/// </returns>
	public boolean validSettlementDateExists(RulesCalendar today, String paymentTemplateId, SettlementDatePolicy policy)
	{
		RulesCalendar startDate = getStartSettlementDate(today, paymentTemplateId, policy);
		RulesCalendar endDate = getEndSettlementDate(today, paymentTemplateId, policy);

        return startDate.compareTo(endDate) <= 0;
    }
	
	protected Iterable<IAgencyHoliday> getHolidayList(String paymentTemplateId)
//	protected SpcfCollectionIterable<IAgencyHoliday> getHolidayList(String paymentTemplateId)
	{
		IRulesPaymentTemplate paymentTemplate = DAOFactory.getDAOFactory().getPaymentTemplateDAO().getPaymentTemplate(paymentTemplateId);
		return DAOFactory.getDAOFactory().getHolidayGroupDAO().getHolidays(paymentTemplate.getHolidayGroupID());
	}

	/// <summary>
	/// create the filter chain and return it for use in
	/// the calculator.
	/// 
	/// Pattern: Strategy
	/// Pattern: Filter
	/// </summary>
	/// <returns>A new SettlementDateFilter chain endpoint.</returns>
	/// <remarks>
	/// The filter chain is created in reverse-order from the
	/// way the filters will be called by the client.  It is important that the Agency
	/// filter run before the EFE offset filter in this case.
	/// </remarks>
	protected SettlementDateFilter createChain()
	{
		SettlementDateEndOfMonthFilter eomFilter = new SettlementDateEndOfMonthFilter ();
		SettlementDateEfeOffsetFilter efeFilter = new SettlementDateEfeOffsetFilter(eomFilter);
		return new SettlementDateAgencyOffsetFilter(efeFilter);
	}

	public String fixUpSettlementDescription(String description, RulesCalendar today, String paymentTemplateId, SettlementDatePolicy policy)
	{
        if (description == null || description.length() == 0)
        {
            return "";
        }

        if (validSettlementDateExists (today, paymentTemplateId, policy))
		{
			RulesCalendar startDate = getStartSettlementDate(today, paymentTemplateId, policy);
			RulesCalendar endDate = getEndSettlementDate(today, paymentTemplateId, policy);

            String result = description.replace("{0}", startDate.toString("M/d/yyyy"));
            result = result.replace("{1}", endDate.toString("M/d/yyyy"));
            return result;
		}
		else 
		{
			return "It is not possible to send to your agency at this time.";
		}

	}
}
