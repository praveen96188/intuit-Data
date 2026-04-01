//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.impl.PaymentPeriod;
import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.IPaymentFrequency;
import com.intuit.payroll.agency.dao.DAOFactory;
import com.intuit.payroll.agency.util.RulesCalendar;
//import com.intuit.spc.foundations.portability.SpcfFactory;
//import com.intuit.spc.foundations.portability.SpcfRuntimeException;

import java.util.ArrayList;

/// <summary>
/// DueDatePeriodBoundaryFilter ensures that the PaymentPeriod
/// is adjusted if we are spanning a Quarter or Year boundary.
/// </summary>
class DueDatePeriodBoundaryFilter extends DueDateFilter
{

	private ArrayList<PaymentPeriod> m_boundaryPeriods = new ArrayList<PaymentPeriod>();
	private PaymentPeriod m_yearPeriod = null;

	/// <summary>
	/// Default constructor.
	/// </summary>
	public DueDatePeriodBoundaryFilter()
	{
	}
	
	/// <summary>
	/// Constructor that takes a child m_nextFilter.
	/// </summary>
	/// <param name="aFilter">The next m_nextFilter in the chain
	/// to call.</param>
	public DueDatePeriodBoundaryFilter(DueDateFilter aFilter)
	{
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
		adjust(request, response);
		return executeNext(request, response);
	}

	/// <summary>
	/// Based on the policy of the agency, we adjust the payment period and due date.
	/// </summary>
	/// <remarks>
	/// If the policy disallows month, quarter or yearly boundaries, we delegate the adjustment to private helper
	/// methods.
	/// </remarks>
	/// <param name="request">The request object from the client.</param>
	/// <param name="response">The response we have calculated up to this point.</param>
	private void adjust(IPaymentPeriodRequest request, PaymentPeriod response)
	{
		IPaymentFrequency frequency = DAOFactory.getDAOFactory().getPaymentFrequencyDAO().getPaymentFrequency(request.getPaymentTemplateId(), request.getFrequencyId());

        // Check each of the boundary conditions starting with the most granular (monthly).
        if (frequency.getDisallowMonthCrossing())
        {
            initializeMonthBoundaryDates(request.getAccrualDate().getYear());
            for (PaymentPeriod period: m_boundaryPeriods)
            {
                if (HandlePeriodCrossing(period, request, response, frequency))
                {
                    // We found a crossing condition, stop looping.
                    break;
                }
            }
        }
        else if (frequency.getDisallowQuarterCrossing())
		{				
			// Boundary dates rely on the current year.
			initializeQuarterBoundaryDates(request.getAccrualDate().getYear());

	        for (PaymentPeriod period: m_boundaryPeriods)
			{
				if (HandlePeriodCrossing(period, request, response, frequency))
				{
					// We found a crossing condition, stop looping.
					break;
				}
			}
		} 
		else if (frequency.getDisallowYearCrossing())
		{
			initializeYearBoundaryDates(request.getAccrualDate().getYear());
			HandlePeriodCrossing(m_yearPeriod, request, response, frequency);
		}
	}

	/// <summary>
	/// Determines if the PaymentPeriod spans a quarter boundary, and invokes logic to adjust response.
	/// </summary>
	/// <param name="boundaryPeriod">The boundary period to check for (ie, the Quarter or Year).</param>
	/// <param name="request">The request object from the client.</param>
	/// <param name="response">The response we have calculated up to this point.</param>
	/// <returns>Returns true if we have hit a boundary condition.</returns>
	private boolean HandlePeriodCrossing(PaymentPeriod boundaryPeriod, IPaymentPeriodRequest request, PaymentPeriod response, IPaymentFrequency frequency)
	{
		// Establish if there are any boundary crossing conditions.  This occurs when the calculated
		// response's from or to dates span the boundary period's from or to dates.
		boolean fromBoundary = (response.getFromAccrualDate().compareTo(boundaryPeriod.getFromAccrualDate()) <= 0 && 
			response.getToAccrualDate().compareTo(boundaryPeriod.getFromAccrualDate()) >= 0);
		boolean toBoundary = (response.getFromAccrualDate().compareTo(boundaryPeriod.getToAccrualDate()) <= 0 && 
			response.getToAccrualDate().compareTo(boundaryPeriod.getToAccrualDate()) >= 0);
		
		// If either from or to have been crossed, we will have to adjust the period, and possibly the due date.
		if (fromBoundary|| toBoundary)
		{
			adjustPeriod(boundaryPeriod, request, response, toBoundary, frequency);
			return true;
		} 
		else 
		{
			return false;
		}
	}

	/// <summary>
	///  Adjusts the Period and Due date if the calculated PaymentPeriod spans a quarter boundary.
	/// </summary>
	/// <param name="boundaryPeriod">The boundary period to check for (ie, the Quarter or Year).</param>
	/// <param name="request">The request object from the client.</param>
	/// <param name="response">The response we have calculated up to this point.</param>
	/// <param name="toBoundary">Indicates if we are crossing the To boundary.</param>
	private void adjustPeriod(PaymentPeriod boundaryPeriod, IPaymentPeriodRequest request, PaymentPeriod response, boolean toBoundary, IPaymentFrequency frequency)
	{
		// Saving these for later, just in case.  We may have to recalc the due date if the to date changes.
		boolean adjustedToDate = false;

		// Now we have to move the From or To, depending on what side of the boundary the accrual date falls.
		if (toBoundary)
		{
			// If the client's AccrualDate is on the left side of the boundary To Date, move the To Date of the 
			// response to that of the boundary.  We will have to recalc the due date.
			if (request.getAccrualDate().compareTo(boundaryPeriod.getToAccrualDate()) <= 0)
			{
				response.setToAccrualDate(boundaryPeriod.getToAccrualDate());
                adjustedToDate = true;
			} 
				// If the AccrualDate is on the right side of the boundary To Date, move the From Date
				// of the response to be a day after the period's To (this gets us into the next quarter).
			else if (request.getAccrualDate().compareTo(boundaryPeriod.getToAccrualDate()) > 0)
			{
				response.setFromAccrualDate(boundaryPeriod.getToAccrualDate().addDays(1));
                adjustedToDate = true;
			}
		}
		else
		{
			response.setFromAccrualDate(boundaryPeriod.getFromAccrualDate());
		}

		// Only adjust the Due Date if the To Date changed.
		if (adjustedToDate)
		{
			adjustDueDate(response, frequency);
		}

	}

	/// <summary>
	/// Adjusts the Due Date, use when the To Date has moved.
	/// </summary>
	/// <param name="response">The calculated PaymentPeriod we are returning to the client.</param>
	private void adjustDueDate(PaymentPeriod response, IPaymentFrequency frequency)
	{
		// Someone set us up the bomb - the Period just changed on us, time to re-adjust the due date.
		if (response.getDueDatePolicy() != null)
		{
			if (response.getDueDatePolicy().getAdjustRelativeToPeriodEnd())
			{
                RulesCalendar dueDate;
				if (response.getDueDatePolicy().getIsBusinessDaysAdjustment())
				{
                    if (frequency.getAddHolidayAllowanceToDueDate()) {
                        dueDate = response.getToAccrualDate().addBusinessDays(response.getDueDatePolicy().getDaysAdjustment(), getHolidaysForTemplate(frequency.getPaymentTemplateID()));
                    }
                    else {
					    dueDate = response.getToAccrualDate().addBusinessDays(response.getDueDatePolicy().getDaysAdjustment());
                    }
				} 
				else
				{
					dueDate = response.getToAccrualDate().addDays(
							response.getDueDatePolicy().getDaysAdjustment());
				}
				response.setDueDate(dueDate);
			}
		}
		else 
		{
			throw new RuntimeException("This DueDatePolicy should have been set.  This means that the SEMIWEEKLY frequency for this agency is incorrectly encoded.");
//			throw new SpcfRuntimeException("This DueDatePolicy should have been set.  This means that the SEMIWEEKLY frequency for this agency is incorrectly encoded.");
		}
	}

    /// <summary>
    /// TODO: Perhaps this should come from the rules file?
    /// For now we are hard coding them.
    /// </summary>
    private void initializeMonthBoundaryDates(int year)
    {
        m_boundaryPeriods.clear();

        PaymentPeriod jan = new PaymentPeriod();
        jan.setFromAccrualDate(RulesCalendar.createCalendar(year, 1, 1));
        jan.setToAccrualDate(RulesCalendar.createCalendar(year, 1, 31));
        m_boundaryPeriods.add(jan);

        PaymentPeriod feb = new PaymentPeriod();
        feb.setFromAccrualDate(RulesCalendar.createCalendar(year, 2, 1));
        int lastDayOfFeb = (year & 3) == 0 ? 29 : 28;
        feb.setToAccrualDate(RulesCalendar.createCalendar(year, 2, lastDayOfFeb));
        m_boundaryPeriods.add(feb);

        PaymentPeriod mar = new PaymentPeriod();
        mar.setFromAccrualDate(RulesCalendar.createCalendar(year, 3, 1));
        mar.setToAccrualDate(RulesCalendar.createCalendar(year, 3, 31));
        m_boundaryPeriods.add(mar);

        PaymentPeriod apr = new PaymentPeriod();
        apr.setFromAccrualDate(RulesCalendar.createCalendar(year, 4, 1));
        apr.setToAccrualDate(RulesCalendar.createCalendar(year, 4, 30));
        m_boundaryPeriods.add(apr);

        PaymentPeriod may = new PaymentPeriod();
        may.setFromAccrualDate(RulesCalendar.createCalendar(year, 5, 1));
        may.setToAccrualDate(RulesCalendar.createCalendar(year, 5, 31));
        m_boundaryPeriods.add(may);

        PaymentPeriod jun = new PaymentPeriod();
        jun.setFromAccrualDate(RulesCalendar.createCalendar(year, 6, 1));
        jun.setToAccrualDate(RulesCalendar.createCalendar(year, 6, 30));
        m_boundaryPeriods.add(jun);

        PaymentPeriod jul = new PaymentPeriod();
        jul.setFromAccrualDate(RulesCalendar.createCalendar(year, 7, 1));
        jul.setToAccrualDate(RulesCalendar.createCalendar(year, 7, 31));
        m_boundaryPeriods.add(jul);

        PaymentPeriod aug = new PaymentPeriod();
        aug.setFromAccrualDate(RulesCalendar.createCalendar(year, 8, 1));
        aug.setToAccrualDate(RulesCalendar.createCalendar(year, 8, 31));
        m_boundaryPeriods.add(aug);

        PaymentPeriod sep = new PaymentPeriod();
        sep.setFromAccrualDate(RulesCalendar.createCalendar(year, 9, 1));
        sep.setToAccrualDate(RulesCalendar.createCalendar(year, 9, 30));
        m_boundaryPeriods.add(sep);

        PaymentPeriod oct = new PaymentPeriod();
        oct.setFromAccrualDate(RulesCalendar.createCalendar(year, 10, 1));
        oct.setToAccrualDate(RulesCalendar.createCalendar(year, 10, 31));
        m_boundaryPeriods.add(oct);

        PaymentPeriod nov = new PaymentPeriod();
        nov.setFromAccrualDate(RulesCalendar.createCalendar(year, 11, 1));
        nov.setToAccrualDate(RulesCalendar.createCalendar(year, 11, 30));
        m_boundaryPeriods.add(nov);

        PaymentPeriod dec = new PaymentPeriod();
        dec.setFromAccrualDate(RulesCalendar.createCalendar(year, 12, 1));
        dec.setToAccrualDate(RulesCalendar.createCalendar(year, 12, 31));
        m_boundaryPeriods.add(dec);

    }

	/// <summary>
	/// TODO: Perhaps this should come from the rules file?
	/// For now we are hard coding them.
	/// </summary>
	private void initializeQuarterBoundaryDates(int year)
	{
		m_boundaryPeriods.clear();
		PaymentPeriod qtr1 = new PaymentPeriod();
		qtr1.setFromAccrualDate(RulesCalendar.createCalendar(year, 1, 1));
		qtr1.setToAccrualDate(RulesCalendar.createCalendar(year, 3, 31));

		m_boundaryPeriods.add(qtr1);

		PaymentPeriod qtr2 = new PaymentPeriod();
		qtr2.setFromAccrualDate(RulesCalendar.createCalendar(year, 4, 1));
		qtr2.setToAccrualDate(RulesCalendar.createCalendar(year, 6, 30));

		m_boundaryPeriods.add(qtr2);

		PaymentPeriod qtr3 = new PaymentPeriod();
		qtr3.setFromAccrualDate(RulesCalendar.createCalendar(year, 7, 1));
		qtr3.setToAccrualDate(RulesCalendar.createCalendar(year, 9, 30));

		m_boundaryPeriods.add(qtr3);

		PaymentPeriod qtr4 = new PaymentPeriod();
		qtr4.setFromAccrualDate(RulesCalendar.createCalendar(year, 10, 1));
		qtr4.setToAccrualDate(RulesCalendar.createCalendar(year, 12, 31));

		m_boundaryPeriods.add(qtr4);
	}

	private void initializeYearBoundaryDates(int year)
	{
		PaymentPeriod yearPeriod = new PaymentPeriod();
		yearPeriod.setFromAccrualDate(RulesCalendar.createCalendar(year, 1, 1));
		yearPeriod.setToAccrualDate(RulesCalendar.createCalendar(year, 12, 31));
		m_yearPeriod = yearPeriod;
	}
}
