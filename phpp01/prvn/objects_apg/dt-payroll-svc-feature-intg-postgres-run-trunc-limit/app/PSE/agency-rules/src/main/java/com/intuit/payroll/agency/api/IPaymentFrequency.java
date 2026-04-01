//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the Frequency object. This interface is exposed through COM.
/// </summary>
public interface IPaymentFrequency
{
	/// <summary>
	/// Payment frequency id. It should be unique to the PaymentTemplate that owns this Frequency, not necessarily globally unique.
	/// </summary>
	String getPaymentFrequencyID();

	/// <summary>
	/// True if this Frequency is obsolete.
	/// </summary>
	boolean getIsObsolete();

	/// <summary>
	/// Read-only property that indicates if this object is valid or not.
	/// If IsValid is true, it means that this object contains valid data.
	/// If IsValid is false, it means that this object was not initialized.
	/// </summary>
	boolean getIsValid();

	/// <summary>
	/// Short text blurb for the user, e.g. "Monthly".
	/// </summary>
	String getName();

	/// <summary>
	/// Long description for the user, e.g. "Payments for each month are due on the 10th of the following month.".
	/// </summary>
	String getDescription();

	/// <summary>
	/// ID of the payment template that owns this payment frequency.
	/// </summary>
	String getPaymentTemplateID();

    /// <summary>
    /// Indicates if you can span a Month boundary.
    /// </summary>
    boolean getDisallowMonthCrossing();

	/// <summary>
	/// Indicates if you can span a Quarter boundary.
	/// </summary>
	boolean getDisallowQuarterCrossing();

	/// <summary>
	/// Indicates if you can span a Year boundary.
	/// </summary>
	boolean getDisallowYearCrossing();

	/// <summary>
	/// Indicates if Holidays affect the DueDate
	/// </summary>
	boolean getAddHolidayAllowanceToDueDate();

     /// <summary>
	/// Indicates if Zero Payments are Required
	/// </summary>

	boolean  isZeroPaymentRequired();
    /// <summary>
	/// List of available upper limits this payment template.
	/// </summary>
	/// <returns>IListResponse collection of upper limits.</returns>
    
	//public abstract IRulesList getUpperLimitList();

}
