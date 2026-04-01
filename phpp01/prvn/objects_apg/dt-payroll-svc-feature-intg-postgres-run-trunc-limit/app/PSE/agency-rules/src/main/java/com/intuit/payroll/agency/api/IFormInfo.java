//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the FormInfo type defined by the agency rules.
/// This interface will be exposed through COM.
/// </summary>
/// <remarks>
/// This interface allows the Agency Rules component to provide data
/// that was heretofore provided by an INI file bundled with the
/// tax forms.
/// </remarks>
public interface IFormInfo
{
	/// <summary>
	/// Identifies the form set the form belongs to.
	/// </summary>
	/// <example>"STATE" or "FED"</example>
	String getFormSetID ();
	
	/// <summary>
	/// The TPS form id.
	/// </summary>
	String getFormID ();
	
	/// <summary>
	/// The customer-facing name of the form.
	/// </summary>
	/// <remarks>
	/// This value can be used in customer-facing user
	/// inetrfaces and messages.
	/// </remarks>
	String getFormName ();
	
	/// <summary>
	/// The category of the form.
	/// </summary>
	/// <remarks>This field is present to retain
	/// backwards compatibility with Denali forms.</remarks>
	String getFormCategory ();
	
	/// <summary>
	/// Defines how data must be totaled.
	/// </summary>
	/// <example>
	/// "Daily" - 941B requires daily breakdowns even 
	/// though it is filed quarterly.
	/// </example>
	String getDataBreakoutPeriod ();
	
	/// <summary>
	/// Does the form require SSN numbers in order
	/// to be processed?
	/// </summary>
	boolean getRequiresSSN ();
	
	/// <summary>
	/// Does the form support preprinted form printing?
	/// </summary>
	boolean getPreprinted ();
	
	/// <summary>
	/// Is this form available only in enhanced QuickBooks?
	/// </summary>
	/// <remarks>
	/// TODO [zjm] I don't really understand this field. Jim,
	/// could you comment further or correct my explanation.
	/// </remarks>
	boolean getEnhancedQBOnly ();
	
	/// <summary>
	/// Is the form a W2?
	/// </summary>
	boolean getIsW2 ();
	
	/// <summary>
	/// Is the form a 1099?
	/// </summary>
	boolean getIs1099 ();
	
	/// <summary>
	/// Is the form available only to a subscriber of
	/// the payroll service?
	/// </summary>
	/// <remarks>
	/// TODO [zjm] I don't really understand this field. Jim,
	/// could you comment further or correct my explanation.
	/// </remarks>
	boolean getSubscriberOnly ();
	
	/// <summary>
	/// Does this form require employee information?
	/// </summary>
	boolean getRequiresEmployeeInfo ();
	
	/// <summary>
	/// Does this form require tips data?
	/// </summary>
	/// <remarks>
	/// TODO [zjm] I don't really understand this field. Jim,
	/// could you comment further or correct my explanation.
	/// </remarks>
	boolean getRequiresTipsIncome ();
	
	/// <summary>
	/// Does this form require hours worked data?
	/// </summary>
	boolean getRequiresHoursWorked ();
	
	/// <summary>
	/// Does this form require employment duration data?
	/// </summary>
	boolean getRequiresEmployeeMonthsWorked ();
}
