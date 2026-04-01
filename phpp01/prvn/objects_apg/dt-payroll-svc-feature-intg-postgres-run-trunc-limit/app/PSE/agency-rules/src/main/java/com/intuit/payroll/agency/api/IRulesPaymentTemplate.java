//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

import com.intuit.payroll.agency.util.RulesCalendar;

/// <summary>
/// Interface for the PaymentTemplate object. This interface will be exposed through COM.
/// </summary>
public interface IRulesPaymentTemplate
{
	/// <summary>
	/// Payment template id (e.g. "IRS941").
	/// </summary>
	String getPaymentTemplateID();

	/// <summary>
	/// True if this PaymentTemplate is obsolete.
	/// </summary>
	boolean getIsObsolete();

	/// <summary>
	/// Read-only property that indicates if this object is valid or not.
	/// If IsValid is true, it means that this object contains valid data.
	/// If IsValid is false, it means that this object was not initialized.
	/// </summary>
	boolean getIsValid();

	/// <summary>
	/// Payment description String for UI (e.g. "941 Payment").
	/// </summary>
	String getDescription();

	/// <summary>
	/// Long payment description String for UI (e.g. "The 941 Payment is blah blah blah").
	/// </summary>
	String getLongDescription();

	/// <summary>
	/// ID of the agency that owns this payment template.
	/// </summary>
	String getAgencyID();

	/// <summary>
	/// ID of the default payment frequency.
	/// </summary>
	String getDefaultPaymentFrequencyID();

	/// <summary>
	/// ID of the default submit method.
	/// </summary>
	String getDefaultSubmitMethodID();

	/// <summary>
	/// Id of the Holiday Group this template uses.
	/// </summary>
	String getHolidayGroupID();

	/// <summary>
	/// Given a frequency id, submit method id, and
	/// the date of the accrual, return an IPaymentPeriod 
	/// that applies to the accrual with a due date 
	/// adjusted for holidays and weekends.
	/// </summary>
	/// <param name="paymentPeriodRq">
	/// The payment period request object, which wraps the criteria for
	/// the construction of rules-based and user-defined payments.
	/// </param>
	/// <returns>An IPaymentPeriod with the start/end dates of the accrual 
	/// period and the due date (adjusted for weekends and holidays).</returns>
	IPaymentPeriod getPaymentPeriod( IPaymentPeriodRequest paymentPeriodRq );

    /// <summary>
    /// Payment due date retrieval method.
    /// </summary>
    /// <param name="paymentPeriodRq">Payment period request to calculate the due date for</param>
    /// <returns>RulesCalendar instance set to the due date the payment needs to be received by the agency.</returns>
    public RulesCalendar getPaymentDueDate ( IPaymentPeriodRequest paymentPeriodRq );

    /// <summary>
	/// List of tax law ids that apply to this payment.
	/// </summary>
	/// <returns>IListResponse collection of law IDs.</returns>
	IRulesList getLawIDList();

	/// <summary>
	/// List of available payment frequencies of this payment template.
	/// </summary>
	/// <returns>IListResponse collection of payment frequency IDs.</returns>
	IRulesList getActivePaymentFrequencyIDList();
	
	/// <summary>
	/// List of available payment frequencies of this payment template.
	/// </summary>
	/// <returns>IListResponse collection of payment frequency IDs.</returns>
	IRulesList getAgencyFormats();

	/// <summary>
	/// List of available submit methods of this payment template.
	/// </summary>
	/// <returns>IListResponse collection of submit method IDs.</returns>
	IRulesList getActiveSubmitMethodIDList();
	
	/// <summary>
	/// Interface to get payment frequency object, given a frequency ID.
	/// </summary>
	/// <param name="paymentFrequencyID">Payment Frequency ID</param>
	/// <returns>Frequency object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	IPaymentFrequency getPaymentFrequency(String paymentFrequencyID);
	
	/// <summary>
	/// Interface to get a submit method object, given a submit method type.
	/// </summary>
	/// <param name="submitMethodType">Submit method type.</param>
	/// <returns>SubmitMethod object. It always returns a non null object, client code should check for IsValid property of the returned object before using it.</returns>
	ISubmitMethod getSubmitMethod(String submitMethodType);

	/// <summary>
	/// Returns a list of holidays for this template.
	/// </summary>
	/// <returns>A List of IAgencyHolidays</returns>
	IRulesList getHolidayList();

	RoundingType getWageRounding();
	RoundingType getPaymentAmountRounding();
	ReconciliationFrequencyType getReconciliationFrequency();
	boolean getPaymentMaySpanReconciliationPeriods();
    String getPaymentTemplateAbbrev();
    String getUsesFrequencyOf();

    
}
