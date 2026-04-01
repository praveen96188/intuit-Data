//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the PaymentReason object. This interface will be exposed through COM.
/// </summary>
public interface IPaymentReason
{
	/// <summary>
	/// Payment reason code to send to agency.
	/// </summary>
	String getPaymentReasonCode();

	/// <summary>
	/// True if this PaymentReason is obsolete.
	/// </summary>
	boolean getIsObsolete();

	/// <summary>
	/// Read-only property that indicates if this object is valid or not.
	/// If IsValid is true, it means that this object contains valid data.
	/// If IsValid is false, it means that this object was not initialized.
	/// </summary>
	boolean getIsValid();

	/// <summary>
	/// Payment reason text for the UI.
	/// </summary>
	String getDescription();

	/// <summary>
	/// Is tax item allowed?
	/// </summary>
	boolean getIsTaxItemAllowed();

	/// <summary>
	/// Is penalty allowed?
	/// </summary>
	boolean getIsPenaltyAllowed();

	/// <summary>
	/// Is interest allowed?
	/// </summary>
	boolean getIsInterestAllowed();

	/// <summary>
	/// Is other expense allowed?
	/// </summary>
	boolean getIsOtherExpenseAllowed();
}
