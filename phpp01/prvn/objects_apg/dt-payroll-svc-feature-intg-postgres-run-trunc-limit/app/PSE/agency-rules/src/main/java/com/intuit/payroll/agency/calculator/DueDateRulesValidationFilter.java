//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.calculator;

import com.intuit.payroll.agency.api.IPaymentPeriodRequest;
import com.intuit.payroll.agency.api.PaymentPeriodRequestType;
import com.intuit.payroll.agency.impl.PaymentPeriod;
//import com.intuit.spc.foundations.portability.SpcfIllegalArgumentException;
/// <summary>
/// DueDateRulesValidationFilter ensures the request has the necessary parameters required
/// for processing.
/// </summary>
class DueDateRulesValidationFilter extends DueDateFilter
{
	/// <summary>
	/// Default constructor.
	/// </summary>
	public DueDateRulesValidationFilter()
	{}
	
	/// <summary>
	/// Continuation constructor. Used when there are
	/// more filters after this to assign.
	/// </summary>
	/// <param name="filter">The next filter in the chain.</param>
	public DueDateRulesValidationFilter(DueDateFilter filter)
	{
		m_nextFilter = filter;
	}

	/// <summary>
	/// This filter will validate that the request for a Rules-defined date is valid (has all params).
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
	/// validates the payment period request for use by the rules-defined payments
	/// due date calculator.
	/// </summary>
	/// <param name="paymentPeriodRq">Payment Period Request containing query parameters.</param>
	/// <exception cref="SpcfIllegalArgumentException">Thrown if argument is missing or invalid.</exception>
	protected void validateRequest( IPaymentPeriodRequest paymentPeriodRq )
	{
		// If we got this far and it's not a rules defined we are in trouble.			
		if( paymentPeriodRq.getRequestType() != PaymentPeriodRequestType.RulesBased )
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "RequestType=" + paymentPeriodRq.getRequestType().toString());
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "RequestType=" + paymentPeriodRq.getRequestType().toString());
		}

		// An Accrual date must be set.
		if( paymentPeriodRq.getAccrualDate() == null )
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "AccrualDate=" + paymentPeriodRq.getAccrualDate().toString());
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "AccrualDate=" + paymentPeriodRq.getAccrualDate().toString());
		}

		// A Payment templateID would be nice too
		if( paymentPeriodRq.getPaymentTemplateId() == null || paymentPeriodRq.getPaymentTemplateId().length() == 0)
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "PaymentTemplateId=EMPTY");
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "PaymentTemplateId=EMPTY");
		}

		// A SubmitMethodID would be nice too
		if( paymentPeriodRq.getSubmitMethodId() == null || paymentPeriodRq.getSubmitMethodId().length() == 0)
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "SubmitMethodId=EMPTY");
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "SubmitMethodId=EMPTY");
		}

		// A FrequencyID would be nice too
		if( paymentPeriodRq.getFrequencyId() == null || paymentPeriodRq.getFrequencyId().length() == 0)
		{
			throw new IllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "FrequencyId=EMPTY");
//			throw new SpcfIllegalArgumentException("PaymentPeriodRequest argument is invalid: " + "FrequencyId=EMPTY");
		}
	}
}
