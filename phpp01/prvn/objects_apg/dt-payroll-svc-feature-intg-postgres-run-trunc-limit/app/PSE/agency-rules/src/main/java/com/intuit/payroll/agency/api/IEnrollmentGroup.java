//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the EnrollmentGroup object. This interface is exposed through COM.
/// </summary>
public interface IEnrollmentGroup
{
	/// <summary>
	/// ID of the enrollment group.
	/// </summary>
	String getEnrollmentGroupID();

	/// <summary>
	/// True is this enrollment group is obsolete.
	/// </summary>
	boolean getIsObsolete();
	
	/// <summary>
	/// Short "name" for the enrollment.
	/// </summary>
	String getUIString();
	
	/// <summary>
	/// Long text description for this enrollment.
	/// </summary>
	String getUIDescription();
	
	/// <summary>
	/// Is an enrollment process required for this group?
	/// </summary>
	boolean getEnrollmentRequired();
	
	/// <summary>
	/// Enroll via external web site, or from within client?
	/// </summary>
	boolean getEnrollViaWebsite();
	
	/// <summary>
	/// The html String which has instructions for the
	/// web enrollment process.
	/// </summary>
	String getWebSiteInstructions();
	
	/// <summary>
	/// URL to the enrollment page for the appropriate agency.
	/// </summary>
	String getWebSiteEnrollmentURL();

	/// <summary>
	/// Read-only property that indicates if this object is valid or not.
	/// If IsValid is true, it means that this object contains valid data.
	/// If IsValid is false, it means that this object was not initialized.
	/// </summary>
	boolean getIsValid();

	/// <summary>
	/// get the agency credential requirements.
	/// </summary>
	ICredentialData getCredentialData();
	
	/// <summary>
	/// get list of {payment template id, submit method type}.
	/// </summary>
	/// <returns>IListResponse collection of payment method rules.</returns>
	IRulesList getPaymentMethodRuleList();
	
	/// <summary>
	/// get the agency enrollment credential requirements
	/// </summary>
	/// <returns></returns>
	ICredentialData getEnrollmentCredentialData();

	/// <summary>
	/// Returns the Agency that this Enrollment Group is associated with.
	/// </summary>
	/// <returns>An instance of IAgency.</returns>
	IAgency getRelatedAgency();
}
