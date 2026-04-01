//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the FormSubmitMethod type defined by the agency rules.
/// This interface will be exposed through COM.
/// </summary>
/// <remarks>
/// This interface allows the Agency Rules component to provide data
/// about the submit methods of tax forms.
/// </remarks>
public interface IFormSubmitMethod
{
	/// <summary>
	/// The String id of the submit method.
	/// </summary>
	/// <example>
	/// "EFE-EFILE", "PRINT"
	/// </example>
	String getSubmitMethodType();
	
	/// <summary>
	/// Describes whether this submit method is
	/// no longer considered usable by Intuit
	/// tax development.
	/// </summary>
	boolean getIsObsolete();
	
	/// <summary>
	/// Defines the customer-facing name of this
	/// submit method.
	/// </summary>
	/// <example>"E-file", "Print"</example>
	/// <remarks>
	/// This field is cleared to be used in customer
	/// facing user interfaces.
	/// </remarks>
	String getDescription();
	
	/// <summary>
	/// Holds the ID used to retrieve enrollment data.
	/// </summary>
	String getEnrollmentGroupID();
	
	/// <summary>
	/// Number of days to bump the agency due date
	/// in order to get the agency send by date.
	/// </summary>
	int getSendByOffset();
	
	/// <summary>
	/// The status of the item with regard to release level.
	/// </summary>
	ReleaseStatus getReleaseStatus();
}
