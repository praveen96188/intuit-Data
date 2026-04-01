//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.UserFrequencyType;
import com.intuit.payroll.agency.impl.PaymentPeriod;
//import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
/// <summary>
/// DueDateUserValidationFilter ensures the request has the necessary parameters required
/// for processing.
/// </summary>
class DueDateUserValidationFilter extends DueDateFilter
{
	/// <summary>
	/// Default constructor.
	/// </summary>
	public DueDateUserValidationFilter()
	{}
	
	/// <summary>
	/// Continuation constructor. Used when there are
	/// more filters after this to assign.
	/// </summary>
	/// <param name="filter">The next filter in the chain.</param>
	public DueDateUserValidationFilter(DueDateFilter filter)
	{
		m_nextFilter = filter;
	}

	/// <summary>
	/// This filter will validate that the request for a user-defined date is valid (has all params).
	/// </summary>
	/// <param name="request">The PaymentPeriodRequest containing
	/// the criteria for the payment period to obtain.</param>
	/// <param name="response">The payment period to return, which
	/// could be in any state depending on it's place in the chain.</param>
	public PaymentPeriod execute(IPaymentPeriodRequest request, PaymentPeriod response)
	{
		validateRequest(request);
		return executeNext(request, response);
	}

	/// <summary>
	/// validates the payment period request for use by the user-defined payments
	/// due date calculator.
	/// </summary>
	/// <param name="paymentPeriodRq">Payment Period Request containing query parameters.</param>
	/// <exception cref="SpcfIllegalArgumentException">Thrown if argument is missing or invalid.</exception>
	protected void validateRequest( IPaymentPeriodRequest paymentPeriodRq )
	{
		// An Accrual date must be set.
		if( paymentPeriodRq.getAccrualDate() == null )
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "AccrualDate=" + paymentPeriodRq.getAccrualDate().toString());
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "AccrualDate=" + paymentPeriodRq.getAccrualDate().toString());
		}

		// We can't calculate stuff if there is no schedule.
		if( paymentPeriodRq.getUserFrequency() == UserFrequencyType.NoSchedule ) 
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "UserFreqency=" + paymentPeriodRq.getUserFrequency().toString());
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "UserFreqency=" + paymentPeriodRq.getUserFrequency().toString());
		}

		// In annual situation we need a hard date to apply.  no longer: null means due date is end of period.

		// If other than annual, we need a Schedule Number.  0 is a special case meaning due date is period end date.
		if ((paymentPeriodRq.getUserFrequency() == UserFrequencyType.CustomMonthly ||
			paymentPeriodRq.getUserFrequency() == UserFrequencyType.CustomQuarterly) && (paymentPeriodRq.getUserScheduleNum() < 0)) 
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "UserScheduleNum=" + paymentPeriodRq.getUserScheduleNum());
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "UserScheduleNum=" + paymentPeriodRq.getUserScheduleNum());
		}
		
		// The schedule number cannot be greate than 31
		if (paymentPeriodRq.getUserScheduleNum() > 31) 
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "UserScheduleNum=" + paymentPeriodRq.getUserScheduleNum());
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "UserScheduleNum=" + paymentPeriodRq.getUserScheduleNum());
		}

		// Check for weekly user schedule num > 6
		if ((paymentPeriodRq.getUserFrequency() == UserFrequencyType.CustomWeekly) && (paymentPeriodRq.getUserScheduleNum() > 6 || paymentPeriodRq.getUserScheduleNum() < 0))
		{
			throw new IllegalArgumentException("PaymentPeriodRequest is invalid for Weekly Frequency: " + "UserScheduleNum=" + paymentPeriodRq.getUserScheduleNum());
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest is invalid for Weekly Frequency: " + "UserScheduleNum=" + paymentPeriodRq.getUserScheduleNum());
		}
	}
}
