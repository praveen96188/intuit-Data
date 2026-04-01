//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------

package com.intuit.payroll.agency.api;
/// <summary>
/// Interface for the FormFilingFrequency type defined by the agency rules.
/// This interface will be exposed through COM.
/// </summary>
public interface IFormFilingFrequency
{
	/// <summary>
	/// The id of the form frequency.
	/// </summary>
	String getFormFrequencyID();
	
	/// <summary>
	/// True if this IFormFrequency is considered obsolete
	/// by Intuit.
	/// </summary>
	/// <remarks>Once a form frequency has been marked as obsolete,
	/// there should be a process used to "upgrade" previously used
	/// obsolete frequency to the new ones that should be used in 
	/// their place. TODO [zjm]: Update this remark with the actual 
	/// process to be used (from an agency rules perspective).</remarks>
	boolean getIsObsolete();
	
	/// <summary>
	/// A customer-facing short text description of the frequency.
	/// </summary>
	/// <example>"Monthly"</example>
	/// <remarks>
	/// This String value can be used in product user
	/// interfaces.
	/// </remarks>
	String getShortDescription();
	
	/// <summary>
	/// A customer facing long text description of the filing 
	/// frequency.
	/// </summary>
	/// <example>"Forms must be filed by the 10th of the first 
	/// month after the end of the quarter."</example>
	/// <remarks>
	/// This String value can be used in product interfaces.
	/// </remarks>
	String getLongDescription();
	
	/* ---- Not sure what to do here yet TODO [zjm]
	IFilingPeriod (?! zach) getFilingPeriod ();
			
	/// <summary>
	/// Indicates if holidays affect the form's due date.
	/// </summary>
	boolean getAddHolidayAllowanceToDueDate();
	*/
}
