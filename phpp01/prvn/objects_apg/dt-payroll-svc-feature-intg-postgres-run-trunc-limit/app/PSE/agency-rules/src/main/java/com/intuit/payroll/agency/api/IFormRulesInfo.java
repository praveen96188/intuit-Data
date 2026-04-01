//---------------------------------------------------------------------------
// Copyright 2006 Intuit Inc. All rights reserved. Unauthorized reproduction 
// is a violation of applicable law. This material contains certain 
// confidential or proprietary information and trade secrets of Intuit Inc.
//---------------------------------------------------------------------------
package com.intuit.payroll.agency.api;

/// <summary>
/// Global interface of the Payroll Tax Forms Rules Component. 
/// This interface will be exposed through COM.
/// </summary>
public interface IFormRulesInfo
{
	/// <summary>
	/// Returns a list of active form template ids.
	/// </summary>
	/// <returns>An IRulesList of id's corresponding to 
	/// valid IRulesFormTemplates.</returns>
	/// <remarks>
	/// These ids can be used by the client to retrieve 
	/// individual form templates using getFormTemplateByFormTemplateID(String id).
	/// </remarks>
	IRulesList getActiveFormTemplateIDList();
	
	/// <summary>
	/// Retrieve the IRulesFormTemplate object represented in the
	/// agency rules store by the supplied <code>id</code>.
	/// </summary>
	/// <param name="id">The id of the form template in
	/// the agency rules store.</param>
	/// <returns>An IRulesFormTemplate object represented by the supplied
	/// id in the agency rules.</returns>
	IRulesFormTemplate getFormTemplateByFormTemplateID(String id);
	
	/// <summary>
	/// Retrieve the IRulesFormTemplate object represented in the
	/// agency rules data store by the supplied <code>id</code>.
	/// </summary>
	/// <param name="id">The TPS form id of the tax form.</param>
	/// <returns>An IRulesFormTemplate object represented by the
	/// supplied form id in the agency rules.</returns>
	IRulesFormTemplate getFormTemplateByFormID(String id);
	
	/// <summary>
	/// Retrieve a list of form template ids that are referred to
	/// by the supplied list of law ids in the agency rules data store.
	/// </summary>
	/// <param name="lawIDs">A list of law ids to get a list of
	/// corresponding form template ids for.</param>
	/// <returns>An IRulesList filled with String formTemplateIDs.</returns>
	IRulesList getActiveFormTemplateIDListFromLawIDs(IRulesList lawIDs);
	
	/// <summary>
	/// Retrieve a form template group object that contains the form
	/// template referred to by the supplied form template id.
	/// </summary>
	/// <param name="id">The id of the form template to get
	/// the owner group for.</param>
	/// <returns>The form template group that includes the given
	/// form template.</returns>
	IFormTemplateGroup getFormTemplateGroup(String id);
	
	/// <summary>
	/// Retrieve a list of all non-obsolete form template group IDs.
	/// </summary>
	/// <returns>An IRulesList of form template group IDs.</returns>
	IRulesList getActiveFormTemplateGroupIDList();
}
