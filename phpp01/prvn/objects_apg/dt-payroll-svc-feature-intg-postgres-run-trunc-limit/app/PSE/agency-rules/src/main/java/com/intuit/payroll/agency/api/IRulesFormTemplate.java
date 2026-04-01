//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Interface for the FormTemplate type defined by the agency rules.
/// This interface will be exposed through COM.
/// </summary>
public interface IRulesFormTemplate
{
	/// <summary>
	/// The identification String defined by the agency rules.
	/// </summary>
	String getFormTemplateID();
	
	/// <summary>
	/// The ID of the agency associated with the form template.
	/// </summary>
	String getAgencyID();
	
	/// <summary>
	/// True if this form template is considered obsolete by
	/// Intuit.
	/// </summary>
	/// <remarks>Once a form template has been marked as obsolete,
	/// there should be a process used to "upgrade" previously used
	/// obsolete template to the new ones that should be used in 
	/// their place. TODO [zjm]: Update this remark with the actual 
	/// process to be used (from an agency rules perspective).</remarks>
	boolean getIsObsolete();
	
	/// <summary>
	/// The customer-facing name for this form template.
	/// </summary>
	/// <remarks>
	/// This String value can be used in product user
	/// interfaces.
	/// </remarks>
	String getName();
	
	/// <summary>
	/// The customer-facing description for this form template.
	/// </summary>
	/// <remarks>
	/// This String value can be used in product user interfaces.
	/// </remarks>
	String getDescription();
	
	/// <summary>
	/// Indicates if the filer is required to submit this form.
	/// </summary>
	boolean getIsRequired();
	
	/// <summary>
	/// Indicates the form template group this form belongs to,
	/// if any.
	/// </summary>
	/// <remarks>
	///	This is an optional field.
	/// </remarks>
	/// <returns>
	/// If present in the agency rules, will return the group id
	/// this form template belongs to.  If not present, then this
	/// property returns null.
	/// </returns>
	String getRulesFormTemplateGroupID();
	
	/// <summary>
	/// The id of the form as determined by Intuit
	/// Tax Dev.
	/// </summary>
	String getFormID();
	
	/// <summary>
	/// The form information and associated metadata.
	/// </summary>
	IFormInfo getFormInfo();
	
	/// <summary>
	/// The filing frequency for this form template.
	/// </summary>
	IFormFilingFrequency getFormFilingFrequency();
	
	/// <summary>
	/// Identifies the default submit method id to use if none
	/// is specified by the client.
	/// </summary>
	String getDefaultSubmitMethodID();
	
	/// <summary>
	/// get the ISubmitMethod object corresponding to the id passed
	/// as an argument.
	/// </summary>
	/// <param name="submitMethodID">The id of the submit method to retrieve.</param>
	/// <returns>An ISubmitMethod of the submit method identified by the
	/// argument. If no submit method with that id is referenced by this 
	/// form template then null is returned.</returns>
	IFormSubmitMethod getSubmitMethod(String submitMethodID);
	
	/// <summary>
	/// gets a list of law ids that apply to this form template.
	/// </summary>
	/// <returns>An IRulesList of String law ids.</returns>
	IRulesList getLawIDList();
	
	/// <summary>
	/// Retrieve the form template group that contains the current form
	/// template.
	/// </summary>
	/// <returns>An IFormTemplateGroup object that the form template
	/// instance belongs to.</returns>
	IFormTemplateGroup getFormTemplateGroup();
	
	/// <summary>
	/// Retrieve a list of all active submit methods on the form
	/// template.
	/// </summary>
	/// <returns>A list of String submit method IDs.</returns>
	IRulesList getActiveSubmitMethodIDList();
}
