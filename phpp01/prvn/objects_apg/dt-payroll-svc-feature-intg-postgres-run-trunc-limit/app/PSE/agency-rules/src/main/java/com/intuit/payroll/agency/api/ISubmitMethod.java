
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

import com.intuit.payroll.agency.util.RulesCalendar;

/// <summary>
/// Interface for the SubmitMethod object. This interface will be exposed through COM.
/// </summary>
public interface ISubmitMethod
{
	/// <summary>
	/// Submit method type {enumerated String: PRINT, EFE-EPAY, OSP-WIRE, OSP-ACH}.
	/// </summary>
	String getSubmitMethodType();

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
	/// Submit method description String for UI.
	/// </summary>
	String getDescription();
	
	/// <summary>
	/// The id of enrollment group for this
	/// submit method.
	/// </summary>
	/// <remarks> Used to lookup enrollment
	/// group information elsewhere.</remarks>
	String getEnrollmentGroupID();
	
	/// <summary>
	/// Payment template id that owns this submit method.
	/// </summary>
	String getPaymentTemplateID();

	/// <summary>
	/// Submit time due.
	/// </summary>
	RulesCalendar getTimeDue();

	/// <summary>
	/// Number of days to bump the agency due date in order to get the agency send by date.  
	/// </summary>
	/// <remarks>
	/// If missing, default to 0 (agency send by date is same as agency due date).  
	/// For example, EFTPS should be "-1", meaning that the agency send by date is the day before the agency due date.  
	/// Note that this is independent of the padding required by EFE.  
	/// This field is for an actual agency requirement, on the agency servers.
	/// </remarks>
	int getSendByOffset();
	
	/// <summary>
	/// Default payment reason code for this submit method.
	/// </summary>
	String getDefaultPaymentReasonCode ();

    /// <summary>
    /// does each payment require bank account number and routing number?
    /// false means agency already know via enrollment.
    /// </summary>
    boolean getPaymentsRequireBankAccountInfo();

	/// <summary>
	/// Allow negative line items?
	/// </summary>
	boolean getIsNegativeLineItemAllowed();

	

	/// <summary>
	/// Interface to get a list of active payment reason codes.
	/// </summary>
	/// <returns>A list of payment reason code that is associated to this submit method.</returns>
	IRulesList getActivePaymentReasonCodeList();

	/// <summary>
	/// Interface to get a payment reason object, given a payment reason code.
	/// </summary>
	/// <param name="code">String that identify an unique payment reason</param>
	/// <returns>the payment reason object</returns>
	IPaymentReason getPaymentReason(String code);

	/// <summary>
	/// get a list of tax ids for which it is ok to send 
	/// negatives. 
	/// </summary>
	/// <returns>An IRulesList of integer tax IDs.</returns>
	/// <example>
	/// It's ok for AEIC to be negative.
	/// </example>
	IRulesList getTaxIDToAllowNegativesList();
	
	/// <summary>
	/// text description of the settlement rule for the user
	/// </summary>
	String getSettlementDescription();

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
	String getSettlementDescription(RulesCalendar today);

	/// <summary>
	/// the "today" used by the settlement date functions.  defaults to 
	/// the computer system date.  Only test clients use this property explicitly.
	/// </summary>
	void setToday(RulesCalendar that);

	/// <summary>
	/// Validate a potential settlement date based on the
	/// rules in the submit method.
	/// </summary>
	/// <param name="date">The date to validate.</param>
	/// <returns>
	/// True if the date is valid, false otherwise.
	/// </returns>
	boolean validateSettlementDate(RulesCalendar date);

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
	boolean validateSettlementDate(RulesCalendar date, RulesCalendar today);

	/// <summary>
	/// default settlement date, in practice the earliest possible
	/// settlement date.  call validSettlementDateExists
	/// before trusting this return value, as this method always returns
	/// something, but it may not be valid.
	/// </summary>
	RulesCalendar getDefaultSettlementDate ();

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
	RulesCalendar getDefaultSettlementDate (RulesCalendar today);
	
	/// <summary>
	/// The status of the item with regard to release level.
	/// </summary>
	ReleaseStatus getReleaseStatus();

    DueDateSpecifier getHowIsDueDateDefined();

    /// <summary>
    /// Sometimes a valid settlement date does not exist because of agency rules
    /// and EFE lag time.  Call this first to get fair warning.
    /// </summary>
    /// <returns>
    /// True if a valid settlement date exists.
    /// </returns>
    boolean validSettlementDateExists();

	/// qboe multiuser tests can't cache "today", they need to pass it as a parameter
    boolean validSettlementDateExists(RulesCalendar today);
}

